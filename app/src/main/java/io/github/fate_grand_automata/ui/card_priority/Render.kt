package io.github.fate_grand_automata.ui.card_priority

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.fate_grand_automata.R
import io.github.fate_grand_automata.scripts.enums.BraveChainEnum
import io.github.fate_grand_automata.scripts.enums.ChainTypeEnum
import io.github.fate_grand_automata.scripts.models.TeamSlot
import io.github.fate_grand_automata.scripts.models.battle.ChainPriorityItem
import io.github.fate_grand_automata.ui.FGAListItemColors
import io.github.fate_grand_automata.ui.drag_sort.DragSort
import io.github.fate_grand_automata.ui.drag_sort.DragSortItemStyle
import io.github.fate_grand_automata.ui.prefs.listDialog
import io.github.fate_grand_automata.util.stringRes

@Composable
fun CardPriorityListItem.Render() {
    val isServantPriorityOn by useServantPriority
    val isChainPriorityOn by useChainPriority

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CardPriorityDragSort(scores)

        ReorderHint()

        Card(
            modifier = Modifier
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        ) {
            CheckboxItem(
                title = stringResource(R.string.p_servant_priority),
                state = useServantPriority,
                modifier = Modifier.fillMaxWidth()
            )

            CheckboxItem(
                title = stringResource(R.string.p_chain_priority),
                state = useChainPriority,
                modifier = Modifier.fillMaxWidth()
            )

            BraveChainsItem(
                state = braveChains,
                isChainPriorityOn = isChainPriorityOn
            )

            CheckboxItem(
                title = stringResource(R.string.p_rearrange_cards),
                summary = stringResource(R.string.p_rearrange_cards_summary),
                state = rearrangeCards,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (isChainPriorityOn) {
            ChainPriority(
                priorities = chainPriority
            )
        }

        if (isServantPriorityOn) {
            ServantPriority(
                priorities = servantPriority
            )
        }
    }
}

@Composable
private fun BraveChainsItem(
    state: MutableState<BraveChainEnum>,
    isChainPriorityOn: Boolean
) {
    var braveChains by state
    // Always runs as With NP without Chain Priority, so that is what is shown. The saved value is kept.
    val shownBraveChains =
        if (!isChainPriorityOn && braveChains == BraveChainEnum.Always) BraveChainEnum.WithNP
        else braveChains

    val braveChainDialog = listDialog(
        selected = shownBraveChains,
        // Choosing the shown value again keeps the saved one
        onSelectedChange = { if (it != shownBraveChains) braveChains = it },
        entries = BraveChainEnum.entries
            .filter { isChainPriorityOn || it != BraveChainEnum.Always }
            .associateWith { stringResource(it.stringRes) },
        title = stringResource(R.string.p_brave_chains)
    )

    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { braveChainDialog.show() },
        supportingContent = { Text(stringResource(shownBraveChains.stringRes)) },
        colors = FGAListItemColors()
    ) {
        Text(stringResource(R.string.p_brave_chains))
    }
}

@Composable
private fun CheckboxItem(
    title: String,
    state: MutableState<Boolean>,
    modifier: Modifier = Modifier,
    summary: String? = null
) {
    var checked by state

    ListItem(
        modifier = modifier
            .clickable { checked = !checked },
        supportingContent = summary?.let { { Text(it) } },
        trailingContent = {
            Checkbox(
                checked = checked,
                onCheckedChange = { checked = it }
            )
        },
        colors = FGAListItemColors()
    ) {
        Text(title)
    }
}

@Composable
private fun ServantPriority(
    priorities: SnapshotStateList<TeamSlot>
) {
    Text(
        stringResource(R.string.p_servant_priority).uppercase(),
        modifier = Modifier
            .padding(bottom = 5.dp, top = 16.dp)
    )

    DragSort(
        items = priorities,
        key = { it.position },
        style = {
            DragSortItemStyle(
                backgroundColor = colorResource(
                    when (it.position) {
                        1 -> R.color.colorArts
                        2 -> R.color.colorQuick
                        3 -> R.color.colorBuster
                        4 -> R.color.colorArtsResist
                        5 -> R.color.colorQuickResist
                        else -> R.color.colorBusterResist
                    }
                ),
                contentColor = Color.White,
                text = "  ${it.position}  "
            )
        },
        modifier = Modifier.fillMaxWidth(),
        fillWidth = true
    )

    ReorderHint()
}

@Composable
private fun ChainPriority(
    priorities: SnapshotStateList<ChainPriorityItem>
) {
    Text(
        stringResource(R.string.p_chain_priority).uppercase(),
        modifier = Modifier
            .padding(bottom = 10.dp, top = 10.dp)
    )

    DragSort(
        items = priorities,
        key = { it.type },
        style = {
            if (it.isEnabled) {
                DragSortItemStyle(
                    backgroundColor = colorResource(
                        when (it.type) {
                            ChainTypeEnum.Arts -> R.color.colorArts
                            ChainTypeEnum.Quick -> R.color.colorQuick
                            ChainTypeEnum.Buster -> R.color.colorBuster
                            ChainTypeEnum.Mighty -> R.color.colorPrimaryDark
                            ChainTypeEnum.Avoid -> R.color.colorAvoid
                        }
                    ),
                    contentColor = Color.White,
                    text = stringResource(it.type.stringRes)
                )
            } else {
                DragSortItemStyle(
                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    text = stringResource(it.type.stringRes)
                )
            }
        },
        modifier = Modifier.fillMaxWidth(),
        fillWidth = true,
        onClick = { index ->
            val item = priorities[index]
            priorities[index] = item.copy(isEnabled = !item.isEnabled)
        }
    )

    Text(
        stringResource(R.string.chain_priority_hint),
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}

@Composable
private fun ReorderHint() {
    Text(
        stringResource(R.string.card_priority_hint_move_item),
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}
