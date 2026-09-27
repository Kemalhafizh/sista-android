package com.sultanagung1.sista.widget

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * FASE 14 regression guard: the widget layouts used to ship sample data
 * ("✅ HADIR (06:45 WIB)", "Agustus 2026: LUNAS", invented lessons, fixed
 * prayer times) that every user saw as their own status.
 */
class WidgetResourcesGuardTest {

    private val layouts = listOf("widget_attendance", "widget_spp", "widget_schedule", "widget_prayer")
        .map { File(WidgetTestStrings.resDir(), "layout/$it.xml") }

    @Test
    fun widgetLayoutsOnlyUseStringResources() {
        val literal = Regex("""android:text="(?!@string/)[^"]*"""")
        for (layout in layouts) {
            assertTrue("${layout.name} missing", layout.isFile)
            val hits = literal.findAll(layout.readText()).map { it.value }.toList()
            assertTrue("${layout.name} has literal text: $hits", hits.isEmpty())
        }
    }

    @Test
    fun widgetLayoutsOnlyUseRemoteViewsSafeViews() {
        val allowed = setOf("LinearLayout", "FrameLayout", "RelativeLayout", "TextView", "ImageView")
        val tag = Regex("""<([A-Za-z.]+)[\s>/]""")
        for (layout in layouts) {
            val tags = tag.findAll(layout.readText()).map { it.groupValues[1] }.filter { it != "?xml" }.toSet()
            assertTrue("${layout.name} uses ${tags - allowed}", allowed.containsAll(tags))
        }
    }

    @Test
    fun widgetStringsCarryNoStatusOrTimes() {
        val clock = Regex("""\b\d{1,2}[:.]\d{2}\b""")
        val fakeStatus = Regex("""\bLUNAS\b|\bPAID\b|✅""", RegexOption.IGNORE_CASE)
        for (dir in listOf("values", "values-en", "values-ar")) {
            val widgetStrings = WidgetTestStrings.strings(dir).filterKeys { it.startsWith("widget_") }
            assertTrue("$dir has no widget strings", widgetStrings.isNotEmpty())
            for ((name, value) in widgetStrings) {
                assertTrue("$dir/$name contains a clock time: $value", !clock.containsMatchIn(value))
                assertTrue("$dir/$name contains a status: $value", !fakeStatus.containsMatchIn(value))
            }
        }
    }

    @Test
    fun providersDoNotFetchFromTheNetwork() {
        val dir = File(WidgetTestStrings.resDir().parentFile, "java/com/sultanagung1/sista/widget")
        val providers = dir.listFiles { f -> f.name.endsWith("WidgetProvider.kt") }.orEmpty()
        assertTrue("no providers found in $dir", providers.size == 4)
        for (provider in providers) {
            val text = provider.readText()
            for (forbidden in listOf("Repository", "ApiClient", "retrofit", "okhttp")) {
                assertTrue("${provider.name} references $forbidden", !text.contains(forbidden))
            }
        }
    }
}
