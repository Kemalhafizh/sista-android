package com.sultanagung1.sista.data.repository

import android.content.Context
import androidx.annotation.StringRes
import com.sultanagung1.sista.core.accessibility.AppLocale
import retrofit2.Response
import java.net.SocketTimeoutException

/**
 * What a repository says when the server explained nothing: the phone is
 * offline, the server timed out, or an error came without a `message`. In the
 * app's language, like the server's own messages (Accept-Language), and never
 * an exception's raw text.
 */
class FallbackMessages(private val context: Context) {

    fun get(@StringRes id: Int, vararg args: Any): String = AppLocale.wrap(context).getString(id, *args)

    /** The request never got an answer. */
    fun connection(e: Exception): String =
        get(if (e is SocketTimeoutException) R.string.error_timeout else R.string.error_offline)

    /** The server's own message for a failed [response], else [fallback] with the status code. */
    fun failure(response: Response<*>, @StringRes fallback: Int): String =
        serverMessageOf(response.errorBody()?.string()) ?: get(fallback, response.code())
}
