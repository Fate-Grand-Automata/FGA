package com.mathewsachin.fategrandautomata.ui.prefs.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.prefs.core.Pref
import com.vanpra.composematerialdialogs.MaterialDialog

@Composable
fun <T> Pref<T>.collect() =
    remember { asFlow() }.collectAsState(get())

@Composable
fun Preference(
    title: String,
    summary: String = "",
    singleLineTitle: Boolean = false,
    icon: ImageVector? = null,
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
    icon: ImageVector? = null,
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
            icon = icon?.let { { Icon(imageVector = it, modifier = Modifier.size(40.dp)) } },
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
                            imageVector = vectorResource(id = R.drawable.ic_info),
                            modifier = Modifier
                                .size(40.dp)
                                .clickable(onClick = { hintDialog.show() })
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
        Providers(AmbientContentAlpha provides ContentAlpha.disabled) {
            content()
        }
    }
    else content()
}