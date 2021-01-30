package com.mathewsachin.fategrandautomata.ui.prefs.compose

import androidx.compose.material.Text
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.AlertDialog
import androidx.compose.material.MaterialTheme
import androidx.compose.material.RadioButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.savedinstancestate.savedInstanceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.mathewsachin.fategrandautomata.prefs.core.Pref

@Composable
fun Pref<String>.ListPreference(
    title: String,
    summary: String = "",
    singleLineTitle: Boolean = false,
    icon: ImageVector? = null,
    entries: Map<String, String> = emptyMap(),
    enabled: Boolean = true,
    hint: String = ""
) {
    val selected by asFlow().collectAsState(get())
    var showDialog by savedInstanceState { false }
    val closeDialog = { showDialog = false }

    Preference(
        title = title,
        summary = entries[selected] ?: summary,
        singleLineTitle = singleLineTitle,
        icon = icon,
        enabled = enabled,
        hint = hint,
        onClick = { showDialog = true },
    )

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { closeDialog() },
            title = { Text(text = title) },
            text = {
                Column {
                    entries.forEach { current ->
                        val isSelected = selected == current.key
                        val onSelected = {
                            set(current.key)
                            closeDialog()
                        }
                        Row(Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = isSelected,
                                onClick = { if (!isSelected) onSelected() }
                            )
                            .padding(16.dp)
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { if (!isSelected) onSelected() }
                            )
                            Text(
                                text = current.value,
                                style = MaterialTheme.typography.body1.merge(),
                                modifier = Modifier.padding(start = 16.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = { }
        )
    }
}