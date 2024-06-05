package io.github.fate_grand_automata.ui.launcher.battle

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.fate_grand_automata.R
import io.github.fate_grand_automata.scripts.prefs.IBattleConfig
import io.github.fate_grand_automata.ui.scrollbar

@Composable
fun ConfigSelectionList(
    modifier: Modifier = Modifier,
    configs: List<IBattleConfig>,
    selectedConfigIndex: Int,
    onSelectedConfigIndexChange: (Int) -> Unit,
) {

    if (configs.isNotEmpty()) {
        // Scrolling the selected config into view
        val configListState = rememberLazyListState()
        LaunchedEffect(true) {
            if (selectedConfigIndex != -1) {
                configListState.scrollToItem(selectedConfigIndex)
            }
        }

        LazyColumn(
            modifier = modifier
                .scrollbar(
                    state = configListState,
                    hiddenAlpha = 0.3f,
                    horizontal = false,
                    knobColor = MaterialTheme.colorScheme.secondary
                ),
            state = configListState
        ) {
            itemsIndexed(configs) { index, item ->
                BattleConfigItem(
                    name = item.name,
                    isSelected = selectedConfigIndex == index,
                    onSelected = {
                        onSelectedConfigIndexChange(index)
                    },
                )
            }
        }
    } else {
        Text(
            stringResource(R.string.battle_config_list_no_items),
            modifier = modifier
                .padding(16.dp)
        )
    }
}

@Composable
private fun BattleConfigItem(
    name: String,
    isSelected: Boolean,
    onSelected: () -> Unit,
) {
    Row(
        modifier = Modifier
            .padding(3.dp)
            .background(
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = MaterialTheme.shapes.medium
            )
            .clickable(onClick = onSelected)
            .padding(11.dp, 3.dp)
            .fillMaxWidth()
    ) {
        Text(
            name,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else Color.Unspecified
        )
    }
}