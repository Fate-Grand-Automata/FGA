package io.github.fate_grand_automata.ui.prefs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults.cardColors
import androidx.compose.material3.CardDefaults.cardElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.fate_grand_automata.prefs.core.Pref
import io.github.fate_grand_automata.ui.VectorIcon
import io.github.fate_grand_automata.util.toggle

@Composable
fun ChipPreferenceItem(
    text: String,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    StatusWrapper(enabled) {
        Card(
            colors = cardColors(
                containerColor = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant,
                contentColor = if (isSelected) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant
            ),
            elevation = cardElevation(2.dp),
            modifier = modifier
                .defaultMinSize(minWidth = 30.dp),
            onClick = onSelect,
            enabled = enabled
        ) {
            Text(
                text,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .padding(5.dp, 2.dp)
                    .align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
fun <T> SingleSelectChip(
    title: String,
    selected: T,
    onSelectedChange: (T) -> Unit,
    modifier: Modifier = Modifier,
    icon: VectorIcon? = null,
    entries: Map<T, String> = emptyMap(),
    enabled: Boolean = true,
    singleRow: Boolean = false
) {
    Preference(
        title = { Text(title) },
        summary = {
            if (singleRow) {
                // The chips share the width evenly instead of wrapping, so a narrow
                // column or a long translation can't push one of them onto its own line.
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    entries.forEach { (key, value) ->
                        ChipPreferenceItem(
                            text = value,
                            isSelected = key == selected,
                            onSelect = { onSelectedChange(key) },
                            enabled = enabled,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            } else {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(0.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    entries.forEach { (key, value) ->
                        ChipPreferenceItem(
                            text = value,
                            isSelected = key == selected,
                            onSelect = { onSelectedChange(key) },
                            enabled = enabled
                        )
                    }
                }
            }
        },
        icon = icon,
        enabled = enabled,
        modifier = modifier
    )
}

@Composable
fun <T> Pref<T>.SingleSelectChipPreference(
    title: String,
    modifier: Modifier = Modifier,
    icon: VectorIcon? = null,
    entries: Map<T, String> = emptyMap(),
    enabled: Boolean = true,
    singleRow: Boolean = false
) {
    var selected by remember()

    SingleSelectChip(
        title = title,
        selected = selected,
        onSelectedChange = { selected = it },
        icon = icon,
        entries = entries,
        enabled = enabled,
        modifier = modifier,
        singleRow = singleRow
    )
}

@Composable
fun <T> MultiSelectChip(
    title: String,
    selected: Set<T>,
    onSelectedChange: (Set<T>) -> Unit,
    modifier: Modifier = Modifier,
    icon: VectorIcon? = null,
    entries: Map<T, String> = emptyMap(),
    enabled: Boolean = true
) {
    Preference(
        title = { Text(title) },
        summary = {
            FlowRow(
                modifier = Modifier.fillMaxWidth()
            ) {
                entries.forEach { (key, value) ->
                    ChipPreferenceItem(
                        text = value,
                        isSelected = key in selected,
                        onSelect = { onSelectedChange(selected.toggle(key)) }
                    )
                }
            }
        },
        icon = icon,
        enabled = enabled,
        modifier = modifier
    )
}

@Composable
fun <T> Pref<Set<T>>.MultiSelectChipPreference(
    title: String,
    modifier: Modifier = Modifier,
    icon: VectorIcon? = null,
    entries: Map<T, String> = emptyMap(),
    enabled: Boolean = true
) {
    var selected by remember()

    MultiSelectChip(
        title = title,
        selected = selected,
        onSelectedChange = { selected = it },
        icon = icon,
        entries = entries,
        enabled = enabled,
        modifier = modifier
    )
}