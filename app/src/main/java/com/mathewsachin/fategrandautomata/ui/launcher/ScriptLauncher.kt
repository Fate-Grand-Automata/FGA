package com.mathewsachin.fategrandautomata.ui.launcher

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.scripts.enums.ScriptModeEnum
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences
import com.mathewsachin.fategrandautomata.ui.prefs.FgaTheme
import com.mathewsachin.fategrandautomata.ui.Stepper

@Composable
fun lotteryLauncher(
    prefs: IPreferences,
    modifier: Modifier = Modifier
): ScriptLauncherResponseBuilder {
    var preventReset by remember { mutableStateOf(prefs.preventLotteryBoxReset) }

    Column(
        modifier = modifier
            .fillMaxWidth()
    ) {
        Text(
            stringResource(R.string.p_script_mode_lottery),
            style = MaterialTheme.typography.h6
        )

        Divider(
            modifier = Modifier
                .padding(5.dp)
                .padding(bottom = 16.dp)
        )

        Row (
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { preventReset = !preventReset }
        ) {
            Text(
                stringResource(R.string.p_prevent_lottery_box_reset),
                style = MaterialTheme.typography.body2,
                color = MaterialTheme.colors.secondary
            )

            Switch(
                checked = preventReset,
                onCheckedChange = { preventReset = it }
            )
        }
    }

    return ScriptLauncherResponseBuilder(
        canBuild = { true },
        build = {
            ScriptLauncherResponse.Lottery(preventReset)
        }
    )
}

@Composable
fun fpLauncher(
    prefs: IPreferences,
    modifier: Modifier = Modifier
): ScriptLauncherResponseBuilder {
    var shouldLimit by remember { mutableStateOf(prefs.shouldLimitFP) }
    var rollLimit by remember { mutableStateOf(prefs.limitFP) }

    Column(
        modifier = modifier
            .fillMaxWidth()
    ) {
        Text(
            stringResource(R.string.p_script_mode_fp),
            style = MaterialTheme.typography.h6
        )

        Divider(
            modifier = Modifier
                .padding(5.dp)
                .padding(bottom = 16.dp)
        )

        Row (
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { shouldLimit = !shouldLimit }
        ) {
            Text(
                stringResource(R.string.p_roll_limit),
                style = MaterialTheme.typography.body2,
                color = MaterialTheme.colors.secondary
            )

            Switch(
                checked = shouldLimit,
                onCheckedChange = { shouldLimit = it }
            )
        }

        Box(
            modifier = Modifier.align(Alignment.End)
        ) {
            Stepper(
                value = rollLimit,
                onValueChange = { rollLimit = it },
                valueRange = 1..999,
                enabled = shouldLimit
            )
        }
    }

    return ScriptLauncherResponseBuilder(
        canBuild = { true },
        build = {
            ScriptLauncherResponse.FP(
                if (shouldLimit) rollLimit else null
            )
        }
    )
}

@Composable
fun giftBoxLauncher(
    prefs: IPreferences,
    modifier: Modifier = Modifier
): ScriptLauncherResponseBuilder {
    var maxGoldEmberStackSize by remember { mutableStateOf(prefs.maxGoldEmberSetSize) }

    Column(
        modifier = modifier
            .fillMaxWidth()
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
            stringResource(R.string.p_run_limit),
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

@Composable
fun ScriptLauncher(
    scriptMode: ScriptModeEnum,
    onResponse: (ScriptLauncherResponse) -> Unit,
    prefs: IPreferences
) {
    FgaTheme(
        backgroundColor = Color.Transparent
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(top = 5.dp)
                .fillMaxSize()
        ) {
            val modifier = Modifier.weight(1f)

            val responseBuilder = when (scriptMode) {
                ScriptModeEnum.Battle, ScriptModeEnum.SupportImageMaker ->
                    battleLauncher(prefs, modifier)
                ScriptModeEnum.FP -> fpLauncher(prefs, modifier)
                ScriptModeEnum.Lottery -> lotteryLauncher(prefs, modifier)
                ScriptModeEnum.PresentBox -> giftBoxLauncher(prefs, modifier)
            }

            Divider()

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row {
                    if (scriptMode == ScriptModeEnum.SupportImageMaker) {
                        TextButton(onClick = { onResponse(ScriptLauncherResponse.SupportImageMaker) }) {
                            Text(stringResource(R.string.p_script_mode_support_image_maker))
                        }
                    }
                }

                Row {
                    TextButton(onClick = { onResponse(ScriptLauncherResponse.Cancel) }) {
                        Text(stringResource(android.R.string.cancel))
                    }

                    TextButton(
                        onClick = { onResponse(responseBuilder.build()) },
                        enabled = responseBuilder.canBuild()
                    ) {
                        Text(stringResource(android.R.string.ok))
                    }
                }
            }
        }
    }
}
