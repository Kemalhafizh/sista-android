package com.sultanagung1.sista.data.repository

/**
 * FASE 76.4: frequency ordering for Home, stated plainly. It is a tap counter
 * kept on this device, not a model, so the UI calls it "Sering Dipakai" and
 * never "direkomendasikan AI". Pure Kotlin so the rules are unit-tested.
 */
object UsageRanking {

    /**
     * Below this many taps across the ranked items the default order is kept.
     * Otherwise a single tap would reshuffle Home, and a layout that moves
     * under the thumb is worse than one that never adapts.
     */
    const val MIN_TOTAL_TAPS = 5

    /** A service must have been opened at least this often to count as "sering". */
    const val MIN_TAPS_FOR_FREQUENT = 3

    /**
     * Most-used first. Stable: equal counts keep their default order, so the
     * result is deterministic and an unused item never jumps ahead of another.
     */
    fun <T> rank(defaults: List<T>, counts: Map<String, Int>, keyOf: (T) -> String): List<T> {
        val total = defaults.sumOf { counts[keyOf(it)] ?: 0 }
        if (total < MIN_TOTAL_TAPS) return defaults
        return defaults.withIndex()
            .sortedWith(
                compareByDescending<IndexedValue<T>> { counts[keyOf(it.value)] ?: 0 }
                    .thenBy { it.index }
            )
            .map { it.value }
    }

    /** True when [rank] would change the order, i.e. the UI should say it adapted. */
    fun <T> isReordered(defaults: List<T>, counts: Map<String, Int>, keyOf: (T) -> String): Boolean =
        rank(defaults, counts, keyOf) != defaults

    /**
     * Keys opened at least [MIN_TAPS_FOR_FREQUENT] times, most-used first
     * (ties keep [candidates] order), at most [limit].
     */
    fun frequent(candidates: List<String>, counts: Map<String, Int>, limit: Int = 4): List<String> =
        candidates.withIndex()
            .filter { (counts[it.value] ?: 0) >= MIN_TAPS_FOR_FREQUENT }
            .sortedWith(
                compareByDescending<IndexedValue<String>> { counts[it.value] ?: 0 }
                    .thenBy { it.index }
            )
            .map { it.value }
            .distinct()
            .take(limit)
}
