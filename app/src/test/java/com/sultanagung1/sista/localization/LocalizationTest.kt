package com.sultanagung1.sista.localization

import androidx.compose.ui.unit.LayoutDirection
import com.sultanagung1.sista.core.accessibility.AppLanguage
import com.sultanagung1.sista.core.accessibility.AppLocale
import com.sultanagung1.sista.core.accessibility.SyncDecision
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * The app is offered in Indonesian (res/values), English (values-en) and
 * Arabic (values-ar), like the web. Every module that has strings has all
 * three, with the same keys, nothing left empty and the same placeholders, so
 * a screen never falls back to Indonesian halfway through.
 */
class LocalizationTest {

    private val root: File = generateSequence(File("").absoluteFile) { it.parentFile }
        .first { File(it, "settings.gradle").exists() || File(it, "settings.gradle.kts").exists() }

    private val resDirs: List<File> =
        (listOf(File(root, "app")) + listOf("core", "feature").flatMap { File(root, it).listFiles()?.toList().orEmpty() })
            .map { File(it, "src/main/res") }
            .filter { File(it, "values/strings.xml").exists() }

    private data class Entry(val text: String, val placeholders: List<String>)

    private val entryPattern = Regex("""<(string|plurals)\s+name="([^"]+)"[^>]*>(.*?)</\1>""", RegexOption.DOT_MATCHES_ALL)
    private val placeholderPattern = Regex("""%(\d+\$)?[sdf]""")

    private fun entries(file: File): Map<String, Entry> =
        if (!file.exists()) emptyMap()
        else entryPattern.findAll(file.readText()).associate { m ->
            val body = m.groupValues[3]
            m.groupValues[2] to Entry(body, placeholderPattern.findAll(body).map { it.value }.sorted().toList())
        }

    @Test
    fun everyModuleWithStringsHasAllThreeLanguages() {
        assertTrue("Tidak ada strings.xml yang ditemukan", resDirs.isNotEmpty())
        val problems = mutableListOf<String>()
        for (res in resDirs) {
            val module = res.parentFile.parentFile.parentFile.relativeTo(root).invariantSeparatorsPath
            val id = entries(File(res, "values/strings.xml"))
            for (lang in listOf("en", "ar")) {
                val other = entries(File(res, "values-$lang/strings.xml"))
                (id.keys - other.keys).forEach { problems += "$module: \"$it\" belum ada di values-$lang" }
                (other.keys - id.keys).forEach { problems += "$module: \"$it\" ada di values-$lang tapi tidak di values" }
                id.forEach { (key, entry) ->
                    val translated = other[key] ?: return@forEach
                    if (translated.text.isBlank()) problems += "$module: \"$key\" kosong di values-$lang"
                    if (translated.placeholders != entry.placeholders) {
                        problems += "$module: \"$key\" values-$lang memakai ${translated.placeholders}, values memakai ${entry.placeholders}"
                    }
                }
            }
            id.filterValues { it.text.isBlank() }.keys.forEach { problems += "$module: \"$it\" kosong di values" }
        }
        assertEquals("Terjemahan belum lengkap:\n" + problems.joinToString("\n"), emptyList<String>(), problems)
    }

    @Test
    fun arabicGreetingUsesArabicScript() {
        val ar = entries(File(root, "app/src/main/res/values-ar/strings.xml"))
        // Compared without harakat: the same marks can be stored in a different order.
        val bare = ar.getValue("islamic_greeting").text.filterNot { it in '\u064B'..'\u0652' }
        assertTrue(bare, bare.contains("السلام عليكم"))
    }

    @Test
    fun layoutDirectionFollowsTheLanguage() {
        assertEquals(LayoutDirection.Ltr, AppLanguage.INDONESIAN.layoutDirection)
        assertEquals(LayoutDirection.Ltr, AppLanguage.ENGLISH.layoutDirection)
        assertEquals(LayoutDirection.Rtl, AppLanguage.ARABIC.layoutDirection)
    }

    @Test
    fun languageTags() {
        assertEquals(AppLanguage.INDONESIAN, AppLanguage.fromCode("id"))
        assertEquals(AppLanguage.ARABIC, AppLanguage.fromCode("AR"))
        assertEquals(AppLanguage.INDONESIAN, AppLanguage.fromCode("jp"))
        // Locale tags from the system, including Java's legacy "in" for Indonesian.
        assertEquals(AppLanguage.ENGLISH, AppLocale.fromTag("en-US"))
        assertEquals(AppLanguage.ARABIC, AppLocale.fromTag("ar-SA"))
        assertEquals(AppLanguage.INDONESIAN, AppLocale.fromTag("in-ID"))
        assertEquals(AppLanguage.INDONESIAN, AppLocale.fromTag("id"))
    }

    @Test
    fun arabicKeepsLatinDigitsLikeTheWeb() {
        val ar = AppLocale.localeOf(AppLanguage.ARABIC)
        assertEquals("ar", ar.language)
        assertEquals("18 / 32", String.format(ar, "%d / %d", 18, 32))
        assertEquals("ar", AppLocale.fromTag(ar.toLanguageTag()).code)
    }

    @Test
    fun everyLanguageNamesItselfInItsOwnScript() {
        assertEquals("Bahasa Indonesia", AppLanguage.INDONESIAN.nativeName)
        assertEquals("English", AppLanguage.ENGLISH.nativeName)
        assertEquals("العربية", AppLanguage.ARABIC.nativeName)
    }

    @Test
    fun aChoiceMadeOnThisPhoneWinsUntilTheServerHasIt() {
        assertEquals(
            SyncDecision.Push(AppLanguage.ENGLISH),
            AppLocale.decide(pending = true, current = AppLanguage.ENGLISH, server = "id"),
        )
    }

    @Test
    fun otherwiseTheAccountsLanguageIsApplied() {
        // Picked on the web: the app follows.
        assertEquals(SyncDecision.Adopt(AppLanguage.ARABIC), AppLocale.decide(pending = false, current = AppLanguage.INDONESIAN, server = "ar"))
        assertEquals(SyncDecision.Nothing, AppLocale.decide(pending = false, current = AppLanguage.ARABIC, server = "ar"))
        assertEquals(SyncDecision.Nothing, AppLocale.decide(pending = false, current = AppLanguage.ARABIC, server = null))
    }
}
