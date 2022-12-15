package com.mathewsachin.fategrandautomata.ui.card_priority

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.scripts.enums.BraveChainEnum
import com.mathewsachin.fategrandautomata.ui.PreventRtl

@Composable
fun CardPriorityWaveSelector(
    items: SnapshotStateList<CardPriorityListItem>,
    selectedWave: Int,
    onSelectedWaveChange: (Int) -> Unit
) {
    PreventRtl {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
        ) {
            LazyRow(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1f)
            ) {
                items(items.size) { index ->
                    val isSelected = selectedWave == index

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(end = 5.dp)
                            .background(
                                color = if (isSelected) MaterialTheme.colorScheme.secondary else Color.Transparent,
                                shape = MaterialTheme.shapes.medium
                            )
                            .clickable { onSelectedWaveChange(index) }
                    ) {
                        Text(
                            stringResource(R.string.card_priority_wave_number, index + 1),
                            color = if (isSelected) MaterialTheme.colorScheme.onSecondary else Color.Unspecified,
                            modifier = Modifier.padding(5.dp, 2.dp)
                        )

                        AnimatedVisibility(index > 0 && index == items.lastIndex) {
                            Box(
                                modifier = Modifier
                                    .clickable {
                                        if (items.size > 1) {
                                            if (selectedWave == items.lastIndex) {
                                                onSelectedWaveChange(items.lastIndex - 1)
                                            }
                                            items.removeLast()
                                        }
                                    }
                            ) {
                                Icon(
                                    painterResource(R.drawable.ic_close),
                                    contentDescription = "Remove wave",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(
                items.size < 3,
                enter = slideInHorizontally(initialOffsetX = { it * 2 }),
                exit = slideOutHorizontally(targetOffsetX = { it * 2 })
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.secondary,
                            shape = CircleShape
                        )
                        .clip(CircleShape)
                        .clickable {
                            if (items.size < 3) {
                                items.add(
                                    CardPriorityListItem(
                                        items[0].scores.toMutableList(),
                                        items[0].servantPriority.toMutableList(),
                                        mutableStateOf(false),
                                        mutableStateOf(BraveChainEnum.None)
                                    )
                                )
                            }
                        }
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add wave",
                        tint = MaterialTheme.colorScheme.onSecondary
                    )
                }
            }
        }
    }
}