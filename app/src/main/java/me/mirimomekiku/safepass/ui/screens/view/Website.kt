package me.mirimomekiku.safepass.ui.screens.view

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextObfuscationMode
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecureTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import coil3.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.mirimomekiku.safepass.R
import me.mirimomekiku.safepass.db.AppDatabase
import me.mirimomekiku.safepass.db.entity.WebsiteCredentials
import me.mirimomekiku.safepass.objects.CryptoUtil
import me.mirimomekiku.safepass.objects.TinkManager
import me.mirimomekiku.safepass.ui.compositions.LocalNavController

@Composable
fun ViewWebsiteCredentialScreen(navBackStackEntry: NavBackStackEntry) {
    val context = LocalContext.current
    val navController = LocalNavController.current
    val id = navBackStackEntry.arguments?.getString("id")?.toIntOrNull() ?: return
    val screenScope = rememberCoroutineScope()

    var credentials by remember { mutableStateOf<WebsiteCredentials?>(null) }
    var isPasswordShown by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(false) }

    val labelTextFieldState = rememberTextFieldState()
    val urlTextFieldState = rememberTextFieldState()
    val usernameTextFieldState = rememberTextFieldState()
    val passwordTextFieldState = rememberTextFieldState()
    val notesTextFieldState = rememberTextFieldState()

    LaunchedEffect(id) {
        val dao = AppDatabase.getDatabase(context).websiteCredentialsDao()
        val aead = TinkManager.getAead()

        val result = dao.getCredentialsById(id)
        credentials =
            result.copy(password = CryptoUtil.decrypt(aead, result.password, result.domain))

        labelTextFieldState.setTextAndPlaceCursorAtEnd(credentials!!.label)
        urlTextFieldState.setTextAndPlaceCursorAtEnd(credentials!!.domain)
        usernameTextFieldState.setTextAndPlaceCursorAtEnd(credentials!!.username)
        passwordTextFieldState.setTextAndPlaceCursorAtEnd(credentials!!.password)
        notesTextFieldState.setTextAndPlaceCursorAtEnd(credentials!!.notes)
    }

    fun saveWebsite(
        label: String,
        url: String,
        domain: String,
        username: String,
        password: String,
        notes: String,
    ) {

        val urlPattern = Regex(
            "^(https://|http://)?([a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,}(/.*)?$"
        )

        if (labelTextFieldState.text.isEmpty()) {
            Toast.makeText(context, "Website cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        if (usernameTextFieldState.text.isEmpty()) {
            Toast.makeText(context, "Username cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        if (passwordTextFieldState.text.isEmpty()) {
            Toast.makeText(context, "Password cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        if (urlTextFieldState.text.isEmpty()) {
            Toast.makeText(context, "URL cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        if (!urlTextFieldState.text.toString().matches(urlPattern)) {
            Toast.makeText(
                context,
                "Please enter a valid URL (e.g. xyz.com or https://xyz.com)",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        screenScope.launch {
            val aead = TinkManager.getAead()
            val encryptedPassword =
                CryptoUtil.encrypt(aead, password, domain)

            val credential = WebsiteCredentials(
                id = credentials!!.id,
                label = label,
                url = url,
                domain = domain,
                username = username,
                password = encryptedPassword,
                notes = notes
            )

            val dao = AppDatabase.getDatabase(context).websiteCredentialsDao()
            withContext(Dispatchers.IO) {
                dao.updateCredentials(credential)
            }

        }
    }

    fun deleteWebsite() {
        screenScope.launch {
            val dao = AppDatabase.getDatabase(context).websiteCredentialsDao()
            withContext(Dispatchers.IO) {
                dao.deleteCredentialsById(credentials?.id!!)
            }
        }

        navController.popBackStack()
    }

    credentials?.let {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    navController.popBackStack()
                }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
                Spacer(modifier = Modifier.weight(1f, fill = true))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        deleteWebsite()
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Delete, contentDescription = "Delete"
                        )
                    }


                    IconButton(onClick = {

                        if (isEditing) {
                            val label = labelTextFieldState.text
                            val domain = urlTextFieldState.text
                            val username = usernameTextFieldState.text
                            val password = passwordTextFieldState.text
                            val notes = notesTextFieldState.text.takeIf { it.isNotBlank() }

                            saveWebsite(
                                label = label.toString(),
                                url = "https://${domain}.com/",
                                domain = domain.toString(),
                                username = username.toString(),
                                password = password.toString(),
                                notes = notes.toString(),
                            )
                        }

                        isEditing = !isEditing
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Edit, contentDescription = "Edit"
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AsyncImage(
                    model = "https://www.google.com/s2/favicons?domain=${urlTextFieldState.text}&sz=128",
                    contentDescription = "${urlTextFieldState.text} favicon",
                    modifier = Modifier
                        .size(64.dp)
                )
                Column {
                    Text(
                        text = labelTextFieldState.text.toString(),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (isEditing) "Edit" else usernameTextFieldState.text.toString(),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TextField(
                    state = labelTextFieldState,
                    enabled = if (isEditing) true else false,
                    label = { Text("Website") },
                    textStyle = MaterialTheme.typography.labelLarge,
                    lineLimits = TextFieldLineLimits.SingleLine,
                    colors = TextFieldDefaults.colors(
                        cursorColor = MaterialTheme.colorScheme.primary,
                        disabledIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done,
                    ),
                )
                TextField(
                    state = urlTextFieldState,
                    enabled = if (isEditing) true else false,
                    label = { Text("URL") },
                    textStyle = MaterialTheme.typography.labelLarge,
                    lineLimits = TextFieldLineLimits.SingleLine,
                    colors = TextFieldDefaults.colors(
                        cursorColor = MaterialTheme.colorScheme.primary,
                        disabledIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done,
                    ),
                )
                TextField(
                    state = usernameTextFieldState,
                    enabled = if (isEditing) true else false,
                    label = { Text("Username") },
                    textStyle = MaterialTheme.typography.labelLarge,
                    lineLimits = TextFieldLineLimits.SingleLine,
                    colors = TextFieldDefaults.colors(
                        cursorColor = MaterialTheme.colorScheme.primary,
                        disabledIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done,
                    ),
                )

                SecureTextField(
                    state = passwordTextFieldState,
                    enabled = if (isEditing) true else false,
                    label = { Text("Password") },
                    textStyle = MaterialTheme.typography.labelLarge,
                    trailingIcon = {
                        IconButton(onClick = {
                            isPasswordShown = !isPasswordShown
                        }) {
                            Image(
                                painter = if (isPasswordShown) painterResource(R.drawable.visibility_off_24px) else painterResource(
                                    R.drawable.visibility_24px
                                ),
                                contentDescription = "Toggle password visibility"
                            )
                        }
                    },
                    textObfuscationMode = if (isPasswordShown) TextObfuscationMode.Visible else TextObfuscationMode.Hidden,
                    colors = TextFieldDefaults.colors(
                        cursorColor = MaterialTheme.colorScheme.primary,
                        disabledIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done,
                    ),
                )
//                if (notesTextFieldState.text.isNotEmpty()) {
//                    TextField(
//                        state = notesTextFieldState,
//                        enabled = if (isEditing) true else false,
//                        label = { Text("Notes") },
//                        textStyle = MaterialTheme.typography.labelLarge,
//                        colors = TextFieldDefaults.colors(
//                            cursorColor = MaterialTheme.colorScheme.primary,
//                            disabledIndicatorColor = Color.Transparent,
//                            focusedIndicatorColor = Color.Transparent,
//                            unfocusedIndicatorColor = Color.Transparent
//                        ),
//                        modifier = Modifier
//                            .fillMaxWidth(),
//                        keyboardOptions = KeyboardOptions(
//                            imeAction = ImeAction.Done,
//                        ),
//                    )
//                }
            }
            Spacer(modifier = Modifier.weight(1f, fill = true))
            if (isEditing) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    FilledTonalButton(onClick = {
                        isEditing = false
                        navController.popBackStack()
                    }, modifier = Modifier.weight(1f, fill = true)) {
                        Text("Cancel")
                    }
                    FilledTonalButton(onClick = {
                        val label = labelTextFieldState.text
                        val domain = urlTextFieldState.text
                        val username = usernameTextFieldState.text
                        val password = passwordTextFieldState.text
                        val notes = notesTextFieldState.text.takeIf { it.isNotBlank() }

                        saveWebsite(
                            label = label.toString(),
                            url = "https://${domain}.com/",
                            domain = domain.toString(),
                            username = username.toString(),
                            password = password.toString(),
                            notes = notes.toString(),
                        )
                    }, modifier = Modifier.weight(1f, fill = true)) {
                        Text("Save")
                    }
                }
            }
        }
    }
}