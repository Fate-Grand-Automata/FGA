package com.mathewsachin.fategrandautomata.ui.prefs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.prefs.core.Pref
import com.mathewsachin.fategrandautomata.ui.VectorIcon
import com.vanpra.composematerialdialogs.MaterialDialog

@Composable
fun <T> Pref<T>.collect() =
    remember { asFlow() }.collectAsState(get())

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
                    Icon(
                        it.asPainter(),
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
                        Icon(
                            painterResource(R.drawable.ic_info),
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