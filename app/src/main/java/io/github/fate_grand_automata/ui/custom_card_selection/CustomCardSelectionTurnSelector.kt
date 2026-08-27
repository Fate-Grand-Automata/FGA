package io.github.fate_grand_automata.ui.custom_card_selection

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import io.github.fate_grand_automata.R
import io.github.fate_grand_automata.ui.PreventRtl

@Composable
fun CustomCardSelectionTurnSelector(
    numberOfTurns: Int,
    selectedTurn: Int,
    onSelectedTurnChange: (Int) -> Unit,
    onAddTurn: () -> Unit,
    onRemoveTurn: (Int) -> Unit
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
                modifier = Modifier.weight(1f)
            ) {
                items(numberOfTurns) { index ->
                    val isSelected = selectedTurn == index

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(end = 5.dp)
                            .background(
                                color = if (isSelected) MaterialTheme.colorScheme.secondary else Color.Transparent,
                                shape = MaterialTheme.shapes.medium
                            )
                            .clickable { onSelectedTurnChange(index) }
                    ) {
                        Text(
                            text = "TURN ${index + 1}",
                            color = if (isSelected) MaterialTheme.colorScheme.onSecondary else Color.Unspecified,
                            modifier = Modifier.padding(5.dp, 2.dp)
                        )

                        AnimatedVisibility(index > 0 && index == numberOfTurns - 1) {
                            Box(
                                modifier = Modifier.clickable {
                                    if (selectedTurn == index) {
                                        onSelectedTurnChange(index - 1)
                                    }
                                    onRemoveTurn(index)
                                }
                            ) {
                                Icon(
                                    painterResource(R.drawable.ic_close),
                                    contentDescription = "Remove turn",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.secondary, shape = CircleShape)
                    .clip(CircleShape)
                    .clickable { onAddTurn() }
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add turn",
                    tint = MaterialTheme.colorScheme.onSecondary
                )
            }
        }
    }
}
