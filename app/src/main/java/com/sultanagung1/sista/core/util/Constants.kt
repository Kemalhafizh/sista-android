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
    
    // SMA Islam Sultan Agung 1 Semarang Geofence Coordinates
    const val CAMPUS_LATITUDE = -6.996160
    const val CAMPUS_LONGITUDE = 110.428510
    const val CAMPUS_RADIUS_METERS = 250.0

    // WebSocket / Echo Config
    val REVERB_HOST: String
        get() = if (EmulatorDetector.isEmulator()) "10.0.2.2" else "127.0.0.1"
    const val REVERB_PORT = 8080
    const val REVERB_APP_KEY = "sulaone_app_key"

    // Roles
    const val ROLE_STUDENT = "student"
    const val ROLE_TEACHER = "guru"
    const val ROLE_PARENT = "parent"
    const val ROLE_ADMIN = "admin"
    const val ROLE_FOUNDATION = "yayasan"
}

