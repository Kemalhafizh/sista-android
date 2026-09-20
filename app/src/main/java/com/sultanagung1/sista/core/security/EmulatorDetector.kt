package com.sultanagung1.sista.core.security

import android.os.Build

object EmulatorDetector {

    fun isEmulator(): Boolean {
        val fingerPrint = Build.FINGERPRINT
        val model = Build.MODEL
        val manufacturer = Build.MANUFACTURER
        val brand = Build.BRAND
        val device = Build.DEVICE
        val product = Build.PRODUCT
        val hardware = Build.HARDWARE

        return fingerPrint.startsWith("generic")
                || fingerPrint.startsWith("unknown")
                || fingerPrint.contains("google_sdk")
                || fingerPrint.contains("Emulator")
                || fingerPrint.contains("Android SDK built for x86")
                || model.contains("google_sdk")
                || model.contains("Emulator")
                || model.contains("Android SDK built for x86")
                || model.contains("sdk_gphone")
                || manufacturer.contains("Genymotion")
                || manufacturer.contains("unknown")
                || brand.startsWith("generic") && device.startsWith("generic")
                || product.contains("google_sdk")
                || product.contains("sdk_gphone")
                || product.contains("vbox86p")
                || product.contains("nox")
                || product.contains("bluestacks")
                || hardware.contains("goldfish")
                || hardware.contains("ranchu")
                || hardware.contains("vbox86")
    }
}
