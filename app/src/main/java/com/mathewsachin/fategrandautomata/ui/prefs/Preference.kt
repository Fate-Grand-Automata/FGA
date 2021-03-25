package com.mathewsachin.fategrandautomata.ui.prefs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.ContentAlpha
import androidx.compose.material.ListItem
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.prefs.core.Pref
import com.mathewsachin.fategrandautomata.ui.DimmedIcon
import com.mathewsachin.fategrandautomata.ui.VectorIcon
import com.mathewsachin.fategrandautomata.ui.icon
import com.vanpra.composematerialdialogs.MaterialDialog
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach

@Composable
fun <T> Pref<T>.remember(): MutableState<T> {
    var state by remember { mutableStateOf(defaultValue) }

    LaunchedEffect(true) {
        asFlow()
            .onEach { state = it }
            .collect()
    }

    return object: MutableState<T> {
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
    summary: String = "",
    singleLineTitle: Boolean = false,
    icon: VectorIcon? = null,
    enabled: Boolean = true,
    hint: String = "",
    onClick: (() -> Unit)? = null,
    trailing: @Composable ((Modifier) -> Unit)? = null
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
                    Text(text = summary)
                }
            }
        } else null,
        icon = icon,
        enabled = enabled,
        hint = hint,
        onClick = onClick,
        trailing = trailing
    )
}

@Composable
fun Preference(
    title: @Composable () -> Unit,
    summary: @Composable (() -> Unit)? = null,
    icon: VectorIcon? = null,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
    hint: String = "",
    trailing: @Composable ((Modifier) -> Unit)? = null
) {
    val hintDialog = MaterialDialog()

    hintDialog.build {
        // TODO: Localize 'Hint'
        title("Hint")

        message(hint)
    }

    StatusWrapper (enabled) {
        ListItem(
            text = title,
            secondaryText = summary,
            icon = icon?.let {
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
            modifier = Modifier.let{
                if (onClick != null)
                    it.clickable(onClick = { if (enabled) onClick() })
                else it
            },
            trailing = {
                Row {
                    trailing?.invoke(Modifier.align(Alignment.CenterVertically))

                    if (hint.isNotBlank()) {
                        DimmedIcon(
                            icon(R.drawable.ic_info),
                            contentDescription = "Hint",
                            modifier = Modifier
                                .size(40.dp)
                                .clickable(onClick = { hintDialog.show() })
                                .padding(7.dp)
                        )
                    }
                }
            }
        )
    }
}

@Composable
fun StatusWrapper(enabled: Boolean, content: @Composable () -> Unit) {
    if (!enabled) {
        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.disabled) {
            content()
        }
    }
    else content()
}