package com.sultanagung1.sista.widget

import android.content.Context
import androidx.annotation.StringRes

/**
 * One piece of widget text: a string resource (with format args, which may
 * themselves be [WidgetText]) or text that came verbatim from the API, such
 * as a child's name or a subject. Kept free of Context so the
 * snapshot → text mapping can be unit-tested.
 */
sealed interface WidgetText {
    data class Res(@param:StringRes val id: Int, val args: List<Any> = emptyList()) : WidgetText
    data class Raw(val text: String) : WidgetText
}

fun WidgetText.resolve(context: Context): String = when (this) {
    is WidgetText.Raw -> text
    is WidgetText.Res -> context.getString(
        id,
        *args.map { if (it is WidgetText) it.resolve(context) else it }.toTypedArray()
    )
}

data class AttendanceWidgetContent(
    val status: WidgetText,
    val detail: WidgetText?,
    val updated: WidgetText?
)

data class SppWidgetContent(
    val status: WidgetText,
    val detail: WidgetText?,
    val updated: WidgetText?
)

/** [lines] has 1–3 entries, one per TextView in widget_schedule.xml. */
data class ScheduleWidgetContent(
    val count: WidgetText,
    val lines: List<WidgetText>
)
