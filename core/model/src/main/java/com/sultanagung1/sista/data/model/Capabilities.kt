package com.sultanagung1.sista.data.model

import com.google.gson.annotations.SerializedName

/**
 * What the signed-in account may use, as decided by the server
 * (GET api/v1/me/capabilities). The server derives each feature from the
 * route middleware of the endpoint the feature needs, so this list is never
 * wider than what the API will accept. The app builds every menu from it and
 * never from a role it interprets locally.
 */
data class Capabilities(
    @SerializedName("role") val role: String = "",
    @SerializedName("features") val features: List<AppFeature> = emptyList(),
    @SerializedName("groups") val groups: List<FeatureGroup> = emptyList(),
    @SerializedName("version") val version: String = "",
) {
    @Transient
    private var keyCache: Set<String>? = null

    /** Feature keys, for quick look-ups. */
    val keys: Set<String>
        get() = keyCache ?: features.map { it.key }.toSet().also { keyCache = it }

    fun has(key: String): Boolean = key in keys
}

data class AppFeature(
    @SerializedName("key") val key: String = "",
    @SerializedName("title") val title: String = "",
    @SerializedName("group") val group: String = "",
    /** Phone hardware the feature needs: camera, location, microphone, … */
    @SerializedName("hardware") val hardware: List<String> = emptyList(),
)

data class FeatureGroup(
    @SerializedName("key") val key: String = "",
    @SerializedName("title") val title: String = "",
)

/** Where the capability list stands for the current session. */
sealed interface CapabilityState {
    /** Nothing known yet (first launch after login, before the server answers). */
    data object Loading : CapabilityState

    /**
     * A list to act on. [fromCache] when it is the copy saved last time
     * because the server could not be reached; the server still enforces
     * every call, so a stale list can only hide or show a menu entry.
     */
    data class Ready(val capabilities: Capabilities, val fromCache: Boolean = false) : CapabilityState

    /** The server could not be reached and nothing is saved. */
    data class Failed(val message: String) : CapabilityState

    /** Signed out: no features at all. */
    data object SignedOut : CapabilityState
}

/** What a guarded destination should show. */
enum class GateDecision {
    /** The account has the feature (or the destination needs none). */
    OPEN,

    /** The account does not have the feature: show "not available". */
    LOCKED,

    /** Still waiting for the list. */
    WAITING,

    /** The list could not be loaded: show an error with retry. */
    UNKNOWN,
}

object FeatureAccess {

    /**
     * Decide a destination that needs [requiredFeature] (null = open to any
     * signed-in account, such as Profile or Settings).
     */
    fun decide(state: CapabilityState, requiredFeature: String?): GateDecision {
        if (requiredFeature == null) return GateDecision.OPEN
        return when (state) {
            is CapabilityState.Ready ->
                if (state.capabilities.has(requiredFeature)) GateDecision.OPEN else GateDecision.LOCKED
            CapabilityState.Loading -> GateDecision.WAITING
            is CapabilityState.Failed -> GateDecision.UNKNOWN
            CapabilityState.SignedOut -> GateDecision.LOCKED
        }
    }

    /**
     * The features to list, grouped in the server's group order. Groups
     * without any feature for this account are left out; features whose group
     * the server did not describe go last under their raw key.
     */
    fun grouped(capabilities: Capabilities, known: (AppFeature) -> Boolean = { true }): List<Pair<FeatureGroup, List<AppFeature>>> {
        val visible = capabilities.features.filter(known)
        val byGroup = visible.groupBy { it.group }
        val ordered = capabilities.groups.mapNotNull { group ->
            byGroup[group.key]?.takeIf { it.isNotEmpty() }?.let { group to it }
        }
        val described = capabilities.groups.map { it.key }.toSet()
        val rest = byGroup.filterKeys { it !in described }.map { (key, features) -> FeatureGroup(key, key) to features }
        return ordered + rest
    }

    /** The first of [preference] the account has, else null. */
    fun firstAvailable(capabilities: Capabilities, preference: List<String>): String? =
        preference.firstOrNull { capabilities.has(it) }
}
