package com.sultanagung1.sista.core.security

import android.content.Context
import android.util.Base64
import java.io.File
import java.nio.charset.StandardCharsets
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class CbtEncryptedVault(private val context: Context) {

    /**
     * Store encrypted payload file locally for offline resilience.
     */
    fun saveEncryptedPayload(examId: Long, encryptedDataBase64: String, ivBase64: String) {
        val file = File(context.filesDir, "cbt_vault_$examId.enc")
        val content = "$ivBase64::$encryptedDataBase64"
        file.writeText(content, StandardCharsets.UTF_8)
    }

    /**
     * Check if encrypted payload exists in local storage.
     */
    fun hasEncryptedPayload(examId: Long): Boolean {
        val file = File(context.filesDir, "cbt_vault_$examId.enc")
        return file.exists() && file.length() > 0
    }

    /**
     * Decrypt exam questions JSON using the retrieved base64 key.
     */
    fun decryptExamPayload(examId: Long, keyBase64: String): String? {
        return try {
            val file = File(context.filesDir, "cbt_vault_$examId.enc")
            if (!file.exists()) return null

            val raw = file.readText(StandardCharsets.UTF_8)
            val parts = raw.split("::")
            if (parts.size != 2) return null

            val ivBytes = Base64.decode(parts[0], Base64.DEFAULT)
            val cipherBytes = Base64.decode(parts[1], Base64.DEFAULT)
            val keyBytes = Base64.decode(keyBase64, Base64.DEFAULT)

            val secretKey = SecretKeySpec(keyBytes, "AES")
            val ivSpec = IvParameterSpec(ivBytes)

            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec)

            val decryptedBytes = cipher.doFinal(cipherBytes)
            String(decryptedBytes, StandardCharsets.UTF_8)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Decrypt in-memory encrypted payload directly.
     */
    fun decryptDirect(encryptedDataBase64: String, ivBase64: String, keyBase64: String): String? {
        return try {
            val ivBytes = Base64.decode(ivBase64, Base64.DEFAULT)
            val cipherBytes = Base64.decode(encryptedDataBase64, Base64.DEFAULT)
            val keyBytes = Base64.decode(keyBase64, Base64.DEFAULT)

            val secretKey = SecretKeySpec(keyBytes, "AES")
            val ivSpec = IvParameterSpec(ivBytes)

            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec)

            val decryptedBytes = cipher.doFinal(cipherBytes)
            String(decryptedBytes, StandardCharsets.UTF_8)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Delete cached encrypted vault files after exam is completed.
     */
    fun clearVault(examId: Long) {
        try {
            val file = File(context.filesDir, "cbt_vault_$examId.enc")
            if (file.exists()) {
                file.delete()
            }
        } catch (_: Exception) {}
    }
}
