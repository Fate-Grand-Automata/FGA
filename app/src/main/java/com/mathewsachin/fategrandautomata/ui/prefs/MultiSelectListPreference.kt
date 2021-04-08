package com.mathewsachin.fategrandautomata.ui.prefs

import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.mathewsachin.fategrandautomata.prefs.core.Pref
import com.mathewsachin.fategrandautomata.ui.VectorIcon
import com.mathewsachin.fategrandautomata.ui.battle_config_item.FgaDialog
import com.mathewsachin.fategrandautomata.ui.battle_config_item.multiChoiceList

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
    hint: String = "",
    trailing: @Composable ((Modifier) -> Unit)? = null
) {
    var selected by remember()

    val itemNames = entries
        .filter { selected.contains(it.key) }
        .map { it.value }

    val dialog = multiSelectListDialog(
        selected = selected,
        onSelectedChange = { selected = it },
        entries = entries,
        title = title
    )

    Preference(
        title = title,
        summary = summary(itemNames),
        singleLineTitle = singleLineTitle,
        icon = icon,
        enabled = enabled,
        onClick = { dialog.show() },
        hint = hint,
        trailing = trailing
    )
}