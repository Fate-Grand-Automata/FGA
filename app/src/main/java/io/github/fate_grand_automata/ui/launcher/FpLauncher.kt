package io.github.fate_grand_automata.ui.launcher

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.fate_grand_automata.R
import io.github.fate_grand_automata.prefs.core.PrefsCore
import io.github.fate_grand_automata.scripts.enums.GameServer
import io.github.fate_grand_automata.scripts.prefs.IPreferences
import io.github.fate_grand_automata.ui.Stepper
import io.github.fate_grand_automata.ui.prefs.remember

@Composable
fun fpLauncher(
    prefs: IPreferences,
    prefsCore: PrefsCore,
    modifier: Modifier = Modifier
): ScriptLauncherResponseBuilder {
    var shouldLimit by prefsCore.friendGacha.shouldLimitFP.remember()
    var rollLimit by prefsCore.friendGacha.limitFP.remember()

    var shouldCreateCEBombAfterSummon by prefsCore.friendGacha.shouldCreateCEBombAfterSummon.remember()

    DisposableEffect(Unit) {
        onDispose {
            if (shouldLimit){
                shouldCreateCEBombAfterSummon = false
            }
        }
    }

    Column(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .padding(top = 5.dp)
    ) {
        Text(
            stringResource(R.string.p_script_mode_fp),
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
                .clickable { shouldLimit = !shouldLimit }
        ) {
            Text(
                stringResource(R.string.p_roll_limit),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
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
        if (prefs.gameServer is GameServer.En || prefs.gameServer is GameServer.Jp) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        enabled = !shouldLimit,
                        onClick = {
                            shouldCreateCEBombAfterSummon = !shouldCreateCEBombAfterSummon
                        }
                    )
            ) {
                Text(
                    stringResource(R.string.p_roll_create_ce_bomb_after_summon),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (shouldLimit) MaterialTheme.colorScheme.secondary.copy(0.3f) else
                        MaterialTheme.colorScheme.secondary
                )

                Switch(
                    checked = shouldCreateCEBombAfterSummon,
                    onCheckedChange = { shouldCreateCEBombAfterSummon = it },
                    enabled = !shouldLimit
                )
            }
        }
    }

    return ScriptLauncherResponseBuilder(
        canBuild = { true },
        build = {
            ScriptLauncherResponse.FP
        }
    )
}