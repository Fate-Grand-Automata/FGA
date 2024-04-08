package io.github.fate_grand_automata.ui.launcher

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import io.github.fate_grand_automata.R
import io.github.fate_grand_automata.prefs.core.PrefsCore
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

    var shouldRedirectToSell by prefsCore.friendGacha.shouldRedirectToSell.remember()

    Column(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .padding(top = 5.dp)
    ) {
        Text(
            stringResource(R.string.p_script_mode_fp),
            style = MaterialTheme.typography.titleLarge
        )

        HorizontalDivider(
            modifier = Modifier
                .padding(5.dp)
                .padding(bottom = 16.dp)
        )
        LazyColumn {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            shouldLimit = !shouldLimit
                        }
                ) {
                    Text(
                        stringResource(R.string.p_roll_limit),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Switch(
                        checked = shouldLimit,
                        onCheckedChange = { shouldLimit = it }
                    )
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Stepper(
                        value = rollLimit,
                        onValueChange = { rollLimit = it },
                        valueRange = 1..999,
                        enabled = shouldLimit
                    )
                }
            }

            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            shouldRedirectToSell = !shouldRedirectToSell
                        }
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f),
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            stringResource(R.string.should_redirect_to_sell_after_summon),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            stringResource(R.string.should_redirect_to_sell_after_summon_warning),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }


                    Switch(
                        checked = shouldRedirectToSell,
                        onCheckedChange = { shouldRedirectToSell = it }
                    )
                }
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

@Composable
private fun bodyTextSize(): TextStyle {
    return if (LocalConfiguration.current.screenHeightDp < 500) MaterialTheme.typography.bodySmall else
        MaterialTheme.typography.bodyLarge
}

@Composable
private fun labelTextSize(): TextStyle {
    return if (LocalConfiguration.current.screenHeightDp < 500) MaterialTheme.typography.labelSmall else
        MaterialTheme.typography.labelLarge
}