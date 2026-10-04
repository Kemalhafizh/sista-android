package com.sultanagung1.sista.data.repository

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.stream.MalformedJsonException
import com.sultanagung1.sista.core.network.ApiEnvelope
import com.sultanagung1.sista.data.api.ClassSessionApiService
import com.sultanagung1.sista.data.model.ActiveClassSessionDto
import com.sultanagung1.sista.data.model.AttendanceMarkRequest
import com.sultanagung1.sista.data.model.AttendanceReportDto
import com.sultanagung1.sista.data.model.BulkAttendanceRequest
import com.sultanagung1.sista.data.model.BulkAttendanceResultDto
import com.sultanagung1.sista.data.model.ClassSessionDto
import com.sultanagung1.sista.data.model.ClassSessionError
import com.sultanagung1.sista.data.model.ClassSessionErrorDataDto
import com.sultanagung1.sista.data.model.ClassSessionErrorKind
import com.sultanagung1.sista.data.model.ClassSessionPageDto
import com.sultanagung1.sista.data.model.ClassSessionQrDto
import com.sultanagung1.sista.data.model.ClassSessionResult
import com.sultanagung1.sista.data.model.ClassSessionRules
import com.sultanagung1.sista.data.model.ClassSessionStatus
import com.sultanagung1.sista.data.model.EndClassSessionRequest
import com.sultanagung1.sista.data.model.OverrideAttendanceRequest
import com.sultanagung1.sista.data.model.ScanClassQrRequest
import com.sultanagung1.sista.data.model.ScanQrResultDto
import com.sultanagung1.sista.data.model.SessionAttendanceDto
import com.sultanagung1.sista.data.model.SessionAttendanceStatus
import com.sultanagung1.sista.data.model.StartClassSessionRequest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.io.IOException

/**
 * FASE 77.1: every class-session call, returning [ClassSessionResult] instead
 * of the older `Flow<NetworkResult>` so a screen can tell *why* a call failed:
 * the server's own message, the business rule behind it
 * ([ClassSessionRules.rejectionOf]), and whether the route exists at all
 * ([ClassSessionErrorKind.NOT_DEPLOYED] on a server without FASE 117). The
 * older repositories read only `response.body()`, which is null on any
 * non-2xx, so the server's own message never reached the user.
 */
