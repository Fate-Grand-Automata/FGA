package io.github.fate_grand_automata.ui.battle_config_apple

import android.annotation.SuppressLint
import android.content.res.Configuration
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.fate_grand_automata.R
import io.github.fate_grand_automata.prefs.core.BattleConfigCore
import io.github.fate_grand_automata.ui.Heading
import io.github.fate_grand_automata.ui.Tabbed
import io.github.fate_grand_automata.util.stringRes
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.fate_grand_automata.scripts.enums.MaterialEnum
import io.github.fate_grand_automata.ui.FGATheme
import io.github.fate_grand_automata.ui.Stepper
import io.github.fate_grand_automata.util.drawable

@Composable
fun BattleConfigAppleScreen(
    vm: BattleConfigAppleViewModel = viewModel(),
    navigate: (String) -> Unit
) {
    val configs by vm.battleConfigItems.collectAsState(emptyList())
    BattleConfigAppleContent(
        configs = configs
    )

}

@SuppressLint("UnrememberedMutableState")
@Composable
fun BattleConfigAppleContent(
    configs: List<BattleConfigCore>,
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Heading(
            text = stringResource(R.string.battle_config_list_apples),
        )
        val servers by derivedStateOf {
            configs
                .mapNotNull { it.server.get().asGameServer() }
                .distinct()
        }

        if (servers.isEmpty()) {

        } else {
            Tabbed(
                items = listOf<BattleConfigCore.Server>(BattleConfigCore.Server.NotSet) + servers.map { BattleConfigCore.Server.Set(it) },
                heading = {
                    Text(
                        when (it) {
                            BattleConfigCore.Server.NotSet -> "ALL"
                            is BattleConfigCore.Server.Set -> stringResource(it.server.stringRes)
                        }
                    )
                },
                content = {
                    AppleContents()
                }
            )
        }
    }
}

@Composable
fun AppleContents() {
    var appleCount = remember {
        mutableStateOf(0)
    }
    LazyColumn(
        modifier= Modifier.padding(horizontal = 8.dp),
        content = {
            item {
                AppleItem(
                    appleCount = appleCount,
                    mat=MaterialEnum.RefillSQ
                )
            }
            item {
                AppleItem(
                    appleCount = appleCount,
                    mat=MaterialEnum.RefillGoldApple
                )
            }
            item {
                AppleItem(
                    appleCount = appleCount,
                    mat=MaterialEnum.RefillSilverApple
                )
            }
            item {
                AppleItem(
                    appleCount = appleCount,
                    mat=MaterialEnum.RefillBlueApple
                )
            }
            item {
                AppleItem(
                    appleCount = appleCount,
                    mat=MaterialEnum.RefillCopperApple
                )
            }
        })
}

@Composable
private fun AppleItem(
    appleCount: MutableState<Int>,
    mat: MaterialEnum,
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

@Preview(name = "Light Mode")
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun AppleContentsPreview() {
    FGATheme {
        AppleContents()
    }
}