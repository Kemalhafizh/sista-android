package com.sultanagung1.sista.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.PersonOff
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.sultanagung1.sista.core.motion.sulaoneSharedElement
import com.sultanagung1.sista.core.ui.component.Avatar
import com.sultanagung1.sista.core.ui.component.EmptyState
import com.sultanagung1.sista.core.ui.component.ErrorState
import com.sultanagung1.sista.core.ui.component.FilterChipRow
import com.sultanagung1.sista.core.ui.component.IconBadge
import com.sultanagung1.sista.core.ui.component.SistaCard
import com.sultanagung1.sista.core.ui.component.SistaTopBar
import com.sultanagung1.sista.core.ui.component.SkeletonList
import com.sultanagung1.sista.core.ui.component.StatTile
import com.sultanagung1.sista.core.ui.component.StatusPill
import com.sultanagung1.sista.core.ui.theme.ShellTheme
import com.sultanagung1.sista.core.ui.theme.SistaTheme
import com.sultanagung1.sista.core.ui.theme.Spacing
import com.sultanagung1.sista.core.ui.theme.StatusTone
import com.sultanagung1.sista.data.model.StudentProfile360Data

/** A student's academic, ibadah, discipline, activity and health records, as the school has them. */
@Composable
fun StudentProfileComprehensiveScreen(
    viewModel: StudentProfileViewModel,
    onNavigateBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    StudentProfileContent(state, onRetry = viewModel::load, onTab = viewModel::selectTab, onNavigateBack = onNavigateBack)
}

