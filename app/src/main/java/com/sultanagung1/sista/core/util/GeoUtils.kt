package com.sultanagung1.sista.core.util

import kotlin.math.*

object GeoUtils {

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

    fun isWithinCampusGeofence(
        currentLat: Double,
        currentLon: Double,
        maxRadiusMeters: Double = Constants.CAMPUS_RADIUS_METERS
    ): Boolean {
        val distance = calculateHaversineDistance(
            lat1 = currentLat,
            lon1 = currentLon,
            lat2 = Constants.CAMPUS_LATITUDE,
            lon2 = Constants.CAMPUS_LONGITUDE
        )
        return distance <= maxRadiusMeters
    }
}
