package com.sultanagung1.sista.core.util

import kotlin.math.*

/** Centre and radius of the school's attendance geofence. */
data class CampusGeofence(
    val latitude: Double,
    val longitude: Double,
    val radiusMeters: Double
)

object GeoUtils {

    // Fallback until the server's value arrives (GET mobile/config → "campus").
    // The server (App\Support\School::campus()) is the single source every
    // check-in path uses; an admin can move it in Profil Sekolah, so the app
    // must not keep checking against a point of its own.
    const val CAMPUS_LATITUDE = -6.996160
    const val CAMPUS_LONGITUDE = 110.428510
    const val CAMPUS_RADIUS_METERS = 250.0

    private val defaultCampus = CampusGeofence(CAMPUS_LATITUDE, CAMPUS_LONGITUDE, CAMPUS_RADIUS_METERS)

    @Volatile
    var campus: CampusGeofence = defaultCampus
        private set

    /**
     * Adopts the server's geofence. Out-of-range values are ignored (keeps the
     * current one) rather than letting a bad payload lock everyone out.
     * Returns true when the value was applied.
     */
    fun updateCampus(latitude: Double?, longitude: Double?, radiusMeters: Double?): Boolean {
        if (latitude == null || longitude == null || radiusMeters == null) return false
        if (latitude !in -90.0..90.0 || longitude !in -180.0..180.0 || radiusMeters !in 20.0..5000.0) return false
        campus = CampusGeofence(latitude, longitude, radiusMeters)
        return true
    }

    /** For tests: back to the built-in fallback. */
    fun resetCampus() {
        campus = defaultCampus
    }

    /**
     * Calculates the great-circle distance between two points in meters using Haversine formula
     */
    fun calculateHaversineDistance(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        val r = 6371000.0 // Earth's radius in meters
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    fun distanceToCampus(currentLat: Double, currentLon: Double): Double {
        val c = campus
        return calculateHaversineDistance(currentLat, currentLon, c.latitude, c.longitude)
    }

    fun isWithinCampusGeofence(
        currentLat: Double,
        currentLon: Double,
        maxRadiusMeters: Double = campus.radiusMeters
    ): Boolean = distanceToCampus(currentLat, currentLon) <= maxRadiusMeters
}
