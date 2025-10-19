package me.mirimomekiku.safepass.services

import android.app.assist.AssistStructure
import android.os.CancellationSignal
import android.service.autofill.AutofillService
import android.service.autofill.Dataset
import android.service.autofill.FillCallback
import android.service.autofill.FillContext
import android.service.autofill.FillRequest
import android.service.autofill.FillResponse
import android.service.autofill.SaveCallback
import android.service.autofill.SaveRequest
import android.text.InputType
import android.util.Log
import android.view.autofill.AutofillId
import android.view.autofill.AutofillValue
import android.widget.RemoteViews
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.mirimomekiku.safepass.R
import me.mirimomekiku.safepass.db.AppDatabase
import me.mirimomekiku.safepass.objects.CryptoUtil
import me.mirimomekiku.safepass.objects.TinkManager

class SafePassAutofillService : AutofillService() {

    override fun onFillRequest(
        request: FillRequest, cancellationSignal: CancellationSignal, callback: FillCallback
    ) {
        val context: List<FillContext> = request.fillContexts
        val structure: AssistStructure = context[context.size - 1].structure
        val appDao by lazy { AppDatabase.getDatabase(this).appCredentialsDao() }
        val websiteDao by lazy { AppDatabase.getDatabase(this).websiteCredentialsDao() }
        TinkManager.init(applicationContext)
        val aead = TinkManager.getAead()


        val webDomain = getWebDomain(structure)
        val packageName: String? = structure.activityComponent?.packageName
        Log.d("Autofill", "Web Domain: $webDomain")
        Log.d("Autofill", "Package Name: $packageName")

        val (usernameId, passwordId) = findUsernameAndPasswordIds(structure)
        if (usernameId == null || passwordId == null) {
            callback.onSuccess(null)
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            val responseBuilder = FillResponse.Builder()

            if (webDomain != null) {
                val credentialsList =
                    websiteDao.getCredentialsForUrl("https://$webDomain").map { cred ->
                        Log.d("Autofill", cred.toString())
                        cred.copy(password = CryptoUtil.decrypt(aead, cred.password, webDomain))
                    }

                if (credentialsList.isNotEmpty()) {
                    credentialsList.forEach { creds ->
                        val dataset =
                            createDataset(creds.username, creds.password, usernameId, passwordId)
                        responseBuilder.addDataset(dataset)
                    }
                } else {
                    callback.onSuccess(null)
                    return@launch
                }


            } else if (packageName != null) {
                val credentialsList = appDao.getCredentialsForApp(packageName).map { cred ->
                    Log.d("Autofill", cred.toString())
                    cred.copy(password = CryptoUtil.decrypt(aead, cred.password, cred.packageName))
                }

                if (credentialsList.isNotEmpty()) {
                    credentialsList.forEach { creds ->
                        val dataset =
                            createDataset(creds.username, creds.password, usernameId, passwordId)
                        responseBuilder.addDataset(dataset)
                    }
                } else {
                    callback.onSuccess(null)
                    return@launch
                }
            }

            val response = responseBuilder.build()
            callback.onSuccess(response)
        }
    }


    private fun createDataset(
        username: String,
        password: String,
        usernameId: AutofillId,
        passwordId: AutofillId
    ): Dataset {
        val presentation = newDatasetPresentation(
            applicationContext, username, R.drawable.ic_launcher_foreground
        )

        return Dataset.Builder(presentation)
            .setValue(usernameId, AutofillValue.forText(username))
            .setValue(passwordId, AutofillValue.forText(password)).build()
    }

    private fun newDatasetPresentation(
        context: android.content.Context, text: CharSequence, iconResId: Int
    ): RemoteViews {
        val presentation = RemoteViews(context.packageName, android.R.layout.simple_list_item_1)
        presentation.setTextViewText(android.R.id.text1, text)
        return presentation
    }

    private fun getWebDomain(structure: AssistStructure): String? {
        for (i in 0 until structure.windowNodeCount) {
            val windowNode = structure.getWindowNodeAt(i)
            val viewNode = windowNode.rootViewNode
            var domain: String? = null
            traverseNode(viewNode) { node ->
                if (node.webDomain != null) {
                    domain = node.webDomain
                    return@traverseNode
                }
            }
            if (domain != null) {
                return domain
            }
        }
        return null // Return null if no domain is found in any window
    }

    private fun findUsernameAndPasswordIds(structure: AssistStructure): Pair<AutofillId?, AutofillId?> {
        var usernameId: AutofillId? = null
        var passwordId: AutofillId? = null

        for (i in 0 until structure.windowNodeCount) {
            val windowNode = structure.getWindowNodeAt(i)
            val viewNode = windowNode.rootViewNode
            traverseNode(viewNode) { node ->
                val hints = node.autofillHints?.map { it.lowercase() } ?: emptyList()
                val viewId = node.idEntry?.lowercase() ?: ""
                val hintText = node.hint?.lowercase() ?: ""
                val isPasswordInputType =
                    node.inputType and (InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD) == (InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)

                // --- Heuristics for Username Field ---
                if (usernameId == null) {
                    if (hints.any { it in listOf("username", "email", "emailaddress") } ||
                        viewId.contains("user") || viewId.contains("email") ||
                        hintText.contains("user") || hintText.contains("email") || hintText.contains(
                            "login"
                        )) {
                        usernameId = node.autofillId
                    }
                }

                // --- Heuristics for Password Field ---
                if (passwordId == null) {
                    if (hints.contains("password") || isPasswordInputType ||
                        viewId.contains("password") || hintText.contains("password")
                    ) {
                        passwordId = node.autofillId
                    }
                }
            }
        }
        return Pair(usernameId, passwordId)
    }

    private fun traverseNode(
        node: AssistStructure.ViewNode, onNode: (AssistStructure.ViewNode) -> Unit
    ) {
        onNode(node)
        for (i in 0 until node.childCount) {
            traverseNode(node.getChildAt(i), onNode)
        }
    }

    override fun onSaveRequest(request: SaveRequest, callback: SaveCallback) {
        // This is where you would handle the "Save to SafePass?" dialog.
        // For now, we'll keep it simple and not implement saving.
        // In a full implementation, you'd parse the request, get the new credentials,
        // and use dao.insertCredentials() or dao.updateCredentials().
        callback.onSuccess() // Or onFailure
    }

    override fun onDisconnected() {
        super.onDisconnected()
    }
}



