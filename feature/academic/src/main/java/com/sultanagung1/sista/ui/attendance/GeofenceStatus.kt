package com.sultanagung1.sista.ui.attendance

import java.util.Locale

/** Where getting the phone's position stands on the GPS attendance screen. */
sealed interface LocationState {
    /** Location permission not asked yet (or asked and dismissed). */
    data object NeedsPermission : LocationState

    /** Permission refused: only the system settings can change it now. */
    data object PermissionDenied : LocationState

    /** Waiting for a fresh fix. */
    data object Locating : LocationState

    /** The phone gave no position (location off, no signal). */
    data object Unavailable : LocationState

    /** A fresh fix, taken now — never a cached position from earlier. */
    data class Fixed(
        val latitude: Double,
        val longitude: Double,
        val accuracyMeters: Float,
        val isMock: Boolean,
    ) : LocationState
}

/** What the screen says about the check-in and whether it may be sent. */
data class GeofenceStatus(
    val kind: Kind,
    val title: String,
    val body: String,
    val canCheckIn: Boolean,
) {
    enum class Kind { INFO, SUCCESS, WARNING, DANGER }

    companion object {
        /** Same limit the server enforces (GeofenceAttendanceService: accuracy > 80 m is refused). */
        const val MAX_ACCURACY_METERS = 80f

        fun of(location: LocationState, distanceMeters: Double?, radiusMeters: Double): GeofenceStatus = when (location) {
            LocationState.NeedsPermission -> GeofenceStatus(
                Kind.INFO,
                "Izinkan akses lokasi",
                "Presensi GPS memeriksa bahwa Anda berada di area sekolah. Lokasi hanya dikirim saat Anda menekan tombol presensi.",
                canCheckIn = false,
            )
            LocationState.PermissionDenied -> GeofenceStatus(
                Kind.WARNING,
                "Akses lokasi ditolak",
                "Aktifkan izin Lokasi untuk aplikasi ini di Pengaturan HP, lalu kembali ke sini.",
                canCheckIn = false,
            )
            LocationState.Locating -> GeofenceStatus(
                Kind.INFO,
                "Mencari lokasi Anda…",
                "Tunggu sebentar. Di dalam gedung, berdiri dekat jendela membantu.",
                canCheckIn = false,
            )
            LocationState.Unavailable -> GeofenceStatus(
                Kind.WARNING,
                "Lokasi belum didapat",
                "Pastikan Lokasi/GPS di HP menyala, lalu tekan Perbarui lokasi.",
                canCheckIn = false,
            )
            is LocationState.Fixed -> fixed(location, distanceMeters, radiusMeters)
        }

        private fun fixed(fix: LocationState.Fixed, distanceMeters: Double?, radiusMeters: Double): GeofenceStatus {
            if (fix.isMock) {
                return GeofenceStatus(
                    Kind.DANGER,
                    "Lokasi palsu terdeteksi",
                    "Matikan aplikasi pengubah lokasi (mock GPS), lalu perbarui lokasi.",
                    canCheckIn = false,
                )
            }
            if (fix.accuracyMeters > MAX_ACCURACY_METERS) {
                return GeofenceStatus(
                    Kind.WARNING,
                    "Sinyal lokasi lemah",
                    "Akurasi ±${formatDistance(fix.accuracyMeters.toDouble())}. Pindah ke tempat terbuka, lalu perbarui lokasi.",
                    canCheckIn = false,
                )
            }
            val distance = distanceMeters ?: return GeofenceStatus(Kind.INFO, "Mencari lokasi Anda…", "", canCheckIn = false)
            val detail = "Sekitar ${formatDistance(distance)} dari titik sekolah · akurasi ±${formatDistance(fix.accuracyMeters.toDouble())}"
            return if (distance <= radiusMeters) {
                GeofenceStatus(Kind.SUCCESS, "Anda di area sekolah", detail, canCheckIn = true)
            } else {
                GeofenceStatus(
                    Kind.WARNING,
                    "Anda di luar area sekolah",
                    "$detail. Presensi GPS hanya bisa dalam radius ${formatDistance(radiusMeters)}.",
                    canCheckIn = false,
                )
            }
        }

        /** 42.3 → "42 m"; 1234 → "1,2 km" (non-breaking space, so a number never wraps from its unit). */
        fun formatDistance(meters: Double): String =
            if (meters < 1000) "${Math.round(meters)}\u00A0m"
            else String.format(Locale.forLanguageTag("id-ID"), "%.1f\u00A0km", meters / 1000)
    }
}
