package io.github.fate_grand_automata.ui.prefs

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import io.github.fate_grand_automata.prefs.core.Pref
import io.github.fate_grand_automata.ui.FgaDialog
import io.github.fate_grand_automata.ui.VectorIcon
import io.github.fate_grand_automata.ui.singleChoiceList

@Composable
fun <T> listDialog(
    selected: T,
    onSelectedChange: (T) -> Unit,
    entries: Map<T, String>,
    title: String
): FgaDialog {
    val dialog = FgaDialog()

    dialog.build {
        title(text = title)

        singleChoiceList(
            selected = selected,
            onSelectedChange = {
                onSelectedChange(it)
                hide()
            },
            items = entries.keys.toList()
        ) {
            Text(entries[it] ?: "--")
        }
    }

    return dialog
}

@Composable
fun <T> Pref<T>.ListPreference(
    title: String,
    entries: Map<T, String>,
    modifier: Modifier = Modifier,
    summary: String = "",
    singleLineTitle: Boolean = false,
    icon: VectorIcon? = null,
    enabled: Boolean = true
) {
    var selected by remember()

    val dialog = listDialog(
        selected = selected,
        onSelectedChange = { selected = it },
        entries = entries,
        title = title
    )

    Preference(
        title = title,
        summary = entries[selected] ?: summary,
        singleLineTitle = singleLineTitle,
        icon = icon,
        enabled = enabled,
        onClick = { dialog.show() },
        modifier = modifier
    )
}