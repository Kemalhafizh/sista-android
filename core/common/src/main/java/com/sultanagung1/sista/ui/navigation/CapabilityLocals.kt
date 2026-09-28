package com.sultanagung1.sista.ui.navigation

import androidx.compose.runtime.compositionLocalOf
import com.sultanagung1.sista.data.model.CapabilityState
import com.sultanagung1.sista.data.model.GateDecision

/**
 * The server's capability list for the signed-in account, provided once by
 * the app shell. Screens that offer shortcuts to other features read it to
 * show only what the account may open.
 */
val LocalCapabilityState = compositionLocalOf<CapabilityState> { CapabilityState.Loading }

/** True when [route] would open for the current account (see FeatureCatalog). */
fun CapabilityState.canOpen(route: String): Boolean =
    FeatureCatalog.decide(this, routePatternOf(route)) == GateDecision.OPEN

/**
 * A concrete route ("child_detail/42") back to the pattern it was declared
 * with ("child_detail/{studentId}"), so shortcuts can be checked before
 * navigating. Unknown routes come back unchanged (and are shut).
 */
fun routePatternOf(route: String): String {
    val base = route.substringBefore('?')
    val all = FeatureCatalog.OPEN + FeatureCatalog.REQUIRES.keys
    if (route in all) return route
    return all.firstOrNull { pattern ->
        val patternBase = pattern.substringBefore('?')
        val a = patternBase.split('/')
        val b = base.split('/')
        a.size == b.size && a.zip(b).all { (p, v) -> p == v || (p.startsWith('{') && p.endsWith('}')) }
    } ?: route
}
