package com.mathewsachin.fategrandautomata.ui.prefs.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.mathewsachin.fategrandautomata.prefs.core.Pref
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.listItemsMultiChoice

@Composable
fun multiSelectListDialog(
    selected: Set<String>,
    selectedChange: (Set<String>) -> Unit,
    entries: Map<String, String>,
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
fun Pref<Set<String>>.MultiSelectListPreference(
    title: String,
    summary: (List<String>) -> String = { it.joinToString() },
    singleLineTitle: Boolean = true,
    icon: ImageVector? = null,
    entries: Map<String, String>,
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