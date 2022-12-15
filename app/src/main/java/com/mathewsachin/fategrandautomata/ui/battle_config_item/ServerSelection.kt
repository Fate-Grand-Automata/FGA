package com.mathewsachin.fategrandautomata.ui.battle_config_item

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.prefs.core.BattleConfigCore
import com.mathewsachin.fategrandautomata.scripts.enums.GameServerEnum
import com.mathewsachin.fategrandautomata.ui.FgaDialog
import com.mathewsachin.fategrandautomata.ui.GroupSelectorItem
import com.mathewsachin.fategrandautomata.ui.prefs.remember
import com.mathewsachin.fategrandautomata.util.stringRes

@Composable
fun ServerSelection(config: BattleConfigCore) {
    var server by config.server.remember()

    val dialog = FgaDialog()

    dialog.build {
        title(stringResource(R.string.p_battle_config_server))

        constrained { modifier ->
            LazyRow(
                horizontalArrangement = Arrangement.Center,
                modifier = modifier
                    .fillMaxWidth()
            ) {
                items(GameServerEnum.values()) {
                    GroupSelectorItem(
                        stringResource(it.stringRes),
                        isSelected = it == server.asGameServer(),
                        onSelect = {
                            server = BattleConfigCore.Server.Set(it)
                            dialog.hide()
                        }
                    )
                }
            }
        }

        buttons(
            showCancel = false,
            // TODO: Localize
            okLabel = "CLEAR",
            onSubmit = { server = BattleConfigCore.Server.NotSet }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .clickable(onClick = { dialog.show() })
            .padding(16.dp, 5.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            stringResource(R.string.p_battle_config_server).uppercase(),
            style = MaterialTheme.typography.bodySmall
        )

        Text(
            server.asGameServer()?.let { stringResource(it.stringRes) } ?: "--",
            style = MaterialTheme.typography.bodySmall
        )
    }
}