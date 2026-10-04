package com.sultanagung1.sista.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * FASE 76.3 – 76.8 source guards, plus the automated touch-target linter
 * 76.8 asks for ("continuous, not a one-off audit"). Reads source files, so it
 * runs without an emulator.
 */
class Fase76CompletionGuardTest {

    private val repoRoot: File = listOf(File(".."), File("."))
        .map { it.canonicalFile }
        .first { File(it, "settings.gradle").exists() }

    private fun source(relativePath: String): String {
        val file = File(repoRoot, relativePath)
        assertTrue("$relativePath not found", file.exists())
        return file.readText()
    }

    /** Code only — comments that explain what was removed don't count. */
    private fun code(relativePath: String): String =
        source(relativePath).replace(Regex("""/\*.*?\*/""", RegexOption.DOT_MATCHES_ALL), "")
            .lines().joinToString("\n") { it.substringBefore("//") }

    private val appNav = "app/src/main/java/com/sultanagung1/sista/ui/navigation/AppNavigation.kt"
    private val authGraph = "app/src/main/java/com/sultanagung1/sista/ui/navigation/graphs/AuthNavGraph.kt"
    private val session = "core/common/src/main/java/com/sultanagung1/sista/core/storage/SessionManager.kt"
    private val examRoom = "feature/cbt/src/main/java/com/sultanagung1/sista/ui/cbt/CbtExamRoomScreen.kt"
    private val quickActions = "feature/home/src/main/java/com/sultanagung1/sista/ui/home/sections/HomeQuickAccess.kt"
    private val homeScreen = "feature/home/src/main/java/com/sultanagung1/sista/ui/home/HomeScreen.kt"
    private val schedulePreview = "feature/home/src/main/java/com/sultanagung1/sista/ui/home/sections/HomeTodayCard.kt"
    private val homeStrings = "feature/home/src/main/res/values/strings.xml"
    private val homeStringsAr = "feature/home/src/main/res/values-ar/strings.xml"
    private val database = "core/database/src/main/java/com/sultanagung1/sista/data/local/SistaDatabase.kt"
    private val usageDao = "core/database/src/main/java/com/sultanagung1/sista/data/local/dao/FeatureUsageDao.kt"
    private val tieredLoading = "core/designsystem/src/main/java/com/sultanagung1/sista/core/designsystem/SulaoneTieredLoading.kt"
    private val teacherDash = "feature/teacher/src/main/java/com/sultanagung1/sista/ui/teacher/TeacherDashboardScreen.kt"
    private val parentDash = "feature/parent/src/main/java/com/sultanagung1/sista/ui/parent/ParentDashboardScreen.kt"
    private val adminDash = "feature/admin/src/main/java/com/sultanagung1/sista/ui/admin/AdminDashboardScreen.kt"
    private val profile = "feature/profile/src/main/java/com/sultanagung1/sista/ui/profile/ProfileScreen.kt"
    private val themeManager = "core/common/src/main/java/com/sultanagung1/sista/core/accessibility/ThemeManager.kt"
    private val tutorScreen = "feature/academic/src/main/java/com/sultanagung1/sista/ui/ai/AiTutorScreen.kt"
    private val tutorVm = "feature/academic/src/main/java/com/sultanagung1/sista/ui/ai/AiViewModel.kt"
    private val chatScreen = "feature/chat/src/main/java/com/sultanagung1/sista/ui/chat/ChatScreen.kt"
    private val theme = "core/designsystem/src/main/java/com/sultanagung1/sista/core/designsystem/Theme.kt"
    private val mainActivity = "app/src/main/java/com/sultanagung1/sista/MainActivity.kt"

    // ── 76.3 navigation ──────────────────────────────────────────────────

    @Test
    fun roleHomeHasOneSourceOfTruth() {
        // The home comes from the account's server-given features, not from a
        // role the app interprets (FeatureCatalog.homeRouteFor).
        assertTrue(code(appNav).contains("FeatureCatalog.homeRouteFor(it.capabilities)"))
        assertFalse(code(appNav).contains("UserRoles.homeRouteFor("))
        assertFalse(code(appNav).contains("UserRoles.groupOf("))
        assertTrue(code(authGraph).contains("onLoginSuccess = onSignedIn"))
        assertTrue("Backend aliases are folded once, at the session", code(session).contains("UserRoles.normalize("))
    }

