package com.sultanagung1.sista.core.designsystem

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle

@Composable
fun AnimatedCounter(
    count: Int,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.headlineMedium,
    prefix: String = "",
    suffix: String = ""
) {
    var oldCount by remember { mutableStateOf(count) }
    SideEffect { oldCount = count }
    Row(modifier = modifier) {
        if (prefix.isNotEmpty()) Text(text = prefix, style = style)
        val countString = count.toString()
        val oldCountString = oldCount.toString()
        for (i in countString.indices) {
            val oldChar = oldCountString.getOrNull(i)
            val newChar = countString[i]
            val char = if (oldChar == newChar) oldCountString[i] else countString[i]
            AnimatedContent(
                targetState = char,
                transitionSpec = { slideInVertically { it } togetherWith slideOutVertically { -it } },
                label = "counterDigitAnim"
            ) { charState ->
                Text(text = charState.toString(), style = style)
            }
        }
        if (suffix.isNotEmpty()) Text(text = suffix, style = style)
    }
}
