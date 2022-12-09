package com.mathewsachin.fategrandautomata.ui.prefs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ListItem
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mathewsachin.fategrandautomata.prefs.core.Pref
import com.mathewsachin.fategrandautomata.ui.DimmedIcon
import com.mathewsachin.fategrandautomata.ui.FGAListItemColors
import com.mathewsachin.fategrandautomata.ui.VectorIcon
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach

@Composable
fun <T> Pref<T>.remember(): MutableState<T> {
    var state by remember { mutableStateOf(defaultValue) }

    LaunchedEffect(this) {
        asFlow()
            .onEach { state = it }
            .collect()
    }

    return object : MutableState<T> {
        override var value: T
            get() = state
            set(value) {
                state = value
                set(value)
            }

        override fun component1() = value
        override fun component2(): (T) -> Unit = { value = it }
    }
}

@Composable
fun Preference(
    title: String,
    modifier: Modifier = Modifier,
    summary: String = "",
    singleLineTitle: Boolean = false,
    icon: VectorIcon? = null,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    Preference(
        title = {
            StatusWrapper(enabled) {
                Text(text = title, maxLines = if (singleLineTitle) 1 else Int.MAX_VALUE)
            }
        },
        summary = if (summary.isNotBlank()) {
            {
                StatusWrapper(enabled) {
                    Text(text = summary, style = MaterialTheme.typography.bodySmall)
                }
            }
        } else null,
        icon = icon,
        enabled = enabled,
        onClick = onClick,
        trailing = trailing,
        modifier = modifier
    )
}

@Composable
fun Preference(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    summary: @Composable (() -> Unit)? = null,
    icon: VectorIcon? = null,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    StatusWrapper(enabled) {
        ListItem(
            headlineText = title,
            supportingText = summary,
            colors = FGAListItemColors(),
            leadingContent = icon?.let {
                {
                    DimmedIcon(
                        it,
                        contentDescription = "icon",
                        modifier = Modifier
                            .size(40.dp)
                            .padding(7.dp)
                    )
                }
            },
            modifier = modifier.clickable(
                enabled = onClick != null && enabled,
                onClick = { onClick?.invoke() }
            ),
            trailingContent = trailing
        )
    }
}

@Composable
fun StatusWrapper(enabled: Boolean, content: @Composable () -> Unit) {
    if (!enabled) {
        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)) {
            content()
        }
    } else content()
}