package com.mathewsachin.fategrandautomata.ui.prefs

import androidx.compose.material3.Text
import androidx.compose.runtime.*
import com.mathewsachin.fategrandautomata.prefs.core.Pref
import com.mathewsachin.fategrandautomata.ui.FgaDialog
import com.mathewsachin.fategrandautomata.ui.VectorIcon
import com.mathewsachin.fategrandautomata.ui.multiChoiceList

@Composable
fun <T> multiSelectListDialog(
    selected: Set<T>,
    onSelectedChange: (Set<T>) -> Unit,
    entries: Map<T, String>,
    title: String
): FgaDialog {
    val dialog = FgaDialog()

    dialog.build {
        title(text = title)

        var current by remember(selected) { mutableStateOf(selected) }

        multiChoiceList(
            selected = current,
            onSelectedChange = { current = it },
            items = entries.keys.toList()
        ) {
            Text(entries[it] ?: "--")
        }

        buttons(
            onSubmit = { onSelectedChange(current) }
        )
    }

    return dialog
}

@Composable
fun <T> Pref<Set<T>>.MultiSelectListPreference(
    title: String,
    summary: @Composable (List<String>) -> String = { it.joinToString() },
    singleLineTitle: Boolean = true,
    icon: VectorIcon? = null,
    entries: Map<T, String>,
    enabled: Boolean = true,
    trailing: @Composable (() -> Unit)? = null
) {
    var selected by remember()

    val selectedItemNames = selected
        .map { entries.getOrDefault(it, it.toString()) }

    val dialog = multiSelectListDialog(
        selected = selected,
        onSelectedChange = { selected = it },
        entries = entries,
        title = title
    )

    Preference(
        title = title,
        summary = summary(selectedItemNames),
        singleLineTitle = singleLineTitle,
        icon = icon,
        enabled = enabled,
        onClick = { dialog.show() },
        trailing = trailing
    )
}