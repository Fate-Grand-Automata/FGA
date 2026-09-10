package io.github.fate_grand_automata.ui.prefs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.fate_grand_automata.prefs.core.Pref
import io.github.fate_grand_automata.ui.FGAMenuContainerColor

/**
 * A single-choice preference that opens a menu anchored to its own row, as opposed to
 * [ListPreference], which opens a full-screen dialog for the same job.
 *
 * The signature matches [ListPreference] so the two are interchangeable at a call site.
 */
@Composable
fun <T> Pref<T>.DropdownPreference(
    title: String,
    entries: Map<T, String>,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    var selected by remember()
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Preference(
            title = { StatusWrapper(enabled) { Text(title) } },
            summary = {
                StatusWrapper(enabled) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            entries[selected] ?: "",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            },
            enabled = enabled,
            onClick = { expanded = true }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = FGAMenuContainerColor()
        ) {
            entries.forEach { (value, label) ->
                DropdownMenuItem(
                    text = { Text(label) },
                    trailingIcon = if (value == selected) {
                        { Icon(Icons.Default.Check, contentDescription = null) }
                    } else null,
                    onClick = {
                        selected = value
                        expanded = false
                    }
                )
            }
        }
    }
}
