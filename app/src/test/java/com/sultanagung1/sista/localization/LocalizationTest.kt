package com.sultanagung1.sista.localization

import androidx.compose.ui.unit.LayoutDirection
import com.sultanagung1.sista.core.accessibility.AppLanguage
import com.sultanagung1.sista.core.accessibility.ArabicStrings
import com.sultanagung1.sista.core.accessibility.EnglishStrings
import com.sultanagung1.sista.core.accessibility.IndonesianStrings
import com.sultanagung1.sista.core.accessibility.StringsDefinition
import com.sultanagung1.sista.core.accessibility.getAppStrings
import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.lang.reflect.Modifier

class LocalizationTest {

    @Test
    fun testAllLanguagesHaveCompleteStringDefinitions() {
        val languages = listOf(
            "Indonesian" to IndonesianStrings,
            "English" to EnglishStrings,
            "Arabic" to ArabicStrings
        )

        val fields = StringsDefinition::class.java.declaredFields.filter {
            !Modifier.isStatic(it.modifiers)
        }

        assertTrue("StringsDefinition should have fields defined", fields.isNotEmpty())

        for ((langName, strings) in languages) {
            for (field in fields) {
                field.isAccessible = true
                val value = field.get(strings) as? String
                assertNotNull("[$langName] Field ${field.name} should not be null", value)
                assertFalse("[$langName] Field ${field.name} should not be empty or blank", value!!.isBlank())
            }
        }
    }

    @Test
    fun testLayoutDirectionMapping() {
        assertEquals(LayoutDirection.Ltr, AppLanguage.INDONESIAN.layoutDirection)
        assertEquals(LayoutDirection.Ltr, AppLanguage.ENGLISH.layoutDirection)
        assertEquals(LayoutDirection.Rtl, AppLanguage.ARABIC.layoutDirection)

        assertFalse(AppLanguage.INDONESIAN.layoutDirection == LayoutDirection.Rtl)
        assertTrue(AppLanguage.ARABIC.layoutDirection == LayoutDirection.Rtl)
    }

    @Test
    fun testKurikulumMerdekaProperNounsInvariant() {
        // Kurikulum Merdeka proper nouns must remain invariant in Indonesian across all locales
        for (strings in listOf(IndonesianStrings, EnglishStrings, ArabicStrings)) {
            assertEquals("SMA Islam Sultan Agung 1 Semarang", strings.schoolName)
            assertTrue("Grades tab should retain KKTP proper noun: ${strings.gradesTab}", strings.gradesTab.contains("KKTP"))
            assertTrue("KKTP passed status should retain KKTP: ${strings.kktpPassed}", strings.kktpPassed.contains("KKTP"))
        }
    }

    @Test
    fun testIslamicGreetingsIntegrity() {
        // Arabic greeting must contain authentic Arabic script
        assertTrue(
            "Arabic greeting must use Arabic script: ${ArabicStrings.islamicGreeting}",
            ArabicStrings.islamicGreeting.contains("السَّلَامُ عَلَيْكُمْ")
        )

        // Indonesian and English must use authentic transliteration
        assertTrue(
            "Indonesian greeting must contain Assalamu'alaikum: ${IndonesianStrings.islamicGreeting}",
            IndonesianStrings.islamicGreeting.contains("Assalamu'alaikum", ignoreCase = true)
        )
        assertTrue(
            "English greeting must contain Assalamu'alaikum: ${EnglishStrings.islamicGreeting}",
            EnglishStrings.islamicGreeting.contains("Assalamu'alaikum", ignoreCase = true)
        )
    }

    @Test
    fun testXmlStringResourceConsistency() {
        val candidates = listOf(
            File("src/main/res"),
            File("app/src/main/res"),
            File("../app/src/main/res")
        )
        val resDir = candidates.firstOrNull { it.exists() && it.isDirectory }
        assertNotNull("Resource directory should exist in one of the candidate paths", resDir)

        val idFile = File(resDir, "values/strings.xml")
        val enFile = File(resDir, "values-en/strings.xml")
        val arFile = File(resDir, "values-ar/strings.xml")

        assertTrue("values/strings.xml should exist", idFile.exists())
        assertTrue("values-en/strings.xml should exist", enFile.exists())
        assertTrue("values-ar/strings.xml should exist", arFile.exists())

        fun extractKeys(file: File): Set<String> {
            val keyRegex = Regex("""<string\s+name="([^"]+)"""")
            return file.readLines().mapNotNull { line ->
                keyRegex.find(line)?.groupValues?.get(1)
            }.toSet()
        }

        val idKeys = extractKeys(idFile)
        val enKeys = extractKeys(enFile)
        val arKeys = extractKeys(arFile)

        assertFalse("ID strings should not be empty", idKeys.isEmpty())
        assertEquals("English keys should match Indonesian keys count", idKeys.size, enKeys.size)
        assertEquals("Arabic keys should match Indonesian keys count", idKeys.size, arKeys.size)

        val missingInEn = idKeys - enKeys
        assertTrue("No keys should be missing in English: $missingInEn", missingInEn.isEmpty())

        val missingInAr = idKeys - arKeys
        assertTrue("No keys should be missing in Arabic: $missingInAr", missingInAr.isEmpty())
    }

    @Test
    fun testLanguageFromCode() {
        assertEquals(AppLanguage.INDONESIAN, AppLanguage.fromCode("id"))
        assertEquals(AppLanguage.ENGLISH, AppLanguage.fromCode("en"))
        assertEquals(AppLanguage.ARABIC, AppLanguage.fromCode("ar"))

        // Case-insensitivity test
        assertEquals(AppLanguage.ARABIC, AppLanguage.fromCode("AR"))
        assertEquals(AppLanguage.ENGLISH, AppLanguage.fromCode("En"))

        // Unknown code fallback to Indonesian
        assertEquals(AppLanguage.INDONESIAN, AppLanguage.fromCode("jp"))
        assertEquals(AppLanguage.INDONESIAN, AppLanguage.fromCode(""))

        // getAppStrings getter test
        assertEquals(IndonesianStrings, getAppStrings(AppLanguage.INDONESIAN))
        assertEquals(EnglishStrings, getAppStrings(AppLanguage.ENGLISH))
        assertEquals(ArabicStrings, getAppStrings(AppLanguage.ARABIC))
    }
}
