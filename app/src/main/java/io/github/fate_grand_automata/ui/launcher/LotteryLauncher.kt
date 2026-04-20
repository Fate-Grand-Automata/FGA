package io.github.fate_grand_automata.ui.launcher

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
import io.github.fate_grand_automata.ui.Stepper

@Composable
fun lotteryLauncher(
    prefs: IPreferences,
    modifier: Modifier = Modifier
): ScriptLauncherResponseBuilder {
    var receiveEmbers by remember { mutableStateOf(prefs.receiveEmbersWhenGiftBoxFull) }
    var maxGoldEmberStackSize by remember { mutableStateOf(prefs.maxGoldEmberStackSize) }
    var maxGoldEmberTotalCount by remember { mutableStateOf(prefs.maxGoldEmberTotalCount) }

    var lottoLongPressSeconds by remember { mutableStateOf(prefs.lottoLongPressSeconds) }

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
        ) {
            Text(
                stringResource(R.string.p_lotto_long_press_duration),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Justify
            )
            Stepper(
                value = lottoLongPressSeconds,
                onValueChange = { lottoLongPressSeconds = it },
                valueRange = 5..20,
                valueRepresentation = { "${it}s" }
            )
        }

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

        if (receiveEmbers) {
            GiftBoxLauncherContent(
                maxGoldEmberStackSize = maxGoldEmberStackSize,
                changeMaxGoldEmberStackSize = { maxGoldEmberStackSize = it },
                maxGoldEmberTotalCount = maxGoldEmberTotalCount,
                changeMaxGoldEmberTotalCount = { maxGoldEmberTotalCount = it }
            )
        }
    }

    return ScriptLauncherResponseBuilder(
        canBuild = { true },
        build = {
            ScriptLauncherResponse.Lottery(
                if (receiveEmbers) {
                    ScriptLauncherResponse.GiftBox(maxGoldEmberStackSize, maxGoldEmberTotalCount)
                } else null
            )
        }
    )
}