package io.github.fate_grand_automata.ui.card_priority

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.fate_grand_automata.R
import io.github.fate_grand_automata.scripts.enums.BraveChainEnum
import io.github.fate_grand_automata.scripts.models.TeamSlot
import io.github.fate_grand_automata.ui.FGAListItemColors
import io.github.fate_grand_automata.ui.customFGAColors
import io.github.fate_grand_automata.ui.drag_sort.DragSort
import io.github.fate_grand_automata.ui.prefs.listDialog
import io.github.fate_grand_automata.util.stringRes

@Composable
fun CardPriorityListItem.Render(
    useServantPriority: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CardPriorityDragSort(
            scores = scores,
            onSubmit = {
                scores = it
            }
        )

        Card(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                var braveChains by braveChains

                val braveChainDialog = listDialog(
                    selected = braveChains,
                    onSelectedChange = { braveChains = it },
                    entries = BraveChainEnum.values()
                        .associateWith { stringResource(it.stringRes) },
                    title = stringResource(R.string.p_brave_chains)
                )

                ListItem(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { braveChainDialog.show() },
                    headlineContent = { Text(stringResource(R.string.p_brave_chains)) },
                    supportingContent = { Text(stringResource(braveChains.stringRes)) },
                    colors = FGAListItemColors()
                )

                var rearrange by rearrangeCards

                ListItem(
                    modifier = Modifier
                        .weight(1.1f)
                        .clickable { rearrange = !rearrange },
                    headlineContent = { Text(stringResource(R.string.p_rearrange_cards)) },
                    trailingContent = {
                        Checkbox(
                            checked = rearrange,
                            onCheckedChange = { rearrange = it }
                        )
                    },
                    colors = FGAListItemColors()
                )
            }
        }

        if (useServantPriority) {
            ServantPriority(
                priorities = servantPriority,
                onSubmit = {
                    servantPriority = it
                }
            )
        }
    }
}

@Composable
private fun ServantPriority(
    priorities: MutableList<TeamSlot>,
    onSubmit: (MutableList<TeamSlot>) -> Unit
) {
    Text(
        text = stringResource(id = R.string.p_battle_config_servant_priority).uppercase(),
        modifier = Modifier
            .padding(bottom = 5.dp, top = 16.dp)
    )

    var clonePriorities by remember { mutableStateOf(priorities.toList()) }


    DragSort(
        titleText = stringResource(id = R.string.p_battle_config_servant_priority),
        messageText= stringResource(R.string.p_battle_config_instructions_drag_and_drop),
        items = clonePriorities,
        itemContent = { item ->
            ListItem(
                leadingContent = {
                    Icon(
                        imageVector = Icons.Default.DragHandle,
                        contentDescription = stringResource(id = R.string.p_battle_config_servant_priority),
                        tint = Color.White
                    )
                },
                trailingContent = {
                    // Added Blank icon to make the Text Centered
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = null,
                        tint = Color.Transparent
                    )
                },
                colors = ListItemDefaults.colors(
                    containerColor = prioritiesItemColor(item),
                ),
                modifier = Modifier
                    .fillMaxWidth(),
                headlineContent = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        Text(
                            text = "${item.position}",
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                }
            )
        },
        initialContent = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                clonePriorities.forEach { item ->
                    PrioritiesCardItem(
                        item = item,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        onSubmit = {
            clonePriorities = it
            onSubmit(clonePriorities.toMutableList())
        }
    )
}

@Composable
private fun PrioritiesCardItem(
    item: TeamSlot,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(modifier),
        shape = RectangleShape
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .background(
                    color = prioritiesItemColor(item)
                )
                .padding(vertical = 4.dp),
            content = {
                Text(
                    text = "${item.position}",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )
            }
        )
    }
}

@Composable
private fun prioritiesItemColor(item: TeamSlot) = when (item.position) {
    1 -> MaterialTheme.customFGAColors.colorArts
    2 -> MaterialTheme.customFGAColors.colorQuick
    3 -> MaterialTheme.customFGAColors.colorBuster
    4 -> MaterialTheme.customFGAColors.colorArtsResist
    5 -> MaterialTheme.customFGAColors.colorQuickResist
    else -> MaterialTheme.customFGAColors.colorBusterResist
}