/** The 360 profile without a ViewModel, for previews and screenshots. */
@Composable
fun StudentProfileContent(
    state: StudentProfileUiState,
    onRetry: () -> Unit,
    onTab: (ProfileTab) -> Unit,
    onNavigateBack: () -> Unit,
) {
    ShellTheme {
        val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = { SistaTopBar(title = "Profil lengkap", onBack = onNavigateBack, scrollBehavior = scrollBehavior) },
            containerColor = SistaTheme.colors.background,
        ) { padding ->
            val profile = state.profile
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .testTag("student_profile_root"),
                contentPadding = PaddingValues(start = Spacing.screen, end = Spacing.screen, bottom = Spacing.xxl),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                when {
                    profile == null && state.errorMessage != null -> item(key = "error") {
                        ErrorState(title = "Profil belum bisa dimuat", body = state.errorMessage, onRetry = onRetry, icon = Icons.Outlined.PersonOff)
                    }
                    profile == null -> item(key = "loading") { SkeletonList(rows = 6, modifier = Modifier.padding(top = Spacing.sm)) }
                    else -> {
                        item(key = "header") { Header(profile) }
                        item(key = "tabs") {
                            FilterChipRow(
                                options = ProfileTab.entries,
                                selected = state.tab,
                                onSelect = onTab,
                                label = { it.label },
                                contentPadding = PaddingValues(vertical = Spacing.sm),
                            )
                        }
                        when (state.tab) {
                            ProfileTab.Academic -> academic(profile)
                            ProfileTab.Ibadah -> ibadah(profile)
                            ProfileTab.Discipline -> discipline(profile)
                            ProfileTab.Activities -> activities(profile)
                            ProfileTab.Health -> health(profile)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Header(profile: StudentProfile360Data) {
    val bio = profile.biodata
    val name = bio.name?.takeIf { it.isNotBlank() }
    SistaCard(modifier = Modifier.fillMaxWidth().padding(top = Spacing.sm)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Avatar(name ?: "?", size = 56.dp, modifier = Modifier.sulaoneSharedElement(key = "student_avatar"))
            Spacer(Modifier.width(Spacing.lg))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Spacing.xxs)) {
                Text(name ?: MISSING, style = SistaTheme.typography.titleLarge)
                Text(
                    listOf("Kelas ${bio.className ?: MISSING}", "NISN ${bio.nisn ?: MISSING}").joinToString(" · "),
                    style = SistaTheme.typography.bodyMedium,
                    color = SistaTheme.colors.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun TileRow(vararg tiles: Triple<String, String, String?>) {
    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        tiles.forEach { (label, value, supporting) ->
            StatTile(label = label, value = value, supporting = supporting, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun FactCard(vararg rows: Pair<String, String>) {
    SistaCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            rows.forEach { (label, value) ->
                Row {
                    Text(label, style = SistaTheme.typography.bodyMedium, color = SistaTheme.colors.onSurfaceVariant, modifier = Modifier.width(150.dp))
                    Text(value, style = SistaTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

private fun LazyListScope.academic(profile: StudentProfile360Data) {
    val a = profile.academicSummary
    item(key = "academic_tiles") {
        TileRow(
            Triple("Rata-rata nilai", idNumber(a.averageScore), "Tahun ajaran ini, skala 100"),
            Triple("Peringkat kelas", rankLabel(a.rankInClass, null), a.totalClassStudents?.takeIf { a.rankInClass != null }?.let { "dari $it siswa" }),
        )
    }
    item(key = "academic_subjects") {
        FactCard(
            "Paling unggul" to (a.strongestSubject ?: MISSING),
            "Perlu ditingkatkan" to (a.improvementNeeded ?: MISSING),
            "Rata-rata rapor" to (a.trend.takeIf { it.isNotEmpty() }?.joinToString(" → ") { idNumber(it) } ?: MISSING),
        )
    }
}

private fun LazyListScope.ibadah(profile: StudentProfile360Data) {
    val i = profile.ibadahSummary
    item(key = "tahfidz") {
        SistaCard(modifier = Modifier.fillMaxWidth()) {
            Text("Tahfidz", style = SistaTheme.typography.titleMedium)
            val juz = juzLabel(i.tahfidzJuzCompleted, i.targetJuz)
            if (juz == null) {
                Text(
                    "Belum ada target hafalan untuk tahun ajaran ini.",
                    style = SistaTheme.typography.bodyMedium,
                    color = SistaTheme.colors.onSurfaceVariant,
                    modifier = Modifier.padding(top = Spacing.xs),
                )
            } else {
                Text(juz, style = SistaTheme.typography.headlineSmall, color = SistaTheme.colors.primary, modifier = Modifier.padding(top = Spacing.xs))
                i.tahfidzProgressPercent?.let {
                    LinearProgressIndicator(progress = { it / 100f }, modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.sm))
                }
            }
            Text(
                "Setoran terakhir: ${i.currentSurah ?: MISSING}",
                style = SistaTheme.typography.bodySmall,
                color = SistaTheme.colors.onSurfaceVariant,
            )
        }
    }
    item(key = "ibadah_tiles") {
        TileRow(
            Triple("Sholat berjamaah", percentLabel(i.sholatJamaahPercent), "Dari sholat yang tercatat, 30 hari"),
            Triple("Mutaba'ah beruntun", streakLabel(i.mutabaahStreakDays), "Hari berturut-turut"),
        )
    }
}

private fun LazyListScope.discipline(profile: StudentProfile360Data) {
    val d = profile.disciplineSummary
    item(key = "discipline_status") {
        SistaCard(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Status", style = SistaTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                d.category?.let { StatusPill(it, if (it == "Aman") StatusTone.Success else StatusTone.Warning) } ?: Text(MISSING)
            }
            Text(
                "Sanksi aktif: ${d.activeSanctions ?: MISSING}",
                style = SistaTheme.typography.bodyMedium,
                color = SistaTheme.colors.onSurfaceVariant,
                modifier = Modifier.padding(top = Spacing.sm),
            )
        }
    }
    item(key = "discipline_tiles") {
        TileRow(
            Triple("Poin prestasi", signedPoints(d.totalPositivePoints, "+"), null),
            Triple("Poin pelanggaran", signedPoints(d.totalViolationPoints, "−"), null),
        )
    }
}

private fun LazyListScope.activities(profile: StudentProfile360Data) {
    item(key = "achievements_title") { Text("Prestasi", style = SistaTheme.typography.titleMedium, modifier = Modifier.padding(top = Spacing.sm)) }
    if (profile.achievementList.isEmpty()) {
        item(key = "achievements_empty") { EmptyState(title = "Belum ada prestasi tervalidasi", icon = Icons.Outlined.EmojiEvents) }
    }
    items(profile.achievementList, key = { "ach_${it.title}_${it.year}" }) { a ->
        SistaCard(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBadge(Icons.Outlined.EmojiEvents, tone = StatusTone.Warning)
                Spacer(Modifier.width(Spacing.md))
                Column(Modifier.weight(1f)) {
                    Text(a.title, style = SistaTheme.typography.titleSmall)
                    Text(
                        listOfNotNull(a.level?.let { "Tingkat $it" }, a.category, a.year?.toString()).joinToString(" · ").ifEmpty { MISSING },
                        style = SistaTheme.typography.bodySmall,
                        color = SistaTheme.colors.onSurfaceVariant,
                    )
                }
            }
        }
    }
    item(key = "ekskul_title") { Text("Ekstrakurikuler", style = SistaTheme.typography.titleMedium, modifier = Modifier.padding(top = Spacing.md)) }
    if (profile.extracurricularList.isEmpty()) {
        item(key = "ekskul_empty") { EmptyState(title = "Belum terdaftar di ekstrakurikuler", icon = Icons.Outlined.Groups) }
    }
    items(profile.extracurricularList, key = { "eks_${it.name}" }) { e ->
        SistaCard(modifier = Modifier.fillMaxWidth()) {
            Text(e.name, style = SistaTheme.typography.titleSmall)
            Text(
                listOfNotNull(e.role, e.joinedYear?.let { "sejak $it" }).joinToString(" · ").ifEmpty { MISSING },
                style = SistaTheme.typography.bodySmall,
                color = SistaTheme.colors.onSurfaceVariant,
            )
        }
    }
}

private fun LazyListScope.health(profile: StudentProfile360Data) {
    val h = profile.healthSummary
    item(key = "health") {
        FactCard(
            "Golongan darah" to (h.bloodType ?: MISSING),
            "Tinggi · berat" to bodyLabel(h.heightCm, h.weightKg),
            "Alergi" to (h.allergies.filter { it.isNotBlank() }.takeIf { it.isNotEmpty() }?.joinToString(", ") ?: "Tidak ada yang tercatat"),
            "Kunjungan UKS" to (h.totalUksVisits?.let { "$it kali" } ?: MISSING),
            "Kunjungan terakhir" to dateLabel(h.lastVisitDate),
        )
    }
}
