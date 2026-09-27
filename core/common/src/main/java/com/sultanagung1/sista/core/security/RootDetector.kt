package com.sultanagung1.sista.core.security

import android.content.Context
import android.os.Build
import java.io.File

object RootDetector {

    private val SU_PATHS = arrayOf(
        "/system/bin/su",
        "/system/xbin/su",
        "/sbin/su",
        "/system/sd/xbin/su",
        "/system/bin/failsafe/su",
        "/data/local/xbin/su",
        "/data/local/bin/su",
        "/data/local/su",
        "/system/app/Superuser.apk"
    )

    private val ROOT_PACKAGES = arrayOf(
        "com.noshufou.android.su",
        "com.noshufou.android.su.elite",
        "eu.chainfire.supersu",
        "com.koushikdutta.superuser",
        "com.thirdparty.superuser",
        "com.yellowes.su",
        "com.topjohnwu.magisk",
        "com.kingroot.kinguser"
    )

    fun isDeviceRooted(context: Context): Boolean {
        return checkBuildTags() || checkSuBinaryPresence() || checkSuCommandExecution() || checkRootPackages(context)
    }

    private fun checkBuildTags(): Boolean {
        val buildTags = Build.TAGS
        return buildTags != null && buildTags.contains("test-keys")
    }

    private fun checkSuBinaryPresence(): Boolean {
        for (path in SU_PATHS) {
            if (File(path).exists()) {
                return true
            }
        }
        return false
    }

    private fun checkSuCommandExecution(): Boolean {
        var process: Process? = null
        return try {
            process = Runtime.getRuntime().exec(arrayOf("/system/xbin/which", "su"))
            val reader = process.inputStream.bufferedReader()
            reader.readLine() != null
        } catch (e: Throwable) {
            false
        } finally {
            process?.destroy()
        }
    }

    private fun checkRootPackages(context: Context): Boolean {
        val pm = context.packageManager
        for (pkg in ROOT_PACKAGES) {
            try {
                pm.getPackageInfo(pkg, 0)
                return true
            } catch (e: Exception) {
                // Not installed
            }
        }
        return false
    }
}
