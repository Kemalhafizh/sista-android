package com.sultanagung1.sista.ui.scanner

import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.data.repository.LibraryRepository
import com.sultanagung1.sista.data.repository.ScannerRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

enum class ScanMode(val title: String, val description: String) {
    LIBRARY_BOOK("Buku Perpus", "Pindai Barcode ISBN Buku"),
    EVENT_TICKET("Tiket Acara", "Pindai Tiket Kajian / Seminar"),
    VISITOR_PASS("Buku Tamu", "Pindai QR Akses Tamu Yayasan")
}

data class ParsedScanResult(
    val title: String,
    val subtitle: String,
    val type: ScanMode,
    val rawPayload: String,
    val isValid: Boolean = true,
    val details: Map<String, String> = emptyMap()
)

/**
 * Every branch here calls a real backend check/mutation — there is no
 * client-side fabrication of "verified"/"valid" results. A scan failure
 * (wrong code, already used, network error) is surfaced honestly via
 * isValid = false with the server's real message, not papered over.
 */
class ScanResultHandler @Inject constructor(
    private val scannerRepository: ScannerRepository,
    private val libraryRepository: LibraryRepository
) {

    suspend fun parse(rawCode: String, mode: ScanMode): ParsedScanResult {
        return when (mode) {
            ScanMode.LIBRARY_BOOK -> parseLibraryBook(rawCode)
            ScanMode.EVENT_TICKET -> parseEventTicket(rawCode)
            ScanMode.VISITOR_PASS -> parseVisitorPass(rawCode)
        }
    }

    private suspend fun parseLibraryBook(rawCode: String): ParsedScanResult {
        val result = libraryRepository.borrowBookByQr(rawCode).first { it !is NetworkResult.Loading }
        return when (result) {
            is NetworkResult.Success -> {
                val loan = result.data
                ParsedScanResult(
                    title = "Peminjaman Buku Berhasil",
                    subtitle = loan.bookTitle,
                    type = ScanMode.LIBRARY_BOOK,
                    rawPayload = rawCode,
                    isValid = true,
                    details = mapOf(
                        "Tanggal Pinjam" to loan.borrowDate,
                        "Jatuh Tempo" to loan.dueDate,
                        "Status" to loan.status
                    )
                )
            }
            is NetworkResult.Error -> invalidResult(ScanMode.LIBRARY_BOOK, rawCode, result.message)
            is NetworkResult.Loading -> invalidResult(ScanMode.LIBRARY_BOOK, rawCode, "Memproses...")
        }
    }

    private suspend fun parseEventTicket(rawCode: String): ParsedScanResult {
        val result = scannerRepository.scanEventTicket(rawCode).first { it !is NetworkResult.Loading }
        return when (result) {
            is NetworkResult.Success -> {
                val data = result.data
                if (data.valid) {
                    ParsedScanResult(
                        title = "Tiket Acara Terverifikasi",
                        subtitle = data.eventName ?: "Acara",
                        type = ScanMode.EVENT_TICKET,
                        rawPayload = rawCode,
                        isValid = true,
                        details = mapOf(
                            "Nama Peserta" to (data.attendeeName ?: "-"),
                            "Jenis Tiket" to (data.ticketType ?: "-"),
                            "Lokasi" to (data.venue ?: "-")
                        )
                    )
                } else {
                    invalidResult(ScanMode.EVENT_TICKET, rawCode, data.message ?: "Tiket tidak valid.")
                }
            }
            is NetworkResult.Error -> invalidResult(ScanMode.EVENT_TICKET, rawCode, result.message)
            is NetworkResult.Loading -> invalidResult(ScanMode.EVENT_TICKET, rawCode, "Memproses...")
        }
    }

    private suspend fun parseVisitorPass(rawCode: String): ParsedScanResult {
        val result = scannerRepository.scanVisitorPass(rawCode).first { it !is NetworkResult.Loading }
        return when (result) {
            is NetworkResult.Success -> {
                val data = result.data
                if (data.valid) {
                    val actionLabel = if (data.status == "active") "Check-in Tamu Berhasil" else "Check-out Tamu Berhasil"
                    ParsedScanResult(
                        title = actionLabel,
                        subtitle = data.visitorName ?: "Tamu",
                        type = ScanMode.VISITOR_PASS,
                        rawPayload = rawCode,
                        isValid = true,
                        details = mapOf(
                            "Keperluan" to (data.purpose ?: "-"),
                            "Status" to (data.status ?: "-")
                        )
                    )
                } else {
                    invalidResult(ScanMode.VISITOR_PASS, rawCode, data.message ?: "Kartu tamu tidak valid.")
                }
            }
            is NetworkResult.Error -> invalidResult(ScanMode.VISITOR_PASS, rawCode, result.message)
            is NetworkResult.Loading -> invalidResult(ScanMode.VISITOR_PASS, rawCode, "Memproses...")
        }
    }

    private fun invalidResult(mode: ScanMode, rawCode: String, message: String): ParsedScanResult {
        return ParsedScanResult(
            title = "Kode Tidak Valid",
            subtitle = message,
            type = mode,
            rawPayload = rawCode,
            isValid = false,
            details = emptyMap()
        )
    }
}
