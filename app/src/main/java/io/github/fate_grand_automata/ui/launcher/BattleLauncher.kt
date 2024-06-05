package io.github.fate_grand_automata.ui.launcher

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.fate_grand_automata.scripts.enums.GameServer
import io.github.fate_grand_automata.scripts.prefs.IPreferences
import io.github.fate_grand_automata.ui.launcher.battle.BattleStateConfig
import io.github.fate_grand_automata.ui.launcher.battle.ConfigSelectionList

@Composable
fun battleLauncher(
    prefs: IPreferences,
    modifier: Modifier = Modifier
): ScriptLauncherResponseBuilder {
    val configs = remember {
        prefs.battleConfigs
            .filter {
                when (it.server) {
                    // always show if no server is set
                    null -> true
                    // ignore betterFgo for En and Jp
                    is GameServer.En -> prefs.gameServer is GameServer.En
                    is GameServer.Jp -> prefs.gameServer is GameServer.Jp
                    GameServer.Cn, GameServer.Kr, GameServer.Tw -> it.server == prefs.gameServer
                }
            }
    }
    var selectedConfigIndex by remember { mutableIntStateOf(configs.indexOf(prefs.selectedBattleConfig)) }

    val perServerConfigPref by remember {
        mutableStateOf(
            prefs.getPerServerConfigPref(prefs.gameServer)
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            if (selectedConfigIndex > -1) {
                prefs.selectedBattleConfig = configs[selectedConfigIndex]
            }
        }
    }



    Row(
        modifier = modifier
            .padding(start = 5.dp, end = 5.dp, top = 5.dp)
    ) {
        ConfigSelectionList(
            modifier = Modifier
                .weight(1f),
            configs = configs,
            selectedConfigIndex = selectedConfigIndex,
            onSelectedConfigIndexChange = { selectedConfigIndex = it }
        )
        VerticalDivider(
            modifier = Modifier
                .padding(5.dp, 2.dp),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
        )
        BattleStateConfig(
            modifier = Modifier
                .weight(1.5f),
            perServerConfigPref = perServerConfigPref,
            prefs = prefs
        )
    }

    return ScriptLauncherResponseBuilder(
        canBuild = { selectedConfigIndex != -1 },
        build = {
            ScriptLauncherResponse.Battle
        }
    )
}

