package com.sultanagung1.sista.ui

import org.junit.Assert.*
import org.junit.Test
import java.io.File

/**
 * The billing screen shows the school's records and nothing invented. It used
 * to offer a "virtual account" that the server made up from a bank prefix and
 * the NIS (no bank ever knew it), and an "official receipt" whose paid date
 * was the due date. The earlier version of this test pinned that screen's
 * look (Slate50, 0.5dp borders); the screen is now built from :core:ui.
 */
class BillingUiOverhaulTest {

    private fun findSourceFile(relativePath: String): File {
        val candidates = listOf(
            File(relativePath),
            File("app/$relativePath"),
            File("../$relativePath")
        )
        return candidates.firstOrNull { it.exists() }
            ?: throw IllegalStateException("Cannot locate $relativePath in any candidate paths")
    }

    private val screen = "feature/finance/src/main/java/com/sultanagung1/sista/ui/finance/BillingScreen.kt"
    private val viewModel = "feature/finance/src/main/java/com/sultanagung1/sista/ui/finance/BillingViewModel.kt"

    @Test
    fun noInventedPaymentChannelOrReceipt() {
        val code = findSourceFile(screen).readText() + findSourceFile(viewModel).readText()
        for (forbidden in listOf("requestPaymentVa", "vaNumber", "Virtual Account", "KUITANSI", "Kwitansi", "receiptNumber")) {
            assertFalse("BillingScreen still has $forbidden", code.contains(forbidden))
        }
    }

    @Test
    fun builtFromCoreUiWithoutHardcodedColours() {
        val content = findSourceFile(screen).readText()
        assertTrue(content.contains("com.sultanagung1.sista.core.ui.component.SistaCard"))
        assertFalse(content.contains("com.sultanagung1.sista.core.designsystem"))
        val hardcodedRegex = Regex("""Color\s*\(\s*0x[0-9a-fA-F]{6,8}\s*\)""")
        assertFalse("BillingScreen must not contain hardcoded Color(0x...)", hardcodedRegex.containsMatchIn(content))
    }
}
