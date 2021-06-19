package com.mathewsachin.fategrandautomata.ui.launcher

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences
import com.mathewsachin.fategrandautomata.ui.Stepper

@Composable
fun giftBoxLauncher(
    prefs: IPreferences,
    modifier: Modifier = Modifier
): ScriptLauncherResponseBuilder {
    var maxGoldEmberStackSize by remember { mutableStateOf(prefs.maxGoldEmberSetSize) }

    Column(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .padding(top = 5.dp)
    ) {
        Text(
            stringResource(R.string.p_script_mode_gift_box),
            style = MaterialTheme.typography.h6
        )

        Divider(
            modifier = Modifier
                .padding(5.dp)
                .padding(bottom = 16.dp)
        )

        Text(
            stringResource(R.string.p_max_gold_ember_set_size),
            style = MaterialTheme.typography.body2,
            color = MaterialTheme.colors.secondary
        )

        Box(
            modifier = Modifier.align(Alignment.End)
        ) {
            Stepper(
                value = maxGoldEmberStackSize,
                onValueChange = { maxGoldEmberStackSize = it },
                valueRange = 0..4
            )
        }
    }

    return ScriptLauncherResponseBuilder(
        canBuild = { true },
        build = { ScriptLauncherResponse.GiftBox(maxGoldEmberStackSize) }
    )
}