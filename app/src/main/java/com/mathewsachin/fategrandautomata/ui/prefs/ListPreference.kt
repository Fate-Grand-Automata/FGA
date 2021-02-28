package com.mathewsachin.fategrandautomata.ui.prefs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.painter.Painter
import com.mathewsachin.fategrandautomata.prefs.core.Pref
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.listItemsSingleChoice

@Composable
fun <T> listDialog(
    selected: T,
    selectedChange: (T) -> Unit,
    entries: Map<T, String>,
    title: String
): MaterialDialog {
    val dialog = MaterialDialog()

    dialog.build {
        title(text = title)

        val keys = entries.keys.toList()
        val values = entries.values.toList()
        val selectedIndex = keys.indexOf(selected)

        listItemsSingleChoice(
            list = values,
            initialSelection = selectedIndex,
            onChoiceChange = {
                selectedChange(keys[it])

                hide()
            },
            waitForPositiveButton = false
        )
    }

    return dialog
}

@Composable
fun <T> Pref<T>.ListPreference(
    title: String,
    summary: String = "",
    singleLineTitle: Boolean = false,
    icon: Painter? = null,
    entries: Map<T, String> = emptyMap(),
    enabled: Boolean = true,
    hint: String = ""
) {
    val selected by collect()

    val dialog = listDialog(
        selected = selected,
        selectedChange = { set(it) },
        entries = entries,
        title = title
    )

    Preference(
        title = title,
        summary = entries[selected] ?: summary,
        singleLineTitle = singleLineTitle,
        icon = icon,
        enabled = enabled,
        hint = hint,
        onClick = { dialog.show() },
    )
}