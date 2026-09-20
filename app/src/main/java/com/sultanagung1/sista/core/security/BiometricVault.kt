package com.sultanagung1.sista.core.security

import android.content.Context
import androidx.biometric.BiometricManager
import com.sultanagung1.sista.core.storage.SessionManager
import kotlinx.coroutines.flow.first

import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

enum class BiometricAvailability(val description: String) {
    AVAILABLE("Sensor sidik jari / wajah siap digunakan"),
    NOT_ENROLLED("Sensor tersedia, namun belum didaftarkan di Pengaturan HP"),
    NO_HARDWARE("Perangkat tidak memiliki sensor biometrik"),
    SECURITY_UPDATE_REQUIRED("Pembaruan keamanan diperlukan")
}

class BiometricVault(
    private val context: Context,
    private val sessionManager: SessionManager,
    private val keystoreManager: KeystoreManager
) {

    companion object {
        fun checkStatus(context: Context): BiometricAvailability {
            val biometricManager = BiometricManager.from(context)
            return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK)) {
                BiometricManager.BIOMETRIC_SUCCESS -> BiometricAvailability.AVAILABLE
                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricAvailability.NOT_ENROLLED
                BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE,
                BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> BiometricAvailability.NO_HARDWARE
                BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> BiometricAvailability.SECURITY_UPDATE_REQUIRED
                else -> BiometricAvailability.NO_HARDWARE
            }
        }

        fun authenticate(
            activity: FragmentActivity,
            title: String = "Autentikasi Biometrik",
            subtitle: String = "Tempelkan sidik jari Anda pada sensor",
            negativeButtonText: String = "Batal",
            onSuccess: () -> Unit,
            onError: (String) -> Unit = {}
        ) {
            val executor = ContextCompat.getMainExecutor(activity)
            val biometricPrompt = androidx.biometric.BiometricPrompt(
                activity,
                executor,
                object : androidx.biometric.BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: androidx.biometric.BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        onSuccess()
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        if (errorCode != androidx.biometric.BiometricPrompt.ERROR_USER_CANCELED &&
                            errorCode != androidx.biometric.BiometricPrompt.ERROR_NEGATIVE_BUTTON
                        ) {
                            onError(errString.toString())
                        }
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                    }
                }
            )

            val promptInfo = androidx.biometric.BiometricPrompt.PromptInfo.Builder()
                .setTitle(title)
                .setSubtitle(subtitle)
                .setNegativeButtonText(negativeButtonText)
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK)
                .build()

            biometricPrompt.authenticate(promptInfo)
        }
    }

    fun isBiometricHardwareAvailable(): Boolean {
        return checkStatus(context) == BiometricAvailability.AVAILABLE
    }

    suspend fun secureStoreToken(token: String) {
        val encrypted = keystoreManager.encrypt(token)
        // Store encrypted token representation
    }

    suspend fun getDecryptedToken(): String? {
        val rawToken = sessionManager.authTokenFlow.first() ?: return null
        return rawToken
    }

    fun signAuthChallenge(challenge: String): String {
        return keystoreManager.signChallenge(challenge)
    }
}
