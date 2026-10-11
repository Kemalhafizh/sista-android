package com.sultanagung1.sista.data.repository

import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.data.api.CounselingMobileApiService
import com.sultanagung1.sista.data.model.BullyingReportItem
import com.sultanagung1.sista.data.model.BullyingReportRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

/** Sends a bullying report to the school's counsellors (BK). */
class BullyingReportRepository(
    private val apiService: CounselingMobileApiService,
    private val messages: FallbackMessages,
) {

    /**
     * Success carries the server's own confirmation in [Submitted.message]; a
     * refusal (a 422 on a future date, for one) carries the server's reason.
     */
    fun submit(request: BullyingReportRequest): Flow<NetworkResult<Submitted>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.reportBullying(request)
            val body = response.body()
            if (response.isSuccessful && body?.data != null) {
                emit(NetworkResult.Success(Submitted(body.data!!, body.message)))
            } else {
                emit(NetworkResult.Error(body?.message ?: messages.failure(response, R.string.bullying_report_failed), response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(messages.connection(e)))
        }
    }.flowOn(Dispatchers.IO)

    data class Submitted(val report: BullyingReportItem, val message: String?)
}
