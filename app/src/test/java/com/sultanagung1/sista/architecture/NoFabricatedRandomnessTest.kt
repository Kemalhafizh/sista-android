package com.sultanagung1.sista.architecture

import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

/**
 * What the app shows comes from the server. A value made up with Random,
 * `.random()` or `.shuffled()` and shown as if it were real is not allowed in
 * main sources. Randomness that is the feature itself (animation, unpredictable
 * proctoring) is listed in [ALLOWED] with the reason. Random UUIDs for ids and
 * idempotency keys are fine and are not counted.
 *
 * See CLAUDE.md, "Tidak ada angka karangan".
 */
class NoFabricatedRandomnessTest {

    private val allowed = mapOf(
        "core/designsystem/src/main/java/com/sultanagung1/sista/core/designsystem/ConfettiEffect.kt" to
            "Animasi konfeti: posisi dan warna partikel.",
    )

    private val pattern = Regex("""\bRandom\s*[.(]|kotlin\.random|java\.util\.Random\b|\bSecureRandom\b|Math\.random\s*\(|\.random\s*\(|\.shuffled\s*\(|\.shuffle\s*\(""")

    /** Comments and string literals never count. */
    private fun code(source: String): String = source
        .replace(Regex("""/\*.*?\*/""", RegexOption.DOT_MATCHES_ALL), "")
        // Unrolled and possessive: `(?:a|b)*` recurses once per character and overflows
        // the smaller stack of the CI runner on long spans between quotes.
        .replace(Regex(""""[^"\\]*+(?:\\.[^"\\]*+)*+""""), "\"\"")
        .lines().joinToString("\n") { it.substringBefore("//") }

    private val root: File = generateSequence(File("").absoluteFile) { it.parentFile }
        .first { File(it, "settings.gradle").exists() || File(it, "settings.gradle.kts").exists() }

    private fun mainSources(): Sequence<File> =
        (listOf(File(root, "app")) + listOf("core", "feature").flatMap { File(root, it).listFiles()?.toList().orEmpty() })
            .map { File(it, "src/main") }
            .filter { it.isDirectory }
            .asSequence()
            .flatMap { it.walkTopDown() }
            .filter { it.isFile && it.extension == "kt" }

    @Test
    fun mainSourcesDoNotMakeUpValuesAtRandom() {
        val found = mainSources()
            .mapNotNull { file ->
                val lines = code(file.readText()).lines()
                    .mapIndexedNotNull { i, line -> (i + 1).takeIf { pattern.containsMatchIn(line) } }
                if (lines.isEmpty()) null else file.relativeTo(root).invariantSeparatorsPath to lines
            }
            .toMap()

        val problems = found.filterKeys { it !in allowed }
            .map { (path, lines) -> "$path: nilai acak di baris ${lines.joinToString()}. Ambil dari server, atau tampilkan \"–\"." } +
            allowed.keys.filterNot { it in found }.map { "$it: sudah tidak memakai nilai acak. Hapus dari daftar izin." }

        assertEquals("Nilai acak di kode aplikasi:\n" + problems.joinToString("\n"), emptyList<String>(), problems)
    }

    @Test
    fun commentsStringsAndUuidsDoNotCount() {
        val sample = """
            // Random.nextInt() in a comment
            val label = "pick .random() later"
            val id = UUID.randomUUID().toString()
            val bad = listOf(1, 2).random()
        """.trimIndent()
        val hits = code(sample).lines().filter { pattern.containsMatchIn(it) }
        assertEquals(listOf("val bad = listOf(1, 2).random()"), hits)
    }
}
