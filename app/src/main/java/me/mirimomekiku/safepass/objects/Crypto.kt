package me.mirimomekiku.safepass.objects

import android.util.Base64
import com.google.crypto.tink.Aead
import java.nio.charset.StandardCharsets

object CryptoUtil {
    fun encrypt(aead: Aead, plaintext: String, associatedData: String? = null): String {
        val ad = (associatedData ?: "").toByteArray(StandardCharsets.UTF_8)
        val ciphertext = aead.encrypt(plaintext.toByteArray(StandardCharsets.UTF_8), ad)
        return Base64.encodeToString(ciphertext, Base64.NO_WRAP)
    }

    fun decrypt(aead: Aead, ciphertextBase64: String, associatedData: String? = null): String {
        val ad = (associatedData ?: "").toByteArray(StandardCharsets.UTF_8)
        val decoded = Base64.decode(ciphertextBase64, Base64.NO_WRAP)
        val plain = aead.decrypt(decoded, ad)
        return String(plain, StandardCharsets.UTF_8)
    }
}
