package com.sultanagung1.sista.ui.cbt.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sultanagung1.sista.core.designsystem.Emerald700
import com.sultanagung1.sista.core.designsystem.Emerald900
import androidx.annotation.StringRes
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.sultanagung1.sista.core.designsystem.R

data class LatexShortcut(
    val label: String,
    val latexSnippet: String,
    /** What the button inserts, read out by TalkBack. */
    @StringRes val description: Int
)

val COMMON_LATEX_SHORTCUTS = listOf(
    LatexShortcut("a/b", "\\frac{a}{b}", R.string.lx_fraction),
    LatexShortcut("√x", "\\sqrt{x}", R.string.lx_sqrt),
    LatexShortcut("x²", "x^{2}", R.string.lx_power),
    LatexShortcut("x₁", "x_{1}", R.string.lx_subscript),
    LatexShortcut("∫dx", "\\int_{a}^{b} f(x) \\, dx", R.string.lx_integral),
    LatexShortcut("∑", "\\sum_{i=1}^{n}", R.string.lx_sum),
    LatexShortcut("lim", "\\lim_{x \\to 0}", R.string.lx_limit),
    LatexShortcut("[2×2]", "\\begin{pmatrix} a & b \\\\ c & d \\end{pmatrix}", R.string.lx_matrix),
    LatexShortcut("α", "\\alpha", R.string.lx_alpha),
    LatexShortcut("β", "\\beta", R.string.lx_beta),
    LatexShortcut("θ", "\\theta", R.string.lx_theta),
    LatexShortcut("π", "\\pi", R.string.lx_pi),
    LatexShortcut("Δ", "\\Delta", R.string.lx_delta),
    LatexShortcut("λ", "\\lambda", R.string.lx_lambda),
    LatexShortcut("→", "\\rightarrow", R.string.lx_arrow),
    LatexShortcut("⇌", "\\rightleftharpoons", R.string.lx_equilibrium),
    LatexShortcut("±", "\\pm", R.string.lx_plusminus),
    LatexShortcut("∞", "\\infty", R.string.lx_infinity),
    LatexShortcut("≤", "\\le", R.string.lx_le),
    LatexShortcut("≥", "\\ge", R.string.lx_ge),
    LatexShortcut("°", "^{\\circ}", R.string.lx_degree)
)

/**
 * CbtLatexToolbar
 *
 * Bilah pintasan formula LaTeX saintek bagi Guru saat membuat/mengetik soal CBT.
 */
@Composable
fun CbtLatexToolbar(
    onInsertSnippet: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(vertical = 6.dp, horizontal = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Functions,
                contentDescription = null,
                tint = Emerald700,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = stringResource(R.string.lx_title),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Emerald900
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = stringResource(R.string.lx_hint),
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(horizontal = 2.dp)
        ) {
            items(COMMON_LATEX_SHORTCUTS) { item ->
                val description = stringResource(item.description)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 1.dp,
                    shadowElevation = 0.5.dp,
                    modifier = Modifier
                        .semantics { contentDescription = description }
                        .clickable { onInsertSnippet(item.latexSnippet) }
                ) {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}
