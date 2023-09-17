package io.github.fate_grand_automata.ui.battle_config_apple

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.fate_grand_automata.R
import io.github.fate_grand_automata.ui.Heading
import io.github.fate_grand_automata.ui.Tabbed
import io.github.fate_grand_automata.util.stringRes
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.fate_grand_automata.prefs.core.PerServerConfigPrefsCore
import io.github.fate_grand_automata.scripts.enums.GameServer
import io.github.fate_grand_automata.scripts.enums.RefillResourceEnum
import io.github.fate_grand_automata.ui.Stepper
import io.github.fate_grand_automata.util.drawable

@Composable
fun BattleConfigAppleScreen(
    vm: BattleConfigAppleViewModel = viewModel(),
    navigate: (String) -> Unit
) {
//    val servers by vm.gameServers.collectAsState(emptyList())
//
//    val serverPrefs by vm.serverPrefs.collectAsState(emptyList())
//    BattleConfigAppleContent(
//        servers=servers,
//        serverPrefs=serverPrefs
//    )

}

@SuppressLint("UnrememberedMutableState")
@Composable
fun BattleConfigAppleContent(
    servers: List<GameServer>,
    serverPrefs: List<PerServerConfigPrefsCore>
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Heading(
            text = stringResource(R.string.battle_config_list_apples),
            subheading = {
                Text(
                    text = stringResource(R.string.battle_config_apple_subheading),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        )

        if (servers.isEmpty()) {
//            AppleContents()
        } else {
            Tabbed(
                items = servers,
                heading = {server ->
                    Text(stringResource(server.stringRes))
                },
                content = { current ->
                    val filterServerPref by derivedStateOf {
                        serverPrefs.singleOrNull{
                            it.serverRaw.get() == current.toString()
                        }
                    }
                    filterServerPref?.let { AppleContents(serverPref = it) }
                }
            )
        }
    }
}

@Composable
fun AppleContents(
    serverPref: PerServerConfigPrefsCore
) {
    var rainbowAppleCount = remember {
        mutableStateOf(serverPref.rainbowAppleCount.get())
    }
    var goldAppleCount = remember {
        mutableStateOf(serverPref.goldAppleCount.get())
    }
    var silverAppleCount = remember {
        mutableStateOf(serverPref.silverAppleCount.get())
    }
    var blueAppleCount = remember {
        mutableStateOf(serverPref.blueAppleCount.get())
    }
    var copperAppleCount = remember {
        mutableStateOf(serverPref.copperAppleCount.get())
    }
    LazyColumn(
        modifier= Modifier.padding(horizontal = 8.dp),
        content = {
            item {
                AppleItem(
                    appleCount = rainbowAppleCount,
                    mat=RefillResourceEnum.SQ
                )
            }
            item {
                AppleItem(
                    appleCount = goldAppleCount,
                    mat=RefillResourceEnum.Gold
                )
            }
            item {
                AppleItem(
                    appleCount = silverAppleCount,
                    mat=RefillResourceEnum.Silver
                )
            }
            item {
                AppleItem(
                    appleCount = blueAppleCount,
                    mat=RefillResourceEnum.Bronze
                )
            }
            item {
                AppleItem(
                    appleCount = copperAppleCount,
                    mat=RefillResourceEnum.Copper
                )
            }
        })
}

@Composable
private fun AppleItem(
    appleCount: MutableState<Int>,
    mat: RefillResourceEnum,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = mat.drawable),
                contentDescription = stringResource(id = mat.stringRes)
            )
            Text(
                text = stringResource(mat.stringRes)
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ){
            Card(
                shape = CircleShape,
            ) {
                Box(
                    contentAlignment= Alignment.Center,
                    modifier = Modifier
                        .clickable {
                            appleCount.value = 0
                        }
                        .padding(10.dp, 4.dp)
                ){
                    Text(
                        text= stringResource(id = R.string.reset).uppercase(),
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            }
            Stepper(
                value = appleCount.value,
                onValueChange = { appleCount.value = it },
                valueRange = 0..999,
                enabled = true
            )
        }

    }
}