    @Test
    fun contextualFabLivesInTheRootScaffoldOnlyOnHomeTabs() {
        val nav = code(appNav)
        assertTrue(nav.contains("floatingActionButton = {"))
        assertTrue(nav.contains("ContextualFab.actionFor(currentRoute, userRole)"))
        assertTrue("Only on tab routes", nav.contains("fabAction != null && isTabRoute"))
    }

    @Test
    fun backDuringAnExamAsksInsteadOfClosingOnAnAccidentalSwipe() {
        val room = code(examRoom)
        val backHandler = room.substringAfter("BackHandler(enabled = true) {").substringBefore("\n    }")
        assertTrue("Back opens the confirmation", backHandler.contains("showExitWarningDialog = true"))
        assertFalse("Back alone must not force-close", backHandler.contains("forceCloseExam"))
        // Confirming really closes the exam (the dialog used to just navigate back,
        // leaving the exam open) and it was never shown at all before.
        assertTrue(room.contains("exitAndForceClose(\"back_button_pressed\")"))
        assertTrue(room.contains("viewModel.forceCloseExam(examId, reason, vault)"))
    }

    // ── 76.4 personalization ─────────────────────────────────────────────

    @Test
    fun usageIsStoredInRoomPerAccount() {
        val db = code(database)
        assertTrue(db.contains("FeatureUsageEntity::class"))
        assertTrue(db.contains("version = 2"))
        val dao = code(usageDao)
        assertTrue(dao.contains("WHERE user_id = :userId"))
        assertFalse("Upsert syntax needs API 30; minSdk is 26", dao.contains("ON CONFLICT"))
    }

    @Test
    fun personalizationIsLabelledHonestly() {
        // The text lives in strings.xml now (id, en, ar); the labels say what it is in each.
        val all = (listOf(quickActions, homeScreen).map { code(it) } + source(homeStrings)).joinToString("\n")
        assertTrue(code(quickActions).contains("R.string.home_frequent"))
        assertTrue(source(homeStrings).contains(">Sering Dipakai<"))
        assertFalse("A tap counter is not AI", Regex("(?i)(rekomendasi|direkomendasikan)\\s+ai|ai personali").containsMatchIn(all))
        assertFalse("A tap counter is not AI", source(homeStringsAr).contains("ذكاء"))
        assertTrue(code(quickActions).contains("UsageRanking.rank("))
        assertTrue("User can reset", code(quickActions).contains("R.string.home_usage_reset"))
        assertTrue(source(homeStrings).contains(">Atur ulang<"))
        assertTrue(code(homeScreen).contains("viewModel::recordFeatureUse"))
    }

    // ── 76.5 tiered loading ──────────────────────────────────────────────

    @Test
    fun tieredLoadingReallyUsesTheTiers() {
        val c = code(tieredLoading)
        assertTrue(c.contains("delay(LoadingTierTiming.SPINNER_AFTER_MS)"))
        assertTrue(c.contains("LoadingTier.Hidden ->"))
        assertTrue(c.contains("LoadingTier.Spinner ->"))
        assertTrue(c.contains("LoadingTier.Skeleton ->"))
    }

    @Test
    fun dashboardsUseContentShapedSkeletons() {
        assertTrue("admin home: first-load skeleton", code(adminDash).contains("SkeletonList("))
        // The rebuilt teacher home shows a list skeleton for its first load, not a spinner.
        val teacher = code(teacherDash)
        assertTrue("teacher home: first-load skeleton", teacher.contains("firstLoad -> SkeletonList("))
        // The text is in strings.xml now (id, en, ar).
        assertTrue("Parent portal has a real empty state", code(parentDash).contains("R.string.parent_no_children"))
        assertTrue(source("feature/parent/src/main/res/values/strings.xml").contains(">Belum ada anak yang ditautkan<"))
        assertTrue("parent home: first-load skeleton", code(parentDash).contains("SkeletonList("))
    }

    @Test
    fun homeNoLongerSaysNoClassesWhileLoadingOrAfterAnError() {
        val c = code(schedulePreview)
        val loading = c.indexOf("todaySchedules.isEmpty() && isLoading")
        val error = c.indexOf("todaySchedules.isEmpty() && loadError != null")
        val empty = c.indexOf("title = stringResource(R.string.home_no_lessons)")
        assertTrue(loading in 0 until empty)
        assertTrue(error in 0 until empty)
        val home = code(homeScreen)
        assertFalse("No fixed-duration fake refresh", home.contains("delay(750)"))
        assertTrue(home.contains("refreshing = refreshRequested && uiState.isLoading"))
    }

