package com.sultanagung1.sista.ui.profile

import android.content.pm.ApplicationInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StudentProfileFormatTest {

    @Test
    fun numbersUseIndonesianSeparatorsAndDashWhenMissing() {
        assertEquals("80,5", idNumber(80.5))
        assertEquals("80", idNumber(80.0))
        assertEquals("1.250", idNumber(1250.0))
        assertEquals(MISSING, idNumber(null))
    }

    @Test
    fun rank() {
        assertEquals("2 dari 30 siswa", rankLabel(2, 30))
        assertEquals("2", rankLabel(2, null))
        assertEquals(MISSING, rankLabel(null, 30))
    }

    @Test
    fun noTahfidzTargetIsNotZeroOfZero() {
        assertNull(juzLabel(null, null))
        assertNull(juzLabel(3, null))
        assertEquals("1 dari 4 juz", juzLabel(1, 4))
    }

    @Test
    fun streakIsInDaysNotWeeks() {
        assertEquals("3 hari", streakLabel(3))
        assertEquals(MISSING, streakLabel(null))
    }

    @Test
    fun percentBodyPointsAndDate() {
        assertEquals("66,7%", percentLabel(66.7))
        assertEquals(MISSING, percentLabel(null))
        assertEquals("165 cm · 52,5 kg", bodyLabel(165.0, 52.5))
        assertEquals("165 cm", bodyLabel(165.0, null))
        assertEquals(MISSING, bodyLabel(null, null))
        assertEquals("+10", signedPoints(10, "+"))
        assertEquals("0", signedPoints(0, "+"))
        assertEquals(MISSING, signedPoints(null, "+"))
        assertEquals("12 September 2026", dateLabel("2026-09-12"))
        assertEquals(MISSING, dateLabel("bukan tanggal"))
        assertEquals(MISSING, dateLabel(null))
    }

    @Test
    fun theCrashDrillIsOnlyInDebuggableBuilds() {
        assertTrue(crashDrillAvailable(ApplicationInfo.FLAG_DEBUGGABLE))
        assertFalse(crashDrillAvailable(0))
    }
}
