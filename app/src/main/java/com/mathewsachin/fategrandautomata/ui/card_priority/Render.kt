package com.mathewsachin.fategrandautomata.ui.card_priority

import android.graphics.Color
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.Checkbox
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.scripts.enums.BraveChainEnum
import com.mathewsachin.fategrandautomata.scripts.modules.ServantTracker
import com.mathewsachin.fategrandautomata.ui.drag_sort.DragSort
import com.mathewsachin.fategrandautomata.ui.drag_sort.DragSortAdapter
import com.mathewsachin.fategrandautomata.ui.prefs.listDialog
import com.mathewsachin.fategrandautomata.util.stringRes

@Composable
fun CardPriorityListItem.Render(
    useServantPriority: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CardPriorityDragSort(scores)

        Card(
            modifier = Modifier
                .padding(16.dp)
                .padding(vertical = 16.dp)
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
                    text = { Text(stringResource(R.string.p_brave_chains)) },
                    secondaryText = { Text(stringResource(braveChains.stringRes)) }
                )

                var rearrange by rearrangeCards

                ListItem(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { rearrange = !rearrange },
                    text = { Text(stringResource(R.string.p_rearrange_cards)) },
                    trailing = {
                        Checkbox(
                            checked = rearrange,
                            onCheckedChange = { rearrange = it }
                        )
                    }
                )
            }
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
    priorities: MutableList<ServantTracker.TeamSlot>
) {
    Text(
        "Servant Priority".uppercase(),
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