    // ── 76.6 AMOLED ──────────────────────────────────────────────────────

    @Test
    fun amoledIsOfferedWhereDarkModeIsTurnedOn() {
        val p = code(profile)
        assertTrue(p.contains("AppThemeMode.AMOLED_BLACK)"))
        assertTrue(p.contains("AMOLED_BATTERY_NOTE"))
        // The note is in strings.xml now (id, en, ar); still honest about LCD in each.
        val res = "feature/profile/src/main/res"
        assertTrue("Honest about LCD", source("$res/values/strings.xml").contains("Di layar LCD"))
        assertTrue("Honest about LCD (en)", source("$res/values-en/strings.xml").contains("On LCD screens"))
        assertFalse(code(themeManager).contains("Super hemat baterai"))
    }

    // ── 76.7 tutor chat ──────────────────────────────────────────────────

    @Test
    fun tutorIsTransparentAndRecoversForReal() {
        val screen = code(tutorScreen)
        assertFalse("App can't know the server is up 24/7", screen.contains("Online 24/7"))
        assertTrue(screen.contains("TUTOR_ENGINE_DISCLOSURE"))
        assertTrue(source(tutorScreen).contains("belum model AI generatif"))
        assertTrue("Retry resends", screen.contains("onRetry = { viewModel.retryLastMessage() }"))
        assertFalse("Retry must not just hide the error", screen.contains("onRetry = { viewModel.clearError() }"))
        assertTrue(screen.contains("AiReplyFormatting.splitIntoBubbles("))
        assertTrue(screen.contains("listState.animateScrollToItem("))
        assertFalse("No invented confidence score", screen.contains("comprehension"))
        assertTrue(code(tutorVm).contains("fun retryLastMessage()"))
    }

    @Test
    fun composersSitAboveTheKeyboard() {
        for (screen in listOf(tutorScreen, chatScreen)) {
            val c = code(screen)
            assertTrue("$screen: consumes scaffold insets", c.contains(".consumeWindowInsets(paddingValues)"))
            assertTrue("$screen: ime padding", c.contains(".imePadding()"))
        }
    }

    // ── 76.8 accessibility ───────────────────────────────────────────────

    @Test
    fun dynamicTypeIsAppliedGloballyAndNotOverridden() {
        assertTrue(code(theme).contains("FontScaleManager.calculateEffectiveFontScale(density.fontScale, fontScale)"))
        assertTrue(code(mainActivity).contains("fontScale = fontScale"))
        // Any other LocalDensity override would silently undo the user's font size.
        val overrides = sourceFiles().filter { code(it).contains("LocalDensity provides") }.map { it }
        assertEquals(listOf(theme), overrides)
    }

    @Test
    fun everyTappableElementHasAtLeast44dpTarget() {
        val violations = sourceFiles().flatMap { TouchTargetLint.check(it, source(it)) }
        assertTrue(
            "Touch targets under 44dp (WCAG 2.2 AA / FASE 76.8):\n" + violations.joinToString("\n"),
            violations.isEmpty()
        )
    }

    @Test
    fun theLinterItselfCatchesTheKnownBadPatterns() {
        val bad = """
            Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(26.dp).clickable { })
            Box(modifier = Modifier.size(36.dp).clip(CircleShape).clickable { })
            Text(text = "Lihat Semua", modifier = Modifier.clip(RoundedCornerShape(6.dp)).springPressable { go() }.padding(4.dp))
        """.trimIndent()
        assertEquals(3, TouchTargetLint.check("Bad.kt", bad).size)

        val good = """
            Box(modifier = Modifier.size(44.dp).clickable { })
            Text(text = "Lihat Semua", modifier = Modifier.minimumInteractiveComponentSize().springPressable { go() })
            Column(modifier = Modifier.width(68.dp).sulaoneInteractiveTouchTarget(48.dp).springPressable { })
        """.trimIndent()
        assertEquals(emptyList<String>(), TouchTargetLint.check("Good.kt", good))
    }

