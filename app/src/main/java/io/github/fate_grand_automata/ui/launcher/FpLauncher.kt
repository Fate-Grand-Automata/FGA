package io.github.fate_grand_automata.ui.launcher

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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

    var skipSortDetection by prefsCore.craftEssence.skipSortDetection.remember()

    var skipFilterDetection by prefsCore.craftEssence.skipCEFilterDetection.remember()

    var targetRarity by prefsCore.craftEssence.ceTargetRarity.remember()

    var fodderRarity by prefsCore.craftEssence.ceFodderRarity.remember()

    var showCEBombSettings by remember { mutableStateOf(false) }

    val isCEBombSupported by remember {
        mutableStateOf(prefs.gameServer is GameServer.Jp || prefs.gameServer is GameServer.En)
    }


    LaunchedEffect(key1 = shouldLimit, block = {
        if (shouldLimit) {
            shouldCreateCEBombAfterSummon = false
        }
    })
    LaunchedEffect(key1 = fodderRarity, block = {
        if (fodderRarity.isEmpty()) {
            fodderRarity = setOf(1, 2)
        }
    })

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
                Divider(modifier = Modifier.padding(vertical = 4.dp))
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            enabled = !shouldLimit && isCEBombSupported,
                            onClick = {
                                shouldCreateCEBombAfterSummon = !shouldCreateCEBombAfterSummon
                            }
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        verticalArrangement = Arrangement.Top,
                    ) {
                        Text(
                            stringResource(R.string.p_roll_create_ce_bomb_after_summon),
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (shouldLimit) MaterialTheme.colorScheme.onSurface.copy(0.3f) else
                                MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.padding(vertical = 4.dp))
                        Text(
                            stringResource(R.string.note),
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (shouldLimit) MaterialTheme.colorScheme.onSurface.copy(0.3f) else
                                MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            stringResource(R.string.p_roll_create_ce_bomb_reminder),
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (shouldLimit) MaterialTheme.colorScheme.onSurface.copy(0.3f) else
                                MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Justify
                        )
                    }


                    Switch(
                        checked = shouldCreateCEBombAfterSummon,
                        onCheckedChange = { shouldCreateCEBombAfterSummon = it },
                        enabled = !shouldLimit && isCEBombSupported
                    )
                }
            }
            if (!isCEBombSupported){
                item {
                    Text(
                        text = stringResource(id = R.string.p_fp_gacha_ce_bomb_pr_for_other_servers),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
            item {
                Divider(modifier = Modifier.padding(vertical = 4.dp))
            }
            if (shouldCreateCEBombAfterSummon) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(1.dp)
                                .clickable(
                                    enabled = shouldCreateCEBombAfterSummon,
                                    onClick = {
                                        showCEBombSettings = !showCEBombSettings
                                    },
                                ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = when (showCEBombSettings) {
                                    true -> stringResource(R.string.p_fp_gacha_hide_ce_bomb_settings)
                                    false -> stringResource(R.string.p_fp_gacha_show_ce_bomb_settings)
                                },
                                style = labelTextSize(),
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        AnimatedVisibility(
                            visible = showCEBombSettings,
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(4.dp),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.Start
                                ) {
                                    sortSettings(
                                        skipSortDetection = skipSortDetection,
                                        onSkipSortDetectionChange = {
                                            skipSortDetection = !skipSortDetection
                                        }
                                    )
                                    skipReminder(
                                        show = skipSortDetection,
                                        text = stringResource(id = R.string.ce_bomb_skip_sort_reminder)
                                    )
                                    Divider(
                                        modifier = Modifier.padding(vertical = 1.dp, horizontal = 2.dp)
                                    )
                                    filterSettings(
                                        skipFilterDetection = skipFilterDetection,
                                        onSkipFilterDetectionChange = {
                                            skipFilterDetection = !skipFilterDetection
                                        }
                                    )
                                    skipReminder(
                                        show = skipFilterDetection,
                                        text = stringResource(id = R.string.ce_bomb_skip_filter_reminder)
                                    )
                                }
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(4.dp),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.Start
                                ) {
                                    setTargetRarityFilter(
                                        skipFilterDetection = skipFilterDetection,
                                        targetRarity = targetRarity,
                                        onTargetRarityChange = {
                                            targetRarity = it
                                        }
                                    )
                                    setFodderRarityFilter(
                                        skipFilterDetection = skipFilterDetection,
                                        fodderRarity = fodderRarity,
                                        onFodderRarityChange = {
                                            fodderRarity = it
                                        }
                                    )
                                }
                            }
                        }
                    }

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