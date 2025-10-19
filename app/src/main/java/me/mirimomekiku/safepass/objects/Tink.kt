package me.mirimomekiku.safepass.objects

import android.content.Context
import androidx.core.content.edit
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.integration.android.AndroidKeysetManager

object TinkManager {
    private const val PREF_FILE = "tink_keyset_prefs"
    private const val PREF_KEYSET_NAME = "tink_keyset"
    private const val MASTER_KEY_URI = "android-keystore://tink_master_key_v1"

    private var aead: Aead? = null

    fun init(context: Context) {
        AeadConfig.register()

        try {
            val manager = AndroidKeysetManager.Builder()
                .withSharedPref(context, PREF_KEYSET_NAME, PREF_FILE)
                .withKeyTemplate(KeyTemplates.get("AES256_GCM"))
                .withMasterKeyUri(MASTER_KEY_URI)
                .build()

            aead = manager.keysetHandle.getPrimitive(Aead::class.java)
        } catch (e: Exception) {
            context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE).edit { clear() }
            val manager = AndroidKeysetManager.Builder()
                .withSharedPref(context, PREF_KEYSET_NAME, PREF_FILE)
                .withKeyTemplate(KeyTemplates.get("AES256_GCM"))
                .withMasterKeyUri(MASTER_KEY_URI)
                .build()

            aead = manager.keysetHandle.getPrimitive(Aead::class.java)
        }


    }

    fun getAead(): Aead {
        return aead
            ?: throw IllegalStateException("TinkManager not initialized. Call TinkManager.init(context) first.")
    }
}
