package io.github.fate_grand_automata.ui.battle_config_apple

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.fate_grand_automata.R
import io.github.fate_grand_automata.ui.Heading
import io.github.fate_grand_automata.ui.Tabbed
import io.github.fate_grand_automata.util.stringRes
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.fate_grand_automata.prefs.core.PerServerConfigPrefsCore
import io.github.fate_grand_automata.prefs.core.Pref
import io.github.fate_grand_automata.scripts.enums.GameServer
import io.github.fate_grand_automata.scripts.enums.MaterialEnum
import io.github.fate_grand_automata.scripts.enums.RefillResourceEnum
import io.github.fate_grand_automata.ui.FgaDialog
import io.github.fate_grand_automata.ui.HeadingButton
import io.github.fate_grand_automata.ui.Stepper
import io.github.fate_grand_automata.ui.battle_config_item.Material
import io.github.fate_grand_automata.ui.battle_config_item.MaterialsSummary
import io.github.fate_grand_automata.ui.multiChoiceList
import io.github.fate_grand_automata.ui.prefs.remember
import io.github.fate_grand_automata.util.drawable

@Composable
fun BattleConfigAppleScreen(
    vm: BattleConfigAppleViewModel = viewModel(),
    navigate: (String) -> Unit
) {

    val serverConfigPrefListFlow by vm.perServerConfigPrefsList.collectAsState(initial = emptyList())

    val gameServers by vm.gameServers.collectAsState(initial = listOf(GameServer.default))

    BattleConfigAppleContent(
        serverConfigPrefsList = serverConfigPrefListFlow,
        gameServers=gameServers,
        onSubmit = {
            vm.updateGameServers(it)
        }
    )
}

@Composable
fun SelectGameServers(
    servers: List<GameServer>,
    onSubmit: (List<GameServer>) -> Unit
){
    val dialog = FgaDialog()

    dialog.build {
        var currentGameServers by remember {
            mutableStateOf(servers)
        }
        Row {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .alignByBaseline()
            ) {
                title(stringResource(R.string.p_game_server))
            }

            TextButton(
                onClick = {
                    currentGameServers = listOf(GameServer.default)
                },
                modifier = Modifier
                    .padding(16.dp, 5.dp)
                    .alignByBaseline()
            ) {
                Text(
                    stringResource(id = R.string.p_game_area_default)
                )
            }
        }
        multiChoiceList(
            selected = currentGameServers.toSet(),
            onSelectedChange = { currentGameServers = it.toList()},
            items = GameServer.values.filterNot {
                it.betterFgo
            }
        ) { server ->

            Text(
                server.toString(),
                modifier = Modifier
                    .padding(start = 16.dp)
            )
        }

        buttons(
            onSubmit = {
                onSubmit(currentGameServers)
            }
        )



    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        HeadingButton(
            text = stringResource(R.string.p_game_server).uppercase(),
            modifier=Modifier.padding(top = 8.dp, bottom = 8.dp),
            onClick = { dialog.show() }
        )

    }
}

@Composable
fun BattleConfigAppleContent(
    serverConfigPrefsList: List<PerServerConfigPrefsCore>,
    gameServers: List<GameServer>,
    onSubmit: (List<GameServer>) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Heading(
            text = stringResource(R.string.battle_config_list_apples),
            subheading = {
                Column {
                    Text(
                        text = stringResource(R.string.battle_config_apple_subheading),
                        style = MaterialTheme.typography.bodySmall
                    )
                    SelectGameServers(
                        servers=gameServers,
                        onSubmit = onSubmit
                    )
                }
            }
        )

        if (gameServers.isNotEmpty()){
            Tabbed(
                items = gameServers,
                heading = { server ->
                    Text(stringResource(server.stringRes))
                },
                content = { current ->
                    val currentServerConfigPref = serverConfigPrefsList.singleOrNull {
                        it.serverRaw.get() == current.toString()
                    }
                    if (currentServerConfigPref != null) {
                        AppleContents(
                            serverConfigPref = currentServerConfigPref
                        )
                    }
                }
            )
        }
        else{
            Column {
                Text(
                    stringResource(R.string.battle_config_apple_no_game_servers),
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

    }
}

@Composable
fun AppleContents(
    serverConfigPref: PerServerConfigPrefsCore
) {
    LazyColumn(
        modifier = Modifier
            .padding(start = 4.dp, end = 4.dp, top = 4.dp)
            .fillMaxSize(),
        content = {
            item {
                serverConfigPref.rainbowAppleCount.AppleItem(mat = RefillResourceEnum.SQ)
            }
            item {
                serverConfigPref.goldAppleCount.AppleItem(mat = RefillResourceEnum.Gold)
            }
            item {
                serverConfigPref.silverAppleCount.AppleItem(mat = RefillResourceEnum.Silver)
            }
            if (serverConfigPref.serverRaw.get() == "${GameServer.Jp.Original}" ||
                serverConfigPref.serverRaw.get() == "${GameServer.Jp.BetterFGO}" ||
                serverConfigPref.serverRaw.get() == "${GameServer.Cn}"
            ){
                item {
                    serverConfigPref.blueAppleCount.AppleItem(mat = RefillResourceEnum.Bronze)
                }
            }
            item {
                serverConfigPref.copperAppleCount.AppleItem(mat = RefillResourceEnum.Copper)
            }
        })
}

@Composable
private fun Pref<Int>.AppleItem(
    mat: RefillResourceEnum,
) {
    var state by remember()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = MaterialTheme.colorScheme.inverseSurface, shape= RectangleShape),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = mat.drawable),
            contentDescription = stringResource(id = mat.stringRes),
            modifier=Modifier.weight(1f)
        )
        Text(
            text = stringResource(mat.stringRes),
            modifier=Modifier.weight(1f),
            textAlign = TextAlign.Center
        )

        Column(
            modifier=Modifier
                .weight(3f)
                .padding(bottom = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                 contentAlignment = Alignment.Center
            ){
                Stepper(
                    value = state,
                    onValueChange = { state = it },
                    valueRange = 0..999,
                    enabled = true
                )
            }

            Card(
                shape = CircleShape,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .clickable {
                            state = 0
                        }
                        .padding(10.dp, 4.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.reset).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        textAlign= TextAlign.Center,
                    )
                }
            }
        }


    }
}