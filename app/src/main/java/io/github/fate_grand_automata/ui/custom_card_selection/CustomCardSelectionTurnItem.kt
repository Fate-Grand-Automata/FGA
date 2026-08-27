package io.github.fate_grand_automata.ui.custom_card_selection

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import io.github.fate_grand_automata.R
import io.github.fate_grand_automata.scripts.enums.CardTypeEnum
import io.github.fate_grand_automata.scripts.models.CustomCard
import io.github.fate_grand_automata.scripts.models.CustomCardSelection
import io.github.fate_grand_automata.scripts.models.FieldSlot

@Composable
fun CustomCardSelectionTurnItem(
    selection: CustomCardSelection,
    onSelectionChange: (CustomCardSelection) -> Unit
) {
    var showPicker by remember { mutableStateOf(false) }

    if (showPicker) {
        CardPicker(
            onDismiss = { showPicker = false },
            onSelected = {
                val newList = selection.toList() + it
                onSelectionChange(CustomCardSelection(newList))
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            selection.forEachIndexed { index, card ->
                SelectedCardItem(
                    card = card,
                    onRemove = {
                        val newList = selection.toMutableList()
                        newList.removeAt(index)
                        onSelectionChange(CustomCardSelection(newList))
                    }
                )
            }
            if (selection.size < 3) {
                Surface(
                    tonalElevation = 5.dp,
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .padding(5.dp)
                        .size(50.dp),
                    onClick = { showPicker = true }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CardPicker(
    onDismiss: () -> Unit,
    onSelected: (CustomCard) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    stringResource(R.string.p_custom_card_selection_card_picker_title),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                val types = listOf(CardTypeEnum.Buster, CardTypeEnum.Arts, CardTypeEnum.Quick)
                Row(
                    modifier = Modifier.padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FieldSlot.list.forEach { slot ->
                        Text(
                            "S${slot.position}",
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                        types.forEach { type ->
                            val color = when (type) {
                                CardTypeEnum.Buster -> R.color.colorBuster
                                CardTypeEnum.Arts -> R.color.colorArts
                                CardTypeEnum.Quick -> R.color.colorQuick
                                else -> R.color.colorAccent
                            }
                            Surface(
                                shape = MaterialTheme.shapes.small,
                                color = colorResource(color),
                                onClick = {
                                    onSelected(CustomCard(slot, type))
                                    onDismiss()
                                },
                                modifier = Modifier.size(45.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    val text = "${type.name[0]}${slot.position}"
                                    Text(text, color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectedCardItem(
    card: CustomCard,
    onRemove: () -> Unit
) {
    val color = when (card.type) {
        CardTypeEnum.Buster -> R.color.colorBuster
        CardTypeEnum.Arts -> R.color.colorArts
        CardTypeEnum.Quick -> R.color.colorQuick
        else -> R.color.colorAccent
    }

    Box(
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        Surface(
            tonalElevation = 5.dp,
            shape = MaterialTheme.shapes.medium,
            color = colorResource(color),
            modifier = Modifier.size(50.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    card.toString(),
                    textAlign = TextAlign.Center,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier
                .size(20.dp)
                .align(Alignment.TopEnd)
                .offset(x = 8.dp, y = (-8).dp)
                .clickable { onRemove() }
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Remove",
                tint = Color.White,
                modifier = Modifier.padding(2.dp)
            )
        }
    }
}
