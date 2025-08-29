package io.github.fate_grand_automata.ui.card_priority

import android.graphics.Color
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.fate_grand_automata.R
import io.github.fate_grand_automata.scripts.enums.BraveChainEnum
import io.github.fate_grand_automata.scripts.enums.ChainTypeEnum
import io.github.fate_grand_automata.scripts.models.TeamSlot
import io.github.fate_grand_automata.ui.FGAListItemColors
import io.github.fate_grand_automata.ui.FGAListItemColorsDisabled
import io.github.fate_grand_automata.ui.drag_sort.DragSort
import io.github.fate_grand_automata.ui.drag_sort.DragSortAdapter
import io.github.fate_grand_automata.ui.prefs.listDialog
import io.github.fate_grand_automata.util.stringRes

@Composable
fun CardPriorityListItem.Render(
    useServantPriority: Boolean,
    useChainPriority: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CardPriorityDragSort(scores)

        Card(
            modifier = Modifier
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                var braveChains by braveChains

                val braveChainDialog = listDialog(
                    selected = braveChains,
                    onSelectedChange = { braveChains = it },
                    entries = BraveChainEnum.entries
                        .associateWith { stringResource(it.stringRes) },
                    title = stringResource(R.string.p_brave_chains)
                )

                val braveChainListItemColor = if (useChainPriority) FGAListItemColorsDisabled() else FGAListItemColors()
                ListItem(
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            if (!useChainPriority)
                                braveChainDialog.show()
                        },
                    headlineContent = { Text(stringResource(R.string.p_brave_chains)) },
                    supportingContent = { Text(stringResource(braveChains.stringRes)) },
                    colors = braveChainListItemColor
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

        if (useChainPriority) {
            ChainPriority(
                priorities = chainPriority
            )
        }

        if (useServantPriority) {
            ServantPriority(
                priorities = servantPriority
            )
        }
    }
}

@Composable
private fun ServantPriority(
    priorities: MutableList<TeamSlot>
) {
    Text(
        stringResource(R.string.p_servant_priority).uppercase(),
        modifier = Modifier
            .padding(bottom = 5.dp, top = 16.dp)
    )

    val context = LocalContext.current

    DragSort(
        items = priorities,
        viewConfigGrabber = {
            DragSortAdapter.ItemViewConfig(
                foregroundColor = Color.WHITE,
                backgroundColor = when (it.position) {
                    1 -> R.color.colorArts
                    2 -> R.color.colorQuick
                    3 -> R.color.colorBuster
                    4 -> R.color.colorArtsResist
                    5 -> R.color.colorQuickResist
                    else -> R.color.colorBusterResist
                }.let { res -> context.getColor(res) },
                text = "  ${it.position}  "
            )
        }
    )
}

@Composable
private fun ChainPriority(
    priorities: MutableList<ChainTypeEnum>
) {
    Text(
        stringResource(R.string.p_chain_priority).uppercase(),
        modifier = Modifier
            .padding(bottom = 10.dp, top = 10.dp)
    )

    val context = LocalContext.current

    DragSort(
        items = priorities,
        viewConfigGrabber = {
            DragSortAdapter.ItemViewConfig(
                foregroundColor = Color.WHITE,
                backgroundColor = when (it) {
                    ChainTypeEnum.Arts -> R.color.colorArts
                    ChainTypeEnum.Quick -> R.color.colorQuick
                    ChainTypeEnum.Buster -> R.color.colorBuster
                    ChainTypeEnum.Brave -> R.color.colorQuickResist
                    ChainTypeEnum.Mighty -> R.color.colorArtsResist
                    ChainTypeEnum.Avoid -> R.color.colorBusterResist
                }.let { res -> context.getColor(res) },
                text = it.name
            )
        }
    )
}