package me.mirimomekiku.safepass.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.view.autofill.AutofillManager

class AutofillReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "me.mirimomekiku.safepass.AUTOFILL_CREDENTIAL_SELECTED") {
            val fillInIntent =
                intent.getParcelableExtra<Intent>(AutofillManager.EXTRA_AUTHENTICATION_RESULT)

            val credentialIndex = fillInIntent?.getIntExtra("autofill_credential_index", -1) ?: -1

            if (credentialIndex != -1) {
                // The system now knows which Dataset to use based on the index (or ID)
                // Android's AutofillManager handles the rest automatically.
                // You don't need to do anything else here.
            }
        }
    }
}
