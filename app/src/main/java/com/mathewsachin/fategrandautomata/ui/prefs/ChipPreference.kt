package com.mathewsachin.fategrandautomata.ui.prefs

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mathewsachin.fategrandautomata.prefs.core.Pref
import com.mathewsachin.fategrandautomata.ui.VectorIcon
import com.mathewsachin.fategrandautomata.util.toggle

@Composable
fun ChipPreferenceItem(
    text: String,
    isSelected: Boolean,
    onSelect: () -> Unit,
    enabled: Boolean = true
) {
    StatusWrapper(enabled) {
        Card(
            backgroundColor = if (isSelected) MaterialTheme.colors.secondary else MaterialTheme.colors.surface,
            contentColor = if (isSelected) MaterialTheme.colors.onSecondary else MaterialTheme.colors.onSurface,
            elevation = 2.dp,
            modifier = Modifier
                .padding(vertical = 5.dp)
                .padding(end = 5.dp),
            onClick = onSelect,
            enabled = enabled
        ) {
            Text(
                text,
                style = MaterialTheme.typography.caption,
                modifier = Modifier
                    .padding(5.dp, 2.dp)
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
    enabled: Boolean = true
) {
    Preference(
        title = { Text(title) },
        summary = {
            LazyRow(
                modifier = Modifier.fillMaxWidth()
            ) {
                items(entries.toList()) { (key, value) ->
                    ChipPreferenceItem(
                        text = value,
                        isSelected = key == selected,
                        onSelect = { onSelectedChange(key) },
                        enabled = enabled
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
fun <T> Pref<T>.SingleSelectChipPreference(
    title: String,
    modifier: Modifier = Modifier,
    icon: VectorIcon? = null,
    entries: Map<T, String> = emptyMap(),
    enabled: Boolean = true
) {
    var selected by remember()

    SingleSelectChip(
        title = title,
        selected = selected,
        onSelectedChange = { selected = it },
        icon = icon,
        entries = entries,
        enabled = enabled,
        modifier = modifier
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
            LazyRow(
                modifier = Modifier.fillMaxWidth()
            ) {
                items(entries.toList()) { (key, value) ->
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