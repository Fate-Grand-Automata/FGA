package com.mathewsachin.fategrandautomata.ui.launcher

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences

@Composable
fun lotteryLauncher(
    prefs: IPreferences,
    modifier: Modifier = Modifier
): ScriptLauncherResponseBuilder {
    var receiveEmbers by remember { mutableStateOf(prefs.receiveEmbersWhenGiftBoxFull) }
    var maxGoldEmberStackSize by remember { mutableStateOf(prefs.maxGoldEmberSetSize) }

    Column(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .padding(top = 5.dp)
    ) {
        Text(
            stringResource(R.string.p_script_mode_lottery),
            style = MaterialTheme.typography.titleLarge
        )

        Divider(
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

        if (receiveEmbers) {
            GiftBoxLauncherContent(
                maxGoldEmberStackSize = maxGoldEmberStackSize,
                changeMaxEmberStackSize = { maxGoldEmberStackSize = it }
            )
        }
    }

    return ScriptLauncherResponseBuilder(
        canBuild = { true },
        build = {
            ScriptLauncherResponse.Lottery(
                if (receiveEmbers) ScriptLauncherResponse.GiftBox(maxGoldEmberStackSize) else null
            )
        }
    )
}