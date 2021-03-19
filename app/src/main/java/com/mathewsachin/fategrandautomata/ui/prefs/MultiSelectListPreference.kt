package com.mathewsachin.fategrandautomata.ui.prefs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.mathewsachin.fategrandautomata.prefs.core.Pref
import com.mathewsachin.fategrandautomata.ui.VectorIcon
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.listItemsMultiChoice

@Composable
fun <T> multiSelectListDialog(
    selected: Set<T>,
    selectedChange: (Set<T>) -> Unit,
    entries: Map<T, String>,
    title: String
): MaterialDialog {
    val dialog = MaterialDialog()

    dialog.build {
        title(text = title)

        val keys = entries.keys.toList()
        val values = entries.values.toList()
        val selectedIndices = selected.map { keys.indexOf(it) }

        listItemsMultiChoice(
            list = values,
            initialSelection = selectedIndices,
            onCheckedChange = { indices ->
                val selectedKeys = indices
                    .map { keys[it] }
                    .toSet()

                selectedChange(selectedKeys)
            }
        )

        buttons {
            positiveButton(res = android.R.string.ok)
            negativeButton(res = android.R.string.cancel)
        }
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
    val selected by collect()

    val itemNames = entries
        .filter { selected.contains(it.key) }
        .map { it.value }

    val dialog = multiSelectListDialog(
        selected = selected,
        selectedChange = { set(it) },
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