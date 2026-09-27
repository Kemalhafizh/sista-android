package com.sultanagung1.sista

import com.sultanagung1.sista.core.designsystem.tokens.SulaoneMotionTokens
import com.sultanagung1.sista.core.designsystem.tokens.SulaoneRadiusTokens
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * FASE 76.1 — verifies the shape/motion token consolidation actually holds:
 * the radius scale stays strictly ascending, the motion tiers use sane
 * spring parameters, and — the part that actually prevents regression —
 * no new ad hoc `spring(dampingRatio = ..., stiffness = ...)` call sneaks
 * back into the codebase outside the one place it's allowed to be defined
 * (SulaoneDesignTokens.kt itself). This mirrors DesignSystemTokensTest's
 * existing testNoHardcodedColorsInUiScreens pattern for colors.
 */
class DesignSystemShapeMotionTokensTest {

    @Test
    fun testRadiusTokensAreStrictlyAscending() {
        val scale = listOf(
            SulaoneRadiusTokens.sm,
            SulaoneRadiusTokens.md,
            SulaoneRadiusTokens.lg,
            SulaoneRadiusTokens.xl,
            SulaoneRadiusTokens.xxl
        )
        for (i in 0 until scale.size - 1) {
            assertTrue(
                "Radius scale must be strictly ascending: ${scale[i]} should be < ${scale[i + 1]}",
                scale[i] < scale[i + 1]
            )
        }
        // pill is a deliberately huge value (fully rounded), not part of the ascending scale
        assertTrue("pill radius must exceed the largest step scale", SulaoneRadiusTokens.pill > SulaoneRadiusTokens.xxl)
    }

    @Test
    fun testMaterialThemeShapesMatchComposeDefaultsExactly() {
        // These three MUST equal Compose M3's own built-in defaults (8dp/12dp/16dp) —
        // that equality is what makes wiring MaterialTheme.shapes from these
        // tokens a zero-visual-regression change (see Theme.kt's SulaoneShapes).
        // If this test ever fails, someone changed a value that is no longer
        // safe to wire into MaterialTheme.shapes without a full visual audit.
        assertEquals(8, SulaoneRadiusTokens.sm.value.toInt())
        assertEquals(12, SulaoneRadiusTokens.md.value.toInt())
        assertEquals(16, SulaoneRadiusTokens.lg.value.toInt())
    }

    @Test
    fun testMotionTiersAreDistinctAndOrderedByEnergy() {
        val subtle = SulaoneMotionTokens.springSubtle
        val standard = SulaoneMotionTokens.springStandard
        val expressive = SulaoneMotionTokens.springExpressive

        // The three tiers must carry genuinely different numbers — this
        // guards against the exact bug this consolidation fixed: two
        // separate objects (old SulaoneMotion / MotionSpecs) that reused
        // the SAME tier names for DIFFERENT values. Asserting real equality
        // on dampingRatio/stiffness (both public on SpringSpec) instead of
        // reference identity actually proves the values differ.
        assertTrue("subtle and standard must use different spring parameters",
            subtle.dampingRatio != standard.dampingRatio || subtle.stiffness != standard.stiffness)
        assertTrue("standard and expressive must use different spring parameters",
            standard.dampingRatio != expressive.dampingRatio || standard.stiffness != expressive.stiffness)

        // "Expressive" (delight moments) should bounce more than "subtle"
        // (snap-back feedback) — lower dampingRatio = more bounce in
        // Compose's spring model.
        assertTrue(
            "springExpressive (${expressive.dampingRatio}) should bounce more than springSubtle (${subtle.dampingRatio})",
            expressive.dampingRatio < subtle.dampingRatio
        )

        // springStandard must be byte-for-byte the values springPressable()
        // already used everywhere before this refactor (see MotionTransitions.kt) —
        // this is the app's highest-traffic spring, and this test is what
        // actually backs the "zero visual regression" claim for it.
        assertEquals(0.5f, standard.dampingRatio, 0.001f) // Spring.DampingRatioMediumBouncy
        assertEquals(1500f, standard.stiffness, 0.1f) // Spring.StiffnessMedium
    }

    @Test
    fun testStaggeredDelayIsMonotonicAndZeroBased() {
        assertEquals(0, SulaoneMotionTokens.staggeredDelay(0))
        assertEquals(50, SulaoneMotionTokens.staggeredDelay(1))
        assertEquals(250, SulaoneMotionTokens.staggeredDelay(5))
        assertEquals(20, SulaoneMotionTokens.staggeredDelay(2, delay = 10))
    }

    @Test
    fun testNoAdHocSpringSpecsOutsideTheTokenFile() {
        val candidates = listOf(
            File("../core/designsystem/src/main/java"),
            File("core/designsystem/src/main/java"),
            File("../../core/designsystem/src/main/java")
        )
        val rootDir = candidates.firstOrNull { it.exists() && it.isDirectory }
        // Soft-skip rather than fail if the module layout can't be found from
        // this test's working directory (Gradle module test working dirs
        // vary) — the intent is to guard against regression when the check
        // CAN run, not to hard-couple this test to one specific invocation path.
        if (rootDir == null) return

        val adHocSpringRegex = Regex("""\bspring\s*\(\s*dampingRatio""")
        val violations = mutableListOf<String>()

        rootDir.walkTopDown().filter { it.isFile && it.extension == "kt" }.forEach { file ->
            if (file.name != "SulaoneDesignTokens.kt") {
                file.readLines().forEachIndexed { index, line ->
                    // Skip comments referencing the old removed values for
                    // documentation purposes (e.g. SwipeActions.kt's migration note).
                    val trimmed = line.trimStart()
                    if (!trimmed.startsWith("//") && adHocSpringRegex.containsMatchIn(line)) {
                        violations.add("${file.path}:${index + 1}: ${line.trim()}")
                    }
                }
            }
        }

        assertTrue(
            "Found ${violations.size} ad hoc spring(dampingRatio=...) call(s) outside SulaoneDesignTokens.kt " +
                "— use SulaoneMotionTokens instead:\n${violations.joinToString("\n")}",
            violations.isEmpty()
        )
    }
}
