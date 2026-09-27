package com.sultanagung1.sista.core.util

import com.sultanagung1.sista.core.security.EmulatorDetector

object Constants {
    // Backend API Base URL
    // Automatically uses 10.0.2.2 for Android Studio AVD or 127.0.0.1 for physical device with adb reverse
    val BASE_URL: String
        get() = if (EmulatorDetector.isEmulator()) {
            "http://10.0.2.2:8000/api/v1/"
        } else {
            "http://127.0.0.1:8000/api/v1/"
        }

    const val DEFAULT_BASE_URL = "http://127.0.0.1:8000/api/v1/"

    /** Server root without the /api/v1/ suffix — needed to resolve Storage::url()-style relative file paths (e.g. "uploads/rapor/x.pdf"). */
    val SERVER_ROOT_URL: String get() = BASE_URL.removeSuffix("api/v1/")

    /** Backend file fields are sometimes a full URL and sometimes a Laravel storage-relative path — normalize both to a fetchable URL. */
    fun resolveStorageUrl(path: String): String {
        return if (path.startsWith("http://") || path.startsWith("https://")) {
            path
        } else {
            SERVER_ROOT_URL.trimEnd('/') + "/storage/" + path.trimStart('/')
        }
    }

    // WebSocket / Echo Config
    val REVERB_HOST: String
        get() = if (EmulatorDetector.isEmulator()) "10.0.2.2" else "127.0.0.1"
    const val REVERB_PORT = 8080
    // Must match REVERB_APP_KEY in the backend's .env (sistem-terpadu/config/reverb.php).
    const val REVERB_APP_KEY = "local-key"

    // Roles
    const val ROLE_STUDENT = "student"
    const val ROLE_TEACHER = "guru"
    const val ROLE_PARENT = "parent"
    const val ROLE_ADMIN = "admin"
    const val ROLE_FOUNDATION = "yayasan"
}

