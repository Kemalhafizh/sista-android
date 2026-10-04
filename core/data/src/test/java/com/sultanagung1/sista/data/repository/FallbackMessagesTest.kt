package com.sultanagung1.sista.data.repository

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import retrofit2.Response
import java.net.ConnectException
import java.net.SocketTimeoutException

/**
 * What a repository says when the server explained nothing. The server's own
 * message always wins (it already speaks the app's language); otherwise the
 * app's own text in that language, never an exception's raw text or a hint
 * meant for developers.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "en")
class FallbackMessagesTest {

    private val messages = FallbackMessages(RuntimeEnvironment.getApplication())

    private fun error(code: Int, body: String) =
        Response.error<Any>(code, body.toResponseBody("application/json".toMediaType()))

    @Test
    fun theServersOwnMessageWins() {
        assertEquals(
            "These credentials do not match our records.",
            messages.failure(error(401, """{"message":"These credentials do not match our records."}"""), R.string.login_failed),
        )
    }

    @Test
    fun withoutAServerMessageTheFallbackNamesTheStatusCode() {
        assertEquals("Couldn't sign in (code 502).", messages.failure(error(502, "<html>Bad gateway</html>"), R.string.login_failed))
        assertEquals("The timetable couldn't be loaded (code 500).", messages.failure(error(500, """{"message":""}"""), R.string.schedule_load_failed))
    }

    @Test
    fun noAnswerAtAllSaysWhyInPlainWords() {
        val offline = messages.connection(ConnectException("Failed to connect to /10.0.2.2:8000"))
        assertEquals("Can't reach the server. Check your internet connection.", offline)
        assertFalse(offline.contains("10.0.2.2"))
        assertEquals("The server took too long to answer. Try again shortly.", messages.connection(SocketTimeoutException("timeout")))
    }

    @Test
    @Config(qualifiers = "ar")
    fun arabic() {
        assertEquals("تعذّر الاتصال بالخادم. تحقّق من اتصالك بالإنترنت.", messages.connection(ConnectException()))
    }

    @Test
    @Config(qualifiers = "in")
    fun indonesian() {
        assertEquals("Belum bisa masuk (kode 401).", messages.failure(error(401, ""), R.string.login_failed))
    }
}
