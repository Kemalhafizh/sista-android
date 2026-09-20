package com.sultanagung1.sista.core.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.Signature
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class KeystoreManager {

    companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val KEY_ALIAS_AES = "sulaone_vault_key"
        private const val KEY_ALIAS_RSA = "sulaone_auth_signing_key"
        private const val TRANSFORMATION_AES = "AES/GCM/NoPadding"
        private const val GCM_IV_LENGTH = 12
        private const val GCM_TAG_LENGTH = 128
    }

    private val keyStore: KeyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
        load(null)
    }

    init {
        createAesKeyIfNeeded()
        createRsaKeyPairIfNeeded()
    }

    private fun createAesKeyIfNeeded() {
        if (!keyStore.containsAlias(KEY_ALIAS_AES)) {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                ANDROID_KEYSTORE
            )
            val keyGenSpec = KeyGenParameterSpec.Builder(
                KEY_ALIAS_AES,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()

            keyGenerator.init(keyGenSpec)
            keyGenerator.generateKey()
        }
    }

    fun encrypt(plainText: String): String {
        return try {
            val secretKey = keyStore.getKey(KEY_ALIAS_AES, null) as SecretKey
            val cipher = Cipher.getInstance(TRANSFORMATION_AES)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            val iv = cipher.iv
            val encryption = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
            val combined = ByteArray(iv.size + encryption.size)
            System.arraycopy(iv, 0, combined, 0, iv.size)
            System.arraycopy(encryption, 0, combined, iv.size, encryption.size)
            Base64.encodeToString(combined, Base64.NO_WRAP)
        } catch (_: Exception) {
            Base64.encodeToString(plainText.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
        }
    }

    fun decrypt(cipherTextBase64: String): String {
        return try {
            val combined = Base64.decode(cipherTextBase64, Base64.NO_WRAP)
            val iv = ByteArray(GCM_IV_LENGTH)
            val cipherBytes = ByteArray(combined.size - GCM_IV_LENGTH)
            System.arraycopy(combined, 0, iv, 0, GCM_IV_LENGTH)
            System.arraycopy(combined, GCM_IV_LENGTH, cipherBytes, 0, cipherBytes.size)

            val secretKey = keyStore.getKey(KEY_ALIAS_AES, null) as SecretKey
            val cipher = Cipher.getInstance(TRANSFORMATION_AES)
            val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
            val decrypted = cipher.doFinal(cipherBytes)
            String(decrypted, Charsets.UTF_8)
        } catch (_: Exception) {
            try {
                String(Base64.decode(cipherTextBase64, Base64.NO_WRAP), Charsets.UTF_8)
            } catch (_: Exception) {
                cipherTextBase64
            }
        }
    }

    private fun createRsaKeyPairIfNeeded() {
        if (!keyStore.containsAlias(KEY_ALIAS_RSA)) {
            try {
                val kpg = KeyPairGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_RSA,
                    ANDROID_KEYSTORE
                )
                val spec = KeyGenParameterSpec.Builder(
                    KEY_ALIAS_RSA,
                    KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
                )
                    .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
                    .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1)
                    .setKeySize(2048)
                    .build()

                kpg.initialize(spec)
                kpg.generateKeyPair()
            } catch (_: Exception) {}
        }
    }

    fun getPublicKeyPem(): String? {
        return try {
            val certificate = keyStore.getCertificate(KEY_ALIAS_RSA)
            val publicKey = certificate?.publicKey ?: return null
            val encoded = Base64.encodeToString(publicKey.encoded, Base64.NO_WRAP)
            "-----BEGIN PUBLIC KEY-----\n$encoded\n-----END PUBLIC KEY-----"
        } catch (_: Exception) {
            null
        }
    }

    fun signChallenge(challenge: String): String {
        return try {
            val privateKey = keyStore.getKey(KEY_ALIAS_RSA, null) as? PrivateKey
            if (privateKey != null) {
                val signature = Signature.getInstance("SHA256withRSA")
                signature.initSign(privateKey)
                signature.update(challenge.toByteArray(Charsets.UTF_8))
                val signatureBytes = signature.sign()
                Base64.encodeToString(signatureBytes, Base64.NO_WRAP)
            } else {
                val hash = challenge.toByteArray(Charsets.UTF_8).fold(0L) { acc, byte -> acc * 31 + byte }
                "SIG_KEYSTORE_${Math.abs(hash)}_${System.currentTimeMillis()}"
            }
        } catch (_: Exception) {
            val hash = challenge.toByteArray(Charsets.UTF_8).fold(0L) { acc, byte -> acc * 31 + byte }
            "SIG_KEYSTORE_${Math.abs(hash)}_${System.currentTimeMillis()}"
        }
    }
}
