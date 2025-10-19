package me.mirimomekiku.safepass.helpers

import android.content.Context
import android.widget.Toast
import androidx.biometric.BiometricPrompt

class BiometricCallback(
    private val context: Context,
    private val onSuccess: (String) -> Unit
) : BiometricPrompt.AuthenticationCallback() {

    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
        super.onAuthenticationSucceeded(result)
        onSuccess(result.cryptoObject?.toString() ?: "")
    }

    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
        super.onAuthenticationError(errorCode, errString)
        Toast.makeText(context, errString, Toast.LENGTH_SHORT).show()
    }

    override fun onAuthenticationFailed() {
        super.onAuthenticationFailed()
        Toast.makeText(context, "Authentication failed", Toast.LENGTH_SHORT).show()
    }
}
