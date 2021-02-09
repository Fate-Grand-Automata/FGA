package com.mathewsachin.fategrandautomata.ui.prefs

import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.scripts.enums.ScriptModeEnum
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences
import com.mathewsachin.fategrandautomata.ui.prefs.compose.FgaTheme

@Composable
fun launcher(
    text: String,
    modifier: Modifier,
    build: () -> ScriptLauncherResponse
): ScriptLauncherResponseBuilder {
    Text(
        text,
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.h6,
        modifier = modifier
            .padding(top = 16.dp)
    )

    return ScriptLauncherResponseBuilder(
        canBuild = { true },
        build = build
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
            val responseBuilder = when (scriptMode) {
                ScriptModeEnum.Battle, ScriptModeEnum.SupportImageMaker ->
                    battleLauncher(
                        prefs,
                        modifier = Modifier.weight(1f)
                    )
                ScriptModeEnum.FP ->
                    launcher(
                        stringResource(R.string.p_script_mode_fp),
                        modifier = Modifier.weight(1f)
                    ) { ScriptLauncherResponse.FP }
                ScriptModeEnum.Lottery ->
                    launcher(
                        stringResource(R.string.p_script_mode_lottery),
                        modifier = Modifier.weight(1f)
                    ) { ScriptLauncherResponse.Lottery }
                ScriptModeEnum.PresentBox -> giftBoxLauncher(
                    prefs,
                    modifier = Modifier.weight(1f)
                )
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
