package com.sultanagung1.sista.telemetry

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * FirebaseTelemetrySink once built FirebaseOptions by hand with the project's
 * Google API key as a string literal; GitHub secret scanning flagged it as a
 * public leak. Firebase config belongs only in app/google-services.json, which
 * the google-services plugin turns into resources.
 */
class NoHardcodedApiKeysGuardTest {

    private val googleApiKey = Regex("""AIza[0-9A-Za-z_\-]{35}""")

    @Test
    fun sourceCodeHasNoGoogleApiKeys() {
        val repoRoot = listOf(File(".."), File(".")).first { File(it, "settings.gradle").exists() }.canonicalFile
        val offenders = repoRoot.walkTopDown()
            .onEnter { it.name != "build" && !it.name.startsWith(".") }
            .filter { it.isFile && it.extension in setOf("kt", "java", "xml") && it.invariantSeparatorsPath.contains("/src/") }
            .filter { googleApiKey.containsMatchIn(it.readText()) }
            .map { it.relativeTo(repoRoot).invariantSeparatorsPath }
            .toList()
        assertTrue("Google API key literal in source (use google-services.json): $offenders", offenders.isEmpty())
    }

    @Test
    fun telemetrySinkDoesNotBuildFirebaseOptionsByHand() {
        val repoRoot = listOf(File(".."), File(".")).first { File(it, "settings.gradle").exists() }.canonicalFile
        val sink = File(repoRoot, "core/common/src/main/java/com/sultanagung1/sista/core/telemetry/FirebaseTelemetrySink.kt").readText()
        assertTrue("FirebaseOptions must come from google-services resources", !sink.contains("FirebaseOptions.Builder"))
        assertTrue("Sink must fall back to FirebaseApp.initializeApp(context)", sink.contains("FirebaseApp.initializeApp(context)"))
    }
}
