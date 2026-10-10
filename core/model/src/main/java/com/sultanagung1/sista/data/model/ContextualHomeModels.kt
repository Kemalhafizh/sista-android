package com.sultanagung1.sista.data.model

import com.google.gson.annotations.SerializedName

data class ContextualHomeResponse(
    @SerializedName("success") val success: Boolean = true,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: ContextualHomePayload? = null
)

data class ContextualHomePayload(
    @SerializedName("context") val context: HomeContext,
    @SerializedName("hero") val hero: HeroCard,
    @SerializedName("quickActions") val quickActions: List<QuickAction> = emptyList(),
    @SerializedName("suggestions") val suggestions: List<SmartSuggestion> = emptyList(),
)

data class HomeContext(
    @SerializedName("timeOfDay") val timeOfDay: String,
    @SerializedName("isExamSeason") val isExamSeason: Boolean,
    @SerializedName("activeEvent") val activeEvent: String?,
    @SerializedName("hijriDate") val hijriDate: String
)

data class HeroCard(
    @SerializedName("type") val type: String,
    @SerializedName("title") val title: String,
    @SerializedName("subtitle") val subtitle: String,
    @SerializedName("gradientStart") val gradientStart: String,
    @SerializedName("gradientEnd") val gradientEnd: String,
    @SerializedName("iconEmoji") val iconEmoji: String,
    @SerializedName("actionLabel") val actionLabel: String,
    @SerializedName("actionRoute") val actionRoute: String
)

data class QuickAction(
    @SerializedName("id") val id: String,
    @SerializedName("label") val label: String,
    @SerializedName("iconUrl") val iconUrl: String,
    @SerializedName("route") val route: String,
    @SerializedName("priority") val priority: Int
)

data class SmartSuggestion(
    @SerializedName("id") val id: String,
    @SerializedName("type") val type: String,
    @SerializedName("message") val message: String,
    @SerializedName("ctaText") val ctaText: String,
    @SerializedName("ctaRoute") val ctaRoute: String
)

data class SmartSuggestionsResponse(
    @SerializedName("success") val success: Boolean = true,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: List<SmartSuggestion> = emptyList()
)
