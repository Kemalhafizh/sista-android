package com.sultanagung1.sista.core.document

import android.content.Context
import com.sultanagung1.sista.data.model.DownloadTaskItem
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

class DownloadManager(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _downloads = MutableStateFlow<List<DownloadTaskItem>>(
        listOf(
            DownloadTaskItem(
                id = "dl_1",
                title = "Rapor_Semester_Ganjil_XII_MIPA1.pdf",
                fileType = "PDF",
                sizeBytes = 2_450_000,
                progress = 1.0f,
                isCompleted = true,
                localFilePath = "/storage/emulated/0/Download/Rapor_Semester_Ganjil.pdf",
                downloadedAt = "Hari ini, 09:15 WIB"
            ),
            DownloadTaskItem(
                id = "dl_2",
                title = "Modul_Ajar_Fisika_Fase_F_Kurikulum_Merdeka.pdf",
                fileType = "PDF",
                sizeBytes = 8_700_000,
                progress = 1.0f,
                isCompleted = true,
                localFilePath = "/storage/emulated/0/Download/Modul_Ajar_Fisika.pdf",
                downloadedAt = "Kemarin"
            ),
            DownloadTaskItem(
                id = "dl_3",
                title = "Bukti_Pembayaran_SPP_Agustus_2026.pdf",
                fileType = "PDF",
                sizeBytes = 640_000,
                progress = 1.0f,
                isCompleted = true,
                localFilePath = "/storage/emulated/0/Download/Kuitansi_SPP.pdf",
                downloadedAt = "23 Agustus 2026"
            )
        )
    )
    val downloads: StateFlow<List<DownloadTaskItem>> = _downloads.asStateFlow()

    fun startDownload(title: String, fileType: String, sizeBytes: Long, url: String) {
        val taskId = "dl_" + System.currentTimeMillis()
        val newTask = DownloadTaskItem(
            id = taskId,
            title = title,
            fileType = fileType,
            sizeBytes = sizeBytes,
            progress = 0.05f,
            isCompleted = false
        )

        _downloads.value = listOf(newTask) + _downloads.value

        scope.launch {
            for (step in 1..10) {
                delay(300)
                val newProgress = step / 10f
                _downloads.value = _downloads.value.map { item ->
                    if (item.id == taskId) {
                        item.copy(
                            progress = newProgress,
                            isCompleted = (step == 10),
                            localFilePath = if (step == 10) File(context.filesDir, title).absolutePath else null
                        )
                    } else item
                }
            }
        }
    }

    fun removeDownload(taskId: String) {
        _downloads.value = _downloads.value.filterNot { it.id == taskId }
    }
}
