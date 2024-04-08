package io.github.fate_grand_automata.ui.launcher

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.fate_grand_automata.R
import io.github.fate_grand_automata.scripts.prefs.IPreferences

@Composable
fun lotteryLauncher(
    prefs: IPreferences,
    modifier: Modifier = Modifier
): ScriptLauncherResponseBuilder {
    var receiveEmbers by remember { mutableStateOf(prefs.receiveEmbersWhenGiftBoxFull) }
    var returnToLotteryAfterPresentBox by remember { mutableStateOf(prefs.loopIntoLotteryAfterPresentBox) }
    var maxGoldEmberStackSize by remember { mutableIntStateOf(prefs.maxGoldEmberStackSize) }
    var maxGoldEmberTotalCount by remember { mutableIntStateOf(prefs.maxGoldEmberTotalCount) }

    Column(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .padding(top = 5.dp)
    ) {
        Text(
            stringResource(R.string.p_script_mode_lottery),
            style = MaterialTheme.typography.titleLarge
        )

        HorizontalDivider(
            modifier = Modifier
                .padding(5.dp)
                .padding(bottom = 16.dp)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 5.dp)
                .clickable { receiveEmbers = !receiveEmbers }
        ) {
            Text(
                stringResource(R.string.p_receive_embers),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )

            Switch(
                checked = receiveEmbers,
                onCheckedChange = { receiveEmbers = it }
            )
        }

        AnimatedVisibility(
            visible = receiveEmbers,
            label = "Animate the showing of the gift box settings",
            enter = slideInVertically { it } + expandVertically(expandFrom = Alignment.Top),
            exit = slideOutHorizontally { -it } + shrinkVertically(shrinkTowards = Alignment.Top)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                GiftBoxLauncherContent(
                    maxGoldEmberStackSize = maxGoldEmberStackSize,
                    changeMaxGoldEmberStackSize = { maxGoldEmberStackSize = it },
                    maxGoldEmberTotalCount = maxGoldEmberTotalCount,
                    changeMaxGoldEmberTotalCount = { maxGoldEmberTotalCount = it },
                )
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 5.dp)
                .clickable(
                    enabled = receiveEmbers,
                    onClick = {
                        returnToLotteryAfterPresentBox = !returnToLotteryAfterPresentBox
                    }
                )
        ) {
            Text(
                stringResource(R.string.p_return_to_lottery),
                style = MaterialTheme.typography.bodyMedium,
                color = when(receiveEmbers) {
                    true -> MaterialTheme.colorScheme.secondary
                    false -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)
                },
                textAlign = TextAlign.Justify
            )

            Switch(
                checked = returnToLotteryAfterPresentBox,
                onCheckedChange = { returnToLotteryAfterPresentBox = it },
                enabled = receiveEmbers
            )
        }


    }

    return ScriptLauncherResponseBuilder(
        canBuild = { true },
        build = {
            ScriptLauncherResponse.Lottery(
                giftBox = if (receiveEmbers) {
                    ScriptLauncherResponse.GiftBox(maxGoldEmberStackSize, maxGoldEmberTotalCount)
                } else null,
                returnToLottery = returnToLotteryAfterPresentBox
            )
        }
    )
}