    private fun sourceFiles(): List<String> =
        listOf("app", "core", "feature").flatMap { top ->
            File(repoRoot, top).walkTopDown()
                .filter { it.isFile && it.extension == "kt" && it.path.contains("${File.separator}src${File.separator}main${File.separator}") }
                .filterNot { it.path.contains("${File.separator}build${File.separator}") }
                .map { it.relativeTo(repoRoot).path }
                .toList()
        }.sorted()
}

/**
 * FASE 76.8 touch-target linter. Three rules, each a pattern that shipped:
 * 1. a click modifier chain that sizes the element below 44dp;
 * 2. a clickable Icon(...) with no touch-target helper (Icon is 24dp);
 * 3. a clickable Text(...) link with no touch-target helper (~20-24dp tall).
 * `sulaoneInteractiveTouchTarget` / `minimumInteractiveComponentSize` /
 * a 44-48dp min-size clear a finding.
 */
internal object TouchTargetLint {
    private const val MIN_DP = 44.0
    private val CLICK = Regex("""\.(clickable|springPressable|combinedClickable|toggleable|selectable)\b""")
    private val SIZE = Regex("""\.(size|height|requiredSize|requiredHeight)\(\s*(\d+(?:\.\d+)?)\.dp\s*\)""")
    private val SIZE_WH = Regex("""\.size\(\s*width\s*=\s*(\d+)\.dp\s*,\s*height\s*=\s*(\d+)\.dp""")
    private val SAFE = listOf(
        "sulaoneInteractiveTouchTarget", "minimumInteractiveComponentSize",
        "defaultMinSize(minWidth = 48", "defaultMinSize(minHeight = 48",
        "heightIn(min = 48", "heightIn(min = 44", "sizeIn(minWidth = 48"
    )

    fun check(path: String, src: String): List<String> =
        (sizedChains(src) + unguardedCalls(src, "Icon") + unguardedCalls(src, "Text"))
            .distinct()
            .sorted()
            .map { "$path:$it" }

    /** Rule 1: explicit size under 44dp anywhere in the clicked element's modifier chain. */
    private fun sizedChains(src: String): List<Int> = CLICK.findAll(src).mapNotNull { match ->
        val start = match.range.first
        val chainStart = maxOf(src.lastIndexOf("Modifier", start), src.lastIndexOf("modifier", start))
        if (chainStart < 0) return@mapNotNull null
        val before = src.substring(chainStart, start)
        if (before.count { it == '(' } < before.count { it == ')' }) return@mapNotNull null

        // Continue over following ".xxx" lines of the same chain, minus lambda bodies.
        var end = src.indexOf('\n', start).let { if (it < 0) src.length else it }
        while (end < src.length) {
            val next = src.indexOf('\n', end + 1).let { if (it < 0) src.length else it }
            if (src.substring(end + 1, next).trim().startsWith(".")) end = next else break
        }
        var after = src.substring(start, end)
        repeat(2) { after = after.replace(Regex("""\{[^{}]*\}"""), "") }
        val chain = before + after

        if (SAFE.any { chain.contains(it) }) return@mapNotNull null
        val sizes = SIZE.findAll(chain).map { it.groupValues[2].toDouble() } +
            SIZE_WH.findAll(chain).map { minOf(it.groupValues[1].toDouble(), it.groupValues[2].toDouble()) }
        val smallest = sizes.minOrNull() ?: return@mapNotNull null
        if (smallest < MIN_DP) lineOf(src, start) else null
    }.toList()

    /** Rules 2 and 3: `Icon(` / `Text(` calls whose own arguments make them clickable. */
    private fun unguardedCalls(src: String, callee: String): List<Int> =
        Regex("""(?<![\w.])$callee\(""").findAll(src).mapNotNull { match ->
            val open = match.range.last
            var depth = 0
            var i = open
            while (i < src.length) {
                when (src[i]) {
                    '(' -> depth++
                    ')' -> { depth--; if (depth == 0) break }
                }
                i++
            }
            val call = src.substring(match.range.first, minOf(i + 1, src.length))
            val clickable = Regex("""\.(clickable|springPressable|combinedClickable)\b""").containsMatchIn(call)
            if (clickable && SAFE.none { call.contains(it) }) lineOf(src, match.range.first) else null
        }.toList()

    private fun lineOf(src: String, index: Int): Int = src.substring(0, index).count { it == '\n' } + 1
}