class ClassSessionRepository(
    private val api: ClassSessionApiService,
    private val gson: Gson = Gson()
) {

    /**
     * Sessions seen in the admin list, so the correction screen (which only
     * gets a session id) can show which class it is without another endpoint.
     */
    private val adminSessionCache = java.util.concurrent.ConcurrentHashMap<Long, ClassSessionDto>()

    fun cachedAdminSession(sessionId: Long): ClassSessionDto? = adminSessionCache[sessionId]

    // ── Guru ────────────────────────────────────────────────────────────

    suspend fun getTodaySessions(): ClassSessionResult<List<ClassSessionDto>> =
        call(emptyOnNull = { emptyList() }) { api.getTodaySessions() }

    suspend fun startSession(scheduleId: Long, topic: String?): ClassSessionResult<ClassSessionDto> =
        call { api.startSession(scheduleId, StartClassSessionRequest(topic?.trim()?.ifBlank { null })) }

    suspend fun endSession(sessionId: Long, notes: String?, topic: String?): ClassSessionResult<ClassSessionDto> =
        call {
            api.endSession(
                sessionId,
                EndClassSessionRequest(
                    notes = notes?.trim()?.ifBlank { null },
                    topic = topic?.trim()?.ifBlank { null }
                )
            )
        }

    suspend fun getActiveQr(sessionId: Long): ClassSessionResult<ClassSessionQrDto> =
        call { api.getActiveQr(sessionId) }

    suspend fun getSessionStudents(sessionId: Long): ClassSessionResult<List<SessionAttendanceDto>> =
        call(emptyOnNull = { emptyList() }) { api.getSessionStudents(sessionId) }

    suspend fun markAttendance(
        sessionId: Long,
        changes: Map<Long, SessionAttendanceStatus>
    ): ClassSessionResult<BulkAttendanceResultDto> =
        call {
            api.bulkMarkAttendance(
                sessionId,
                BulkAttendanceRequest(changes.map { (studentId, status) -> AttendanceMarkRequest(studentId, status.wireValue) })
            )
        }

    // ── Siswa ───────────────────────────────────────────────────────────

    /** Success(null) means no class of this student is running right now. */
    suspend fun getActiveSessionForStudent(): ClassSessionResult<ActiveClassSessionDto?> =
        call(emptyOnNull = { null }) { api.getActiveSessionForStudent() }

    suspend fun scanQr(qrToken: String): ClassSessionResult<ScanQrResultDto> =
        call { api.scanQr(ScanClassQrRequest(qrToken.trim())) }

    // ── Admin / Waka Kurikulum / TU ─────────────────────────────────────

    suspend fun getAdminSessions(date: String?, status: ClassSessionStatus?, page: Int): ClassSessionResult<ClassSessionPageDto> =
        call { api.getAdminSessions(date, status?.let(::wireValueOf), page) }.also { result ->
            if (result is ClassSessionResult.Success) {
                result.data.items.orEmpty().forEach { s -> s.sessionId?.let { adminSessionCache[it] = s } }
            }
        }

    suspend fun getSessionAttendances(sessionId: Long): ClassSessionResult<List<SessionAttendanceDto>> =
        call(emptyOnNull = { emptyList() }) { api.getSessionAttendances(sessionId) }

    suspend fun overrideAttendance(
        attendanceId: Long,
        status: SessionAttendanceStatus,
        reason: String
    ): ClassSessionResult<SessionAttendanceDto> =
        call { api.overrideAttendance(attendanceId, OverrideAttendanceRequest(status.wireValue, reason.trim())) }

    suspend fun getAttendanceReport(startDate: String, endDate: String, groupBy: String): ClassSessionResult<AttendanceReportDto> =
        call { api.getAttendanceReport(startDate, endDate, groupBy) }

    // ── Plumbing ────────────────────────────────────────────────────────

    /**
     * @param emptyOnNull value for a successful response whose `data` is null
     *   (an empty list, or "no active session"). Without it, a null payload is
     *   an error: the caller expected an object.
     */
    private suspend fun <T> call(
        emptyOnNull: (() -> T)? = null,
        request: suspend () -> Response<ApiEnvelope<T>>
    ): ClassSessionResult<T> = withContext(Dispatchers.IO) {
        try {
            val response = request()
            val body = response.body()
            if (response.isSuccessful && body != null && body.success) {
                val data = body.data
                when {
                    data != null -> ClassSessionResult.Success(data)
                    emptyOnNull != null -> ClassSessionResult.Success(emptyOnNull())
                    else -> ClassSessionResult.Failure(
                        ClassSessionError(ClassSessionErrorKind.SERVER, "Respons server kosong.", response.code())
                    )
                }
            } else if (response.isSuccessful) {
                // 2xx with success=false: the server refused without an HTTP error.
                ClassSessionResult.Failure(
                    ClassSessionError(ClassSessionErrorKind.UNKNOWN, body?.message.orEmpty(), response.code())
                )
            } else {
                ClassSessionResult.Failure(parseError(response.code(), response.errorBody()?.string()))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: MalformedJsonException) {
            // An IOException subtype, but the server answered: it is not a connection problem.
            ClassSessionResult.Failure(
                ClassSessionError(ClassSessionErrorKind.UNKNOWN, "Respons server tidak bisa dibaca.")
            )
        } catch (e: IOException) {
            ClassSessionResult.Failure(ClassSessionError(ClassSessionErrorKind.NETWORK, e.localizedMessage.orEmpty()))
        } catch (e: RuntimeException) {
            // Malformed JSON from a proxy/captive portal, etc.
            ClassSessionResult.Failure(
                ClassSessionError(ClassSessionErrorKind.UNKNOWN, "Respons server tidak bisa dibaca.")
            )
        }
    }

    private fun parseError(httpCode: Int, rawBody: String?): ClassSessionError {
        val json: JsonObject? = rawBody
            ?.takeIf { it.isNotBlank() }
            ?.let { runCatching { JsonParser.parseString(it) }.getOrNull() }
            ?.takeIf { it.isJsonObject }
            ?.asJsonObject
        val message = json?.get("message")?.takeIf { it.isJsonPrimitive }?.asString.orEmpty()
        val data = json?.get("data")
            ?.takeIf { it.isJsonObject }
            ?.let { runCatching { gson.fromJson(it, ClassSessionErrorDataDto::class.java) }.getOrNull() }
        // Every FASE 117 controller answers inside the { success, message, data }
        // envelope. A 404 without it is Laravel's "route not found": this server
        // has no class-session routes.
        val inEnvelope = json?.has("success") == true

        val kind = when (httpCode) {
            401 -> ClassSessionErrorKind.UNAUTHORIZED
            403 -> ClassSessionErrorKind.FORBIDDEN
            404 -> if (inEnvelope) ClassSessionErrorKind.NOT_FOUND else ClassSessionErrorKind.NOT_DEPLOYED
            409 -> ClassSessionErrorKind.CONFLICT
            422 -> ClassSessionErrorKind.VALIDATION
            429 -> ClassSessionErrorKind.RATE_LIMITED
            in 500..599 -> ClassSessionErrorKind.SERVER
            else -> ClassSessionErrorKind.UNKNOWN
        }
        return ClassSessionError(
            kind = kind,
            message = message,
            httpCode = httpCode,
            rejection = ClassSessionRules.rejectionOf(json?.get("error_code")?.takeIf { it.isJsonPrimitive }?.asString),
            checkedInAt = data?.checkedInAt,
            existingSessionId = data?.sessionId
        )
    }

    private fun wireValueOf(status: ClassSessionStatus): String = when (status) {
        ClassSessionStatus.SCHEDULED -> "scheduled"
        ClassSessionStatus.ACTIVE -> "active"
        ClassSessionStatus.COMPLETED -> "completed"
        ClassSessionStatus.CANCELLED -> "cancelled"
        ClassSessionStatus.AUTO_CLOSED -> "auto_closed"
    }
}
