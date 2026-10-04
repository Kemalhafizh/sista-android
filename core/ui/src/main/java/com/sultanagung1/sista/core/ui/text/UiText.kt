package com.sultanagung1.sista.core.ui.text

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * Text for the screen that is resolved in the app's language when shown, so
 * ViewModels and formatting functions don't hold Indonesian strings.
 * [Raw] is text that is already final: the server's own message (the server
 * answers in the app's language) or a name from the database.
 */
sealed interface UiText {
    data class Raw(val value: String) : UiText

    /** A string resource; [args] may themselves be [UiText]. */
    class Res(@StringRes val id: Int, vararg val args: Any) : UiText {
        override fun equals(other: Any?): Boolean = other is Res && other.id == id && other.args.contentEquals(args)
        override fun hashCode(): Int = 31 * id + args.contentHashCode()
        override fun toString(): String = "Res($id, ${args.toList()})"
    }

    fun resolve(context: Context): String = when (this) {
        is Raw -> value
        is Res -> context.getString(id, *args.map { if (it is UiText) it.resolve(context) else it }.toTypedArray())
    }

    @Composable
    fun asString(): String = resolve(LocalContext.current)
}

fun String.asUiText(): UiText = UiText.Raw(this)
