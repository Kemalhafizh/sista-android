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

data class LatexShortcut(
    val label: String,
    val latexSnippet: String,
    val description: String
)

val COMMON_LATEX_SHORTCUTS = listOf(
    LatexShortcut("a/b", "\\frac{a}{b}", "Pecahan"),
    LatexShortcut("√x", "\\sqrt{x}", "Akar Kuadrat"),
    LatexShortcut("x²", "x^{2}", "Pangkat"),
    LatexShortcut("x₁", "x_{1}", "Subskrip"),
    LatexShortcut("∫dx", "\\int_{a}^{b} f(x) \\, dx", "Integral"),
    LatexShortcut("∑", "\\sum_{i=1}^{n}", "Sigma Penjumlahan"),
    LatexShortcut("lim", "\\lim_{x \\to 0}", "Limit"),
    LatexShortcut("[Matriks]", "\\begin{pmatrix} a & b \\\\ c & d \\end{pmatrix}", "Matriks 2x2"),
    LatexShortcut("α", "\\alpha", "Alpha"),
    LatexShortcut("β", "\\beta", "Beta"),
    LatexShortcut("θ", "\\theta", "Theta"),
    LatexShortcut("π", "\\pi", "Pi"),
    LatexShortcut("Δ", "\\Delta", "Delta"),
    LatexShortcut("λ", "\\lambda", "Lambda"),
    LatexShortcut("→", "\\rightarrow", "Panah Reaksi"),
    LatexShortcut("⇌", "\\rightleftharpoons", "Reaksi Kesetimbangan"),
    LatexShortcut("±", "\\pm", "Plus Minus"),
    LatexShortcut("∞", "\\infty", "Tak Hingga"),
    LatexShortcut("≤", "\\le", "Kurang dari sama dengan"),
    LatexShortcut("≥", "\\ge", "Lebih dari sama dengan"),
    LatexShortcut("°", "^{\\circ}", "Derajat")
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
                text = "Pintasan Rumus Saintek (LaTeX):",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Emerald900
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "(Klik untuk menyisipkan ke kursor)",
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
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 1.dp,
                    shadowElevation = 0.5.dp,
                    modifier = Modifier.clickable {
                        onInsertSnippet(item.latexSnippet)
                    }
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
