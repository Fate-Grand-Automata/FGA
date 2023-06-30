package io.github.fate_grand_automata.ui.pref_support

import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import io.github.fate_grand_automata.R
import io.github.fate_grand_automata.prefs.core.Pref
import io.github.fate_grand_automata.ui.DimmedIcon
import io.github.fate_grand_automata.ui.VectorIcon
import io.github.fate_grand_automata.ui.icon
import io.github.fate_grand_automata.ui.prefs.MultiSelectListPreference
import io.github.fate_grand_automata.ui.prefs.remember

@Composable
fun Pref<Set<String>>.SupportSelectPreference(
    title: String,
    entries: Map<String, String>,
    icon: VectorIcon? = null
) {
    val value by remember()

    MultiSelectListPreference(
        title = title,
        entries = entries,
        icon = icon,
        summary = {
            if (it.isEmpty())
                stringResource(R.string.p_not_set)
            else it.joinToString()
        }
    ) {
        if (value.isNotEmpty()) {
            IconButton(
                onClick = { resetToDefault() }
            ) {
                DimmedIcon(
                    icon(R.drawable.ic_close),
                    contentDescription = "Clear"
                )
            }
        }
    }
}