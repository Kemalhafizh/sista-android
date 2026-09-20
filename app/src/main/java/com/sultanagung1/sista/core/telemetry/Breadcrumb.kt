package com.sultanagung1.sista.core.telemetry

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Kategori jejak telemetri (Breadcrumbs)
 */
enum class BreadcrumbCategory {
    NAVIGATION,
    NETWORK,
    LIFECYCLE,
    USER_ACTION,
    STATE,
    SECURITY,
    SYSTEM
}

/**
 * Tingkat urgensi / keparahan jejak
 */
enum class BreadcrumbLevel {
    DEBUG,
    INFO,
    WARN,
    ERROR
}

/**
 * Model data jejak aktivitas sebelum terjadinya crash atau event telemetri.
 */
data class Breadcrumb(
    val timestampMs: Long = System.currentTimeMillis(),
    val category: BreadcrumbCategory,
    val level: BreadcrumbLevel = BreadcrumbLevel.INFO,
    val message: String,
    val data: Map<String, String> = emptyMap()
) {
    fun getFormattedTime(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
        return sdf.format(Date(timestampMs))
    }

    override fun toString(): String {
        val dataStr = if (data.isNotEmpty()) " data=$data" else ""
        return "[${getFormattedTime()}][${level.name}][${category.name}] $message$dataStr"
    }
}
