package com.sultanagung1.sista.core.telemetry

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Model rekaman crash & diagnostik
 */
data class CrashReport(
    val reportId: String = UUID.randomUUID().toString(),
    val timestampMs: Long = System.currentTimeMillis(),
    val exceptionType: String,
    val exceptionMessage: String,
    val stackTrace: String,
    val isFatal: Boolean = true,
    val deviceInfo: Map<String, String> = emptyMap(),
    val appInfo: Map<String, String> = emptyMap(),
    val userContext: Map<String, String> = emptyMap(),
    val breadcrumbs: List<String> = emptyList(),
    var isSent: Boolean = false
) {
    fun getFormattedDate(): String {
        val sdf = SimpleDateFormat("dd MMM yyyy HH:mm:ss", Locale.getDefault())
        return sdf.format(Date(timestampMs))
    }

    fun toJsonString(): String {
        return gson.toJson(this)
    }

    companion object {
        private val gson: Gson by lazy {
            GsonBuilder().setPrettyPrinting().create()
        }

        fun fromJsonString(json: String): CrashReport {
            return gson.fromJson(json, CrashReport::class.java)
        }
    }
}

/**
 * Repositori lokal untuk manajemen berkas Crash Report di penyimpanan internal aplikasi.
 */
class CrashReportRepository(private val context: Context) {

    private val tag = "CrashReportRepository"
    private val reportsDir: File
        get() {
            val dir = File(context.filesDir, "crash_reports")
            if (!dir.exists()) {
                dir.mkdirs()
            }
            return dir
        }

    fun saveCrashReport(report: CrashReport): Boolean {
        return try {
            val file = File(reportsDir, "crash_${report.reportId}.json")
            file.writeText(report.toJsonString())
            Log.d(tag, "💾 Crash report saved to: ${file.absolutePath}")
            true
        } catch (e: Throwable) {
            Log.e(tag, "Failed to save crash report: ${e.message}")
            false
        }
    }

    fun getAllCrashReports(): List<CrashReport> {
        val list = mutableListOf<CrashReport>()
        try {
            val files = reportsDir.listFiles { f -> f.extension == "json" } ?: emptyArray()
            for (file in files.sortedByDescending { it.lastModified() }) {
                try {
                    list.add(CrashReport.fromJsonString(file.readText()))
                } catch (e: Exception) {
                    Log.w(tag, "Corrupted report file ${file.name}: ${e.message}")
                }
            }
        } catch (e: Throwable) {
            Log.e(tag, "Failed to read crash reports: ${e.message}")
        }
        return list
    }

    fun getLatestCrashReport(): CrashReport? {
        return getAllCrashReports().firstOrNull()
    }

    fun getPendingReportCount(): Int {
        return getAllCrashReports().count { !it.isSent }
    }

    fun markReportSent(reportId: String) {
        try {
            val file = File(reportsDir, "crash_${reportId}.json")
            if (file.exists()) {
                val report = CrashReport.fromJsonString(file.readText())
                report.isSent = true
                file.writeText(report.toJsonString())
            }
        } catch (e: Throwable) {
            Log.w(tag, "Failed to mark report as sent: ${e.message}")
        }
    }

    fun clearAllReports() {
        try {
            val files = reportsDir.listFiles() ?: emptyArray()
            files.forEach { it.delete() }
        } catch (e: Throwable) {
            Log.w(tag, "Failed to clear reports: ${e.message}")
        }
    }

    fun deleteOlderThan(days: Int = 14) {
        try {
            val cutoff = System.currentTimeMillis() - (days * 24L * 60 * 60 * 1000)
            val files = reportsDir.listFiles { f -> f.lastModified() < cutoff } ?: emptyArray()
            files.forEach { it.delete() }
        } catch (e: Throwable) {
            Log.w(tag, "Failed to clean old reports: ${e.message}")
        }
    }

    /**
     * Menghasilkan teks ringkasan ramah IT yang disanitasi untuk dibagikan (WhatsApp / Email).
     */
    fun exportReportSummary(report: CrashReport): String {
        val sb = StringBuilder()
        sb.append("📋 *LAPORAN DIAGNOSTIK KENDALA APLIKASI SISTA*\n")
        sb.append("------------------------------------------\n")
        sb.append("🆔 ID Tiket: ${report.reportId.take(8)}\n")
        sb.append("⏰ Waktu: ${report.getFormattedDate()}\n")
        sb.append("⚠️ Tipe Kendala: ${report.exceptionType}\n")
        sb.append("💬 Pesan: ${report.exceptionMessage}\n\n")

        sb.append("📱 *Spesifikasi Perangkat:*\n")
        report.deviceInfo.forEach { (k, v) ->
            sb.append("• $k: $v\n")
        }
        sb.append("\n")

        sb.append("👤 *Konteks Pengguna:*\n")
        report.userContext.forEach { (k, v) ->
            sb.append("• $k: $v\n")
        }
        sb.append("\n")

        sb.append("🍞 *Jejak Aktivitas Terakhir (Breadcrumbs):*\n")
        report.breadcrumbs.takeLast(10).forEach { bc ->
            sb.append("• $bc\n")
        }
        sb.append("\n")

        sb.append("🛠️ *Stack Trace Singkat:*\n")
        val shortTrace = report.stackTrace.lines().take(8).joinToString("\n")
        sb.append("```\n$shortTrace\n```\n")
        sb.append("------------------------------------------\n")
        sb.append("Dikirim dari SISTA Super App Enterprise")
        return sb.toString()
    }
}
