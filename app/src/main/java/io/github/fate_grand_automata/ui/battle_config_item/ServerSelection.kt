package io.github.fate_grand_automata.ui.battle_config_item

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import io.github.fate_grand_automata.R
import io.github.fate_grand_automata.prefs.core.BattleConfigCore
import io.github.fate_grand_automata.scripts.enums.GameServerEnum
import io.github.fate_grand_automata.ui.FgaDialog
import io.github.fate_grand_automata.ui.GroupSelectorItem
import io.github.fate_grand_automata.ui.prefs.remember
import io.github.fate_grand_automata.util.stringRes

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