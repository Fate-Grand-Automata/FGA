package com.mathewsachin.fategrandautomata.ui.card_priority

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Checkbox
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.scripts.enums.BraveChainEnum
import com.mathewsachin.fategrandautomata.ui.prefs.listDialog
import com.mathewsachin.fategrandautomata.util.stringRes

@Composable
fun CardPriorityListItem.Render() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CardPriorityDragSort(scores)

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 5.dp)
        ) {
            var braveChains by braveChains

            val braveChainDialog = listDialog(
                selected = braveChains,
                selectedChange = { braveChains = it },
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
}