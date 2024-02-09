package io.github.fate_grand_automata.ui.launcher

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import io.github.fate_grand_automata.R
import io.github.fate_grand_automata.prefs.core.PrefsCore
import io.github.fate_grand_automata.scripts.enums.ScriptModeEnum
import io.github.fate_grand_automata.scripts.prefs.IPreferences
import io.github.fate_grand_automata.ui.FgaScreen

@Composable
fun ScriptLauncher(
    scriptMode: ScriptModeEnum,
    onResponse: (ScriptLauncherResponse) -> Unit,
    prefs: IPreferences,
    prefsCore: PrefsCore
) {
    FgaScreen {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            val modifier = Modifier.weight(1f)

            val responseBuilder = when (scriptMode) {
                ScriptModeEnum.Battle, ScriptModeEnum.SupportImageMaker ->
                    battleLauncher(prefs, modifier)

                ScriptModeEnum.FP -> fpLauncher(prefs, modifier)
                ScriptModeEnum.Lottery -> lotteryLauncher(prefs, modifier)
                ScriptModeEnum.PresentBox -> giftBoxLauncher(prefs, modifier)
                ScriptModeEnum.CEBomb -> ceBombLauncher(prefs, modifier)
                ScriptModeEnum.Append -> appendLauncher(prefsCore, modifier)
                ScriptModeEnum.ServantLevel -> servantLevelLauncher(prefsCore.servantEnhancement, modifier)
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
