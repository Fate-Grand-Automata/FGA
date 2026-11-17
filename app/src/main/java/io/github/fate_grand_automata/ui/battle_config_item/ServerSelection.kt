package io.github.fate_grand_automata.ui.battle_config_item

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import io.github.fate_grand_automata.R
import io.github.fate_grand_automata.prefs.core.BattleConfigCore
import io.github.fate_grand_automata.scripts.enums.GameServers
import io.github.fate_grand_automata.ui.FGAMenuContainerColor
import io.github.fate_grand_automata.ui.prefs.remember
import io.github.fate_grand_automata.util.stringRes

@Composable
fun ServerSelection(
    config: BattleConfigCore,
    modifier: Modifier = Modifier
) {
    var server by config.server.remember()
    var expanded by remember { mutableStateOf(false) }

    val anyLabel = stringResource(R.string.battle_config_server_any)
    val selectedCheck: @Composable () -> Unit = { Icon(Icons.Default.Check, contentDescription = null) }

    /*
     * The Box only exists to anchor the menu, so the cell has to fill it — otherwise the cell
     * shrinks to its content and stops matching the other cells in the strip.
     */
    Box(modifier = modifier.fillMaxHeight()) {
        ConfigSummaryCell(
            label = stringResource(R.string.p_battle_config_server),
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            ConfigSummaryValue(
                server.asGameServer()?.let { stringResource(it.stringRes) } ?: anyLabel
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = FGAMenuContainerColor()
        ) {
            DropdownMenuItem(
                text = { Text(anyLabel) },
                trailingIcon = if (server.asGameServer() == null) selectedCheck else null,
                onClick = {
                    server = BattleConfigCore.Server.NotSet
                    expanded = false
                }
            )

            // battle configs don't need to know about BetterFGO
            GameServers.values.filter { !it.betterFgo }.forEach {
                DropdownMenuItem(
                    text = { Text(stringResource(it.stringRes)) },
                    trailingIcon = if (it == server.asGameServer()) selectedCheck else null,
                    onClick = {
                        server = BattleConfigCore.Server.Set(it)
                        expanded = false
                    }
                )
            }
        }
    }
}
