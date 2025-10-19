package me.mirimomekiku.safepass.repository

import android.content.Context
import androidx.core.content.edit
import me.mirimomekiku.safepass.objects.CryptoUtil
import me.mirimomekiku.safepass.objects.TinkManager
import org.json.JSONObject

data class Credential(
    val packageName: String,
    val username: String,
    val password: String
)

class UserRepository(context: Context) {
    private val prefs = context.getSharedPreferences("secrets", Context.MODE_PRIVATE)
    private val aead = TinkManager.getAead()

    fun saveSecret(key: String, secretPlaintext: String) {
        val ciphertext = CryptoUtil.encrypt(aead, secretPlaintext, associatedData = key)
        prefs.edit { putString(key, ciphertext) }
    }

    fun getSecret(key: String): String? {
        val ciphertext = prefs.getString(key, null) ?: return null
        return CryptoUtil.decrypt(aead, ciphertext, associatedData = key)
    }

    fun removeSecret(key: String) {
        prefs.edit { remove(key) }
    }

    fun getCredentialsForPackage(packageName: String): Credential? {
        val stored = prefs.getString(packageName, null) ?: return null
        val json = JSONObject(stored)
        val username = json.getString("username")
        val encryptedPassword = json.getString("password")
        val decryptedPassword = CryptoUtil.decrypt(aead, encryptedPassword, packageName)

        return Credential(packageName, username, decryptedPassword)
    }
}
