package com.sultanagung1.sista.core.document

import android.app.DownloadManager.ACTION_DOWNLOAD_COMPLETE
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.os.Build
import androidx.core.content.ContextCompat
import com.sultanagung1.sista.data.model.DownloadTaskItem
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.app.DownloadManager as SystemDownloadManager

/**
 * Wraps Android's own DownloadManager system service — the standard,
 * dependency-free way to download a file reliably in the background with
 * real progress and a persisted history, instead of faking a progress bar
 * over a local timer with no actual network transfer.
 */
class DownloadManager(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val systemDownloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as SystemDownloadManager

    private val _downloads = MutableStateFlow<List<DownloadTaskItem>>(emptyList())
    val downloads: StateFlow<List<DownloadTaskItem>> = _downloads.asStateFlow()

    // system download id -> our task id, so the completion receiver can find the right row.
    private val activeSystemIds = mutableMapOf<Long, String>()

    private val completionReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context, intent: Intent) {
            val systemId = intent.getLongExtra(SystemDownloadManager.EXTRA_DOWNLOAD_ID, -1L)
            if (systemId != -1L) refreshTask(systemId)
        }
    }

    init {
        val filter = IntentFilter(ACTION_DOWNLOAD_COMPLETE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.registerReceiver(context, completionReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            context.registerReceiver(completionReceiver, filter)
        }
        scope.launch { refreshAllFromSystem() }
    }

    fun startDownload(title: String, fileType: String, sizeBytes: Long, url: String) {
        val safeFileName = title.replace(Regex("[^A-Za-z0-9._-]"), "_").ifBlank { "dokumen_sulaone.pdf" }
        val request = SystemDownloadManager.Request(android.net.Uri.parse(url))
            .setTitle(title)
            .setDescription("Sulaone — SMA Islam Sultan Agung 1 Semarang")
            .setNotificationVisibility(SystemDownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(android.os.Environment.DIRECTORY_DOWNLOADS, safeFileName)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        val systemId = try {
            systemDownloadManager.enqueue(request)
        } catch (e: Exception) {
            val failedTask = DownloadTaskItem(
                id = "failed_${System.currentTimeMillis()}",
                title = title,
                fileType = fileType,
                sizeBytes = sizeBytes,
                isFailed = true,
                downloadedAt = "Gagal memulai unduhan: ${e.localizedMessage}"
            )
            _downloads.value = listOf(failedTask) + _downloads.value
            return
        }

        activeSystemIds[systemId] = systemId.toString()
        _downloads.value = listOf(
            DownloadTaskItem(
                id = systemId.toString(),
                title = title,
                fileType = fileType,
                sizeBytes = sizeBytes,
                progress = 0f,
                isCompleted = false
            )
        ) + _downloads.value

        scope.launch { pollProgress(systemId) }
    }

    private suspend fun pollProgress(systemId: Long) {
        while (true) {
            val (status, bytesDownloaded, bytesTotal) = queryStatus(systemId) ?: return
            val progress = if (bytesTotal > 0) bytesDownloaded.toFloat() / bytesTotal else 0f
            _downloads.value = _downloads.value.map { item ->
                if (item.id == systemId.toString()) item.copy(progress = progress) else item
            }
            if (status == SystemDownloadManager.STATUS_SUCCESSFUL || status == SystemDownloadManager.STATUS_FAILED) {
                refreshTask(systemId)
                return
            }
            delay(400)
        }
    }

    private fun queryStatus(systemId: Long): Triple<Int, Long, Long>? {
        val query = SystemDownloadManager.Query().setFilterById(systemId)
        systemDownloadManager.query(query)?.use { cursor: Cursor ->
            if (cursor.moveToFirst()) {
                val status = cursor.getInt(cursor.getColumnIndexOrThrow(SystemDownloadManager.COLUMN_STATUS))
                val bytesDownloaded = cursor.getLong(cursor.getColumnIndexOrThrow(SystemDownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                val bytesTotal = cursor.getLong(cursor.getColumnIndexOrThrow(SystemDownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                return Triple(status, bytesDownloaded, bytesTotal)
            }
        }
        return null
    }

    private fun refreshTask(systemId: Long) {
        val query = SystemDownloadManager.Query().setFilterById(systemId)
        systemDownloadManager.query(query)?.use { cursor: Cursor ->
            if (cursor.moveToFirst()) {
                val item = cursorToTask(cursor)
                _downloads.value = listOf(item) + _downloads.value.filterNot { it.id == item.id }
            }
        }
    }

    private fun refreshAllFromSystem() {
        val query = SystemDownloadManager.Query()
        systemDownloadManager.query(query)?.use { cursor: Cursor ->
            val items = mutableListOf<DownloadTaskItem>()
            while (cursor.moveToNext()) {
                items.add(cursorToTask(cursor))
            }
            _downloads.value = items.sortedByDescending { it.id.toLongOrNull() ?: 0L }
        }
    }

    private fun cursorToTask(cursor: Cursor): DownloadTaskItem {
        val id = cursor.getLong(cursor.getColumnIndexOrThrow(SystemDownloadManager.COLUMN_ID))
        val title = cursor.getString(cursor.getColumnIndexOrThrow(SystemDownloadManager.COLUMN_TITLE)) ?: "Berkas"
        val status = cursor.getInt(cursor.getColumnIndexOrThrow(SystemDownloadManager.COLUMN_STATUS))
        val bytesTotal = cursor.getLong(cursor.getColumnIndexOrThrow(SystemDownloadManager.COLUMN_TOTAL_SIZE_BYTES))
        val bytesDownloaded = cursor.getLong(cursor.getColumnIndexOrThrow(SystemDownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
        val localUri = cursor.getString(cursor.getColumnIndexOrThrow(SystemDownloadManager.COLUMN_LOCAL_URI))
        val lastModified = cursor.getLong(cursor.getColumnIndexOrThrow(SystemDownloadManager.COLUMN_LAST_MODIFIED_TIMESTAMP))

        return DownloadTaskItem(
            id = id.toString(),
            title = title,
            fileType = title.substringAfterLast('.', "PDF").uppercase(),
            sizeBytes = bytesTotal.coerceAtLeast(0),
            progress = if (bytesTotal > 0) bytesDownloaded.toFloat() / bytesTotal else 0f,
            isCompleted = status == SystemDownloadManager.STATUS_SUCCESSFUL,
            isFailed = status == SystemDownloadManager.STATUS_FAILED,
            localFilePath = localUri?.let { android.net.Uri.parse(it).path },
            downloadedAt = if (lastModified > 0) SimpleDateFormat("d MMM yyyy, HH:mm", Locale("id", "ID")).format(Date(lastModified)) else "—"
        )
    }

    fun removeDownload(taskId: String) {
        taskId.toLongOrNull()?.let { systemDownloadManager.remove(it) }
        _downloads.value = _downloads.value.filterNot { it.id == taskId }
    }
}
