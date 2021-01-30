package com.mathewsachin.fategrandautomata.ui.prefs.compose

import androidx.compose.material.Text
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.savedinstancestate.savedInstanceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.preference.MultiSelectListPreference
import com.mathewsachin.fategrandautomata.prefs.core.Pref
import kotlinx.coroutines.flow.onEach

@Composable
fun MultiSelectListPreferenceDialog(
    showDialog: Boolean,
    closeDialog: () -> Unit,
    selected: Set<String>,
    selectedChange: (Set<String>) -> Unit,
    entries: Map<String, String>,
    title: String
) {
    if (showDialog) {
        var tempSelected by savedInstanceState { selected }

        AlertDialog(
            onDismissRequest = closeDialog,
            title = { Text(text = title) },
            text = {
                Column {
                    entries.forEach { current ->
                        val isSelected = tempSelected.contains(current.key)
                        val onSelectionChanged = {
                            val result = when (!isSelected) {
                                true -> tempSelected + current.key
                                false -> tempSelected - current.key
                            }
                            tempSelected = result
                        }
                        Row(Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = isSelected,
                                onClick = { onSelectionChanged() }
                            )
                            .padding(16.dp)
                        ) {
                            Checkbox(checked = isSelected, onCheckedChange = {
                                onSelectionChanged()
                            })
                            Text(
                                text = current.value,
                                style = MaterialTheme.typography.body1.merge(),
                                modifier = Modifier.padding(start = 16.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedChange(tempSelected)
                        closeDialog()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colors.secondary),
                ) {
                    Text(text = stringResource(id = android.R.string.ok))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = closeDialog,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colors.secondary),
                ) {
                    Text(text = stringResource(id = android.R.string.cancel))
                }
            }
        )
    }
}

@Composable
fun Pref<Set<String>>.MultiSelectListPreference(
    title: String,
    summary: (List<String>) -> String = { it.joinToString() },
    singleLineTitle: Boolean = true,
    icon: ImageVector? = null,
    entries: Map<String, String>,
    enabled: Boolean = true,
    hint: String = ""
) {
    val selected by asFlow().collectAsState(get())
    var showDialog by savedInstanceState { false }

    val itemNames = entries
        .filter { selected.contains(it.key) }
        .map { it.value }

    Preference(
        title = title,
        summary = summary(itemNames),
        singleLineTitle = singleLineTitle,
        icon = icon,
        enabled = enabled,
        onClick = { showDialog = true },
        hint = hint
    )

    MultiSelectListPreferenceDialog(
        showDialog = showDialog,
        closeDialog = { showDialog = false },
        selected = selected,
        selectedChange = { set(it) },
        entries = entries,
        title = title
    )
}