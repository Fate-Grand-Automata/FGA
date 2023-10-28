package io.github.fate_grand_automata.ui.launcher

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.fate_grand_automata.R
import io.github.fate_grand_automata.prefs.core.PrefsCore
import io.github.fate_grand_automata.scripts.prefs.IPreferences
import io.github.fate_grand_automata.ui.VerticalDivider
import io.github.fate_grand_automata.ui.prefs.remember
import io.github.fate_grand_automata.ui.scrollbar

@Composable
fun ceBombLauncher(
    prefsCore: PrefsCore,
    prefs: IPreferences,
    modifier: Modifier = Modifier
): ScriptLauncherResponseBuilder {

    val isEmptyEnhance by remember {
        mutableStateOf(prefs.craftEssence.emptyEnhance)
    }
    var skipFilterDetection by prefsCore.craftEssence.skipCEFilterDetection.remember()

    var targetRarity by prefsCore.craftEssence.ceTargetRarity.remember()

    var fodderRarity by prefsCore.craftEssence.ceFodderRarity.remember()

    var skipAutomaticDisplayChange by prefsCore.craftEssence.skipAutomaticDisplayChange.remember()

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
            stringResource(R.string.p_script_mode_ce_bomb),
            style = MaterialTheme.typography.titleLarge
        )

        Divider(
            modifier = Modifier
                .padding(5.dp)
                .padding(bottom = 8.dp)
        )
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val leftColumnState = rememberLazyListState()
            LazyColumn(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .weight(1f)
                    .scrollbar(
                        state = leftColumnState,
                        horizontal = false,
                        fadeOutAnimationDurationMs = 100,
                        fadeOutAnimationDelayMs = 50
                    ),
                state = leftColumnState,
            ) {
                item {
                    Text(
                        stringResource(R.string.p_ce_bomb_explanation),
                        style = bodyTextSize(),
                        textAlign = TextAlign.Justify
                    )
                }
            }
            VerticalDivider()

            val rightColumnState = rememberLazyListState()
            LazyColumn(
                modifier = Modifier
                    .padding(start = 4.dp)
                    .weight(1f)
                    .scrollbar(rightColumnState, horizontal = false),
                state = rightColumnState,
            ) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                skipAutomaticDisplayChange = !skipAutomaticDisplayChange
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(id = R.string.p_ce_bomb_skip_automatic_display_change),
                                modifier = Modifier
                                    .weight(1f),
                                style = bodyTextSize()
                            )
                            Checkbox(
                                checked = skipAutomaticDisplayChange,
                                onCheckedChange = {
                                    skipAutomaticDisplayChange = !skipAutomaticDisplayChange
                                },
                            )
                        }
                    }
                }
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                skipFilterDetection = !skipFilterDetection
                            },
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(id = R.string.p_ce_bomb_skip_filter_detection),
                                modifier = Modifier
                                    .weight(1f),
                                style = bodyTextSize()
                            )
                            Checkbox(
                                checked = skipFilterDetection,
                                onCheckedChange = {
                                    skipFilterDetection = !skipFilterDetection
                                },
                            )
                        }
                        Text(
                            text = stringResource(id = R.string.p_ce_bomb_skip_filter_summary),
                            style = labelTextSize(),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                }
                item {
                    Divider()
                }
                if (isEmptyEnhance) {
                    item {
                        Column {
                            Text(
                                text = stringResource(id = R.string.p_ce_bomb_target_rarity).uppercase(),
                                modifier = Modifier,
                                style = bodyTextSize(),
                                color = if (skipFilterDetection) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                                else MaterialTheme.colorScheme.onSurface
                            )

                            targetRarityItems(
                                selected = targetRarity,
                                onClick = {
                                    targetRarity = it
                                },
                                modifier = Modifier,
                                enabled = !skipFilterDetection
                            )
                        }
                    }
                }
                item {
                    Column {
                        Text(
                            text = stringResource(id = R.string.p_ce_bomb_fodder_rarity).uppercase(),
                            modifier = Modifier,
                            style = bodyTextSize(),
                            color = if (skipFilterDetection) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                            else MaterialTheme.colorScheme.onSurface
                        )

                        fodderRarityItems(
                            modifier = Modifier,
                            enabled = !skipFilterDetection,
                            items = fodderRarity.toList(),
                            onClick = {
                                fodderRarity = it
                            }
                        )
                    }
                }
            }
        }
    }

    return ScriptLauncherResponseBuilder(
        canBuild = { true },
        build = { ScriptLauncherResponse.CEBomb }
    )
}

@Composable
fun targetRarityItems(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    selected: Int,
    onClick: (Int) -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        (1..2).map { item ->
            val isSelected = item == selected
            selectedItem(
                text = stringResource(id = R.string.rarity, item),
                enabled = enabled,
                isSelected = isSelected,
                onClick = {
                    if (enabled) {
                        onClick(item)
                    }
                },
                modifier = Modifier
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
fun fodderRarityItems(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    items: List<Int>,
    onClick: (Set<Int>) -> Unit
) {

    Row(
        modifier = modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        (1..3).map { item ->
            val isSelected = items.contains(item)

            selectedItem(
                text = stringResource(id = R.string.rarity, item),
                enabled = enabled,
                isSelected = isSelected,
                onClick = {
                    if (enabled) {
                        if (isSelected && items.size >= 2) {
                            onClick(items.filterNot { it == item }.toSet())
                        } else {
                            onClick(items.toSet() + item)
                        }
                    }
                },
                modifier = Modifier
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
private fun selectedItem(
    modifier: Modifier = Modifier,
    text: String,
    enabled: Boolean = true,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(15),
        colors = CardDefaults.cardColors(
            containerColor = when {
                !enabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                isSelected -> MaterialTheme.colorScheme.primaryContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(vertical = 2.dp, horizontal = 4.dp),
            style = labelTextSize(),
            color = if (enabled) MaterialTheme.colorScheme.onSurface
            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
        )
    }
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