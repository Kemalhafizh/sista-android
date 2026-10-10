package com.sultanagung1.sista.ui.home.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Class
import androidx.compose.material.icons.outlined.Grade
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import com.sultanagung1.sista.core.ui.component.ButtonVariant
import com.sultanagung1.sista.core.ui.component.FeatureTile
import com.sultanagung1.sista.core.ui.component.SectionHeader
import com.sultanagung1.sista.core.ui.component.SistaButton
import com.sultanagung1.sista.core.ui.theme.SistaTheme
import com.sultanagung1.sista.core.ui.theme.Spacing
import com.sultanagung1.sista.data.repository.UsageRanking
import com.sultanagung1.sista.feature.home.R
import com.sultanagung1.sista.ui.navigation.Screen

/** A shortcut on Home. [key] is the server feature it opens, also the usage-counter key. */
@Immutable
data class QuickItem(
    val key: String,
    @StringRes val title: Int,
    val icon: ImageVector,
    val route: String,
    val hints: List<ImageVector> = emptyList(),
)

/** A student's everyday shortcuts, in their default order. Each is shown only when the account has it. */
val STUDENT_QUICK_ITEMS = listOf(
    QuickItem("attendance.class_scan", R.string.quick_class_qr, Icons.Outlined.QrCodeScanner, Screen.StudentSessionQrScan.route, listOf(Icons.Outlined.CameraAlt)),
    QuickItem("student.grades", R.string.quick_grades, Icons.Outlined.Grade, Screen.Grades.route),
    QuickItem("student.cbt", R.string.quick_cbt, Icons.Outlined.Quiz, Screen.CbtList.route),
    QuickItem("student.billing", R.string.quick_billing, Icons.Outlined.AccountBalanceWallet, Screen.Billing.route),
    QuickItem("student.elearning", R.string.quick_elearning, Icons.Outlined.Class, Screen.ElearningClassList.route),
    QuickItem("student.mutabaah", R.string.quick_mutabaah, Icons.Outlined.SelfImprovement, Screen.Mutabaah.route),
    QuickItem("student.rapor", R.string.quick_rapor, Icons.Outlined.Assignment, Screen.RaporDetail.route),
)

/** How many shortcuts Home shows; everything else lives in Layanan. */
const val QUICK_ACCESS_LIMIT = 6

/**
 * The account's shortcuts. Once this phone has opened them often enough,
 * the most used come first — a plain tap counter kept on the device, and
 * labelled as such ("Sering Dipakai"), with a way to reset it.
 */
@Composable
fun HomeQuickAccess(
    available: List<QuickItem>,
    usage: Map<String, Int>,
    onOpen: (QuickItem) -> Unit,
    onOpenAllServices: () -> Unit,
    onResetUsage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (available.isEmpty()) return
    val ranked = UsageRanking.rank(available, usage) { it.key }
    val personalised = ranked != available
    val shown = ranked.take(QUICK_ACCESS_LIMIT)

    Column(modifier.padding(horizontal = Spacing.screen), verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
        SectionHeader(
            title = stringResource(if (personalised) R.string.home_frequent else R.string.home_quick_access),
            actionLabel = stringResource(R.string.home_all_services),
            onAction = onOpenAllServices,
        )
        shown.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                row.forEach { item ->
                    FeatureTile(
                        title = stringResource(item.title),
                        icon = item.icon,
                        hints = item.hints,
                        onClick = { onOpen(item) },
                        modifier = Modifier.weight(1f),
                    )
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
        if (personalised) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.home_usage_note),
                    style = SistaTheme.typography.bodySmall,
                    color = SistaTheme.colors.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                SistaButton(stringResource(R.string.home_usage_reset), onResetUsage, variant = ButtonVariant.Text)
            }
        }
    }
}
