package com.mathewsachin.fategrandautomata.ui.card_priority

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.ui.Heading

@Composable
fun CardPriorityView(
    items: SnapshotStateList<CardPriorityListItem>
) {
    var selectedWave by rememberSaveable { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Heading(stringResource(R.string.p_nav_card_priority))

        CardPriorityWaveSelector(
            items = items,
            selectedWave = selectedWave,
            onSelectedWaveChange = { selectedWave = it }
        )

        Divider()
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp, 5.dp)
                .padding(top = 11.dp)
        ) {
            Text(stringResource(R.string.card_priority_high))
            Text(stringResource(R.string.card_priority_low))
        }

        val selected = items.getOrNull(selectedWave)

        AnimatedContent(
            targetState = selected
        ) {
            it?.Render()
        }
    }
}