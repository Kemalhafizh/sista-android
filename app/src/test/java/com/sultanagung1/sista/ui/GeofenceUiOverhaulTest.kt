package com.sultanagung1.sista.ui

import com.sultanagung1.sista.ui.attendance.GeofenceStatus
import com.sultanagung1.sista.ui.attendance.LocationState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * GPS check-in: what the screen promises must match what the server accepts
 * (GeofenceAttendanceService), and the position must be taken now.
 */
class GeofenceUiOverhaulTest {

    private fun findSourceFile(relativePath: String): File {
        val candidates = listOf(File(relativePath), File("app/$relativePath"), File("../$relativePath"))
        return candidates.firstOrNull { it.exists() }
            ?: throw IllegalStateException("Cannot locate $relativePath in any candidate paths")
    }

    private val radius = 250.0
    private fun fix(accuracy: Float = 10f, mock: Boolean = false) = LocationState.Fixed(-6.99, 110.42, accuracy, mock)

    @Test
    fun `check-in is offered only inside the radius with a good fix and no mock location`() {
        assertTrue(GeofenceStatus.of(fix(), 42.0, radius).canCheckIn)
        assertEquals("Anda di area sekolah", GeofenceStatus.of(fix(), 42.0, radius).title)

        val outside = GeofenceStatus.of(fix(), 1234.0, radius)
        assertFalse(outside.canCheckIn)
        val body = outside.body.replace('\u00A0', ' ')
        assertTrue(body.contains("1,2 km"))
        assertTrue(body.contains("250 m"))

        assertFalse(GeofenceStatus.of(fix(mock = true), 10.0, radius).canCheckIn)
        // The server refuses accuracy worse than 80 m; the app says so first.
        assertFalse(GeofenceStatus.of(fix(accuracy = 95f), 10.0, radius).canCheckIn)
        assertTrue(GeofenceStatus.of(fix(accuracy = 80f), 10.0, radius).canCheckIn)
    }

    @Test
    fun `without a fix nothing can be sent`() {
        listOf(LocationState.NeedsPermission, LocationState.PermissionDenied, LocationState.Locating, LocationState.Unavailable)
            .forEach { assertFalse("$it", GeofenceStatus.of(it, null, radius).canCheckIn) }
    }

    @Test
    fun `the screen takes a fresh fix and uses the server's radius`() {
        val src = findSourceFile("feature/academic/src/main/java/com/sultanagung1/sista/ui/attendance/GeofenceAttendanceScreen.kt").readText()
        assertTrue("fresh fix", src.contains("getCurrentLocation(") && src.contains("setMaxUpdateAgeMillis(0)"))
        assertFalse("never the cached last location", src.contains("lastLocation"))
        assertTrue("radius from mobile/config", src.contains("GeoUtils.campus.radiusMeters"))
        assertFalse("no hardcoded radius text", src.contains("Radius Presensi 250m"))
        assertTrue("E2E hook stays", src.contains("\"geofence_checkin_button\""))
        assertFalse("design-system colours only", Regex("""Color\(0x""").containsMatchIn(src))
    }
}
