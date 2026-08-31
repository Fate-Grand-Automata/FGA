package io.github.fate_grand_automata.ui.battle_config_item

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import io.github.fate_grand_automata.R
import io.github.fate_grand_automata.prefs.core.BattleConfigCore
import io.github.fate_grand_automata.scripts.enums.GameServers
import io.github.fate_grand_automata.ui.GroupSelectorItem
import io.github.fate_grand_automata.ui.dialog.FgaDialog
import io.github.fate_grand_automata.ui.prefs.remember
import io.github.fate_grand_automata.util.stringRes

@Composable
fun ServerSelection(
    config: BattleConfigCore,
    modifier: Modifier = Modifier
) {
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
                items(
                    // battle configs don't need to know about BetterFGO
                    GameServers.values.filter { !it.betterFgo }
                ) {
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

    ConfigSummaryCell(
        label = stringResource(R.string.p_battle_config_server),
        onClick = { dialog.show() },
        modifier = modifier
    ) {
        ConfigSummaryValue(
            server.asGameServer()?.let { stringResource(it.stringRes) } ?: "--"
        )
    }
}