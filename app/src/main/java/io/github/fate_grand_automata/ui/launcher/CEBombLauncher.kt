package io.github.fate_grand_automata.ui.launcher

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import io.github.fate_grand_automata.R
import io.github.fate_grand_automata.prefs.core.PrefsCore
import io.github.fate_grand_automata.scripts.prefs.IPreferences
import io.github.fate_grand_automata.ui.Stepper
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
        mutableStateOf(prefs.emptyEnhance)
    }
    var skipFilterDetection by prefsCore.craftEssence.skipCEFilterDetection.remember()

    var targetRarity by prefsCore.craftEssence.ceTargetRarity.remember()

    var fodderRarity by prefsCore.craftEssence.ceFodderRarity.remember()

    var skipAutomaticDisplayChange by prefsCore.craftEssence.skipAutomaticDisplayChange.remember()

    var skipSortDetection by prefsCore.craftEssence.skipSortDetection.remember()

    val canShowAutomaticDisplayChange by remember {
        mutableStateOf(prefs.craftEssence.canShowAutomaticDisplayChange)
    }

    var useDragging by prefsCore.craftEssence.useDragging.remember()

    var skipAutoLockTargetCE by prefsCore.craftEssence.skipAutoLockTargetCE.remember()

    val canUseLongPressed by remember {
        mutableStateOf(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
    }

    var longPressDuration by prefsCore.longPressDuration.remember()

    var dragDuration by prefsCore.dragDuration.remember()

    val longPressDurationDefault by remember {
        mutableStateOf(prefsCore.longPressDuration.defaultValue)
    }
    val dragDurationDefault by remember {
        mutableStateOf(prefsCore.dragDuration.defaultValue)
    }

    LaunchedEffect(key1 = fodderRarity, block = {
        if (fodderRarity.isEmpty()) {
            fodderRarity = setOf(1, 2)
        }
    })

    LaunchedEffect(key1 = Unit, block = {
        if (!canUseLongPressed) {
            useDragging = false
            skipAutoLockTargetCE = true
        }
    })

    Column(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .padding(top = 5.dp)
    ) {
        Text(
            text = when(isEmptyEnhance) {
                true -> stringResource(id = R.string.p_script_mode_ce_bomb)
                false -> stringResource(id = R.string.p_script_mode_ce_level)
            },
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
                    .weight(0.4f)
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
                        text = when(isEmptyEnhance) {
                            true -> stringResource(R.string.ce_bomb_explanation)
                            false -> stringResource(id = R.string.ce_level_explanation)
                        },
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
                    .weight(0.6f)
                    .scrollbar(
                        rightColumnState,
                        horizontal = false,
                        hiddenAlpha = 0.3f
                    ),
                state = rightColumnState,
            ) {
                if (canUseLongPressed) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {
                            Text(
                                text = stringResource(id = R.string.ce_bomb_selection_method).uppercase(),
                                style = bodyTextSize(),
                                modifier = Modifier.fillMaxWidth(),
                                textDecoration = TextDecoration.Underline
                            )
                            determineSelectionMethod(
                                useDragging = useDragging,
                                onUseDraggingChange = {
                                    useDragging = it
                                }
                            )
                        }
                    }
                    item {
                        longPressAndDragSettings(
                            useDragging = useDragging,
                            longPressDuration = longPressDuration,
                            longPressDurationDefault = longPressDurationDefault,
                            onLongPressDurationChange = {
                                longPressDuration = it
                            },
                            dragDuration = dragDuration,
                            dragDurationDefault = dragDurationDefault,
                            onDragDurationChange = {
                                dragDuration = it
                            },
                        )
                    }
                    item {
                        Divider()
                    }

                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    skipAutoLockTargetCE = !skipAutoLockTargetCE
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(id = R.string.ce_bomb_skip_auto_lock_of_target_ce),
                                    modifier = Modifier
                                        .weight(1f),
                                    style = bodyTextSize()
                                )
                                Checkbox(
                                    checked = skipAutoLockTargetCE,
                                    onCheckedChange = {
                                        skipAutoLockTargetCE = !skipAutoLockTargetCE
                                    },
                                )
                            }
                        }
                    }
                    item{
                        Divider()
                    }
                }

                if (canShowAutomaticDisplayChange) {
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
                                    text = stringResource(id = R.string.ce_bomb_skip_display_size_setup),
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
                        skipReminder(
                            show = skipAutomaticDisplayChange,
                            text = stringResource(id = R.string.ce_bomb_skip_display_size_reminder)
                        )
                    }
                } else {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = stringResource(id = R.string.ce_bomb_skip_display_size_warning),
                                style = bodyTextSize(),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                item {
                    Divider()
                }

                item {
                    sortSettings(
                        skipSortDetection = skipSortDetection,
                        onSkipSortDetectionChange = {
                            skipSortDetection = !skipSortDetection
                        }
                    )
                }
                item {
                    skipReminder(
                        show = skipSortDetection,
                        text = stringResource(id = R.string.ce_bomb_skip_sort_reminder)
                    )
                }
                item {
                    Divider()
                }

                item {
                    filterSettings(
                        skipFilterDetection = skipFilterDetection,
                        onSkipFilterDetectionChange = {
                            skipFilterDetection = !skipFilterDetection
                        }
                    )
                }
                item {
                    skipReminder(
                        show = skipFilterDetection,
                        text = stringResource(id = R.string.ce_bomb_skip_filter_reminder)
                    )
                }
                if (isEmptyEnhance) {
                    item {
                        setTargetRarityFilter(
                            skipFilterDetection = skipFilterDetection,
                            targetRarity = targetRarity,
                            onTargetRarityChange = {
                                targetRarity = it
                            }
                        )
                    }
                }
                item {
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

    return ScriptLauncherResponseBuilder(
        canBuild = { true },
        build = { ScriptLauncherResponse.CEBomb }
    )
}

@Composable
fun filterSettings(
    skipFilterDetection: Boolean,
    onSkipFilterDetectionChange: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = onSkipFilterDetectionChange
            ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(id = R.string.ce_bomb_skip_filter_setup),
                modifier = Modifier
                    .weight(1f),
                style = bodyTextSize()
            )
            Checkbox(
                checked = skipFilterDetection,
                onCheckedChange = {
                    onSkipFilterDetectionChange()
                },
            )
        }
    }
}

@Composable
fun sortSettings(
    skipSortDetection: Boolean,
    onSkipSortDetectionChange: () -> Unit = {}
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = onSkipSortDetectionChange
            )
    ) {
        Text(
            text = stringResource(id = R.string.ce_bomb_skip_sort_setup),
            style = bodyTextSize(),
            modifier = Modifier.weight(1f),
        )
        Checkbox(
            checked = skipSortDetection,
            onCheckedChange = {
                onSkipSortDetectionChange()
            },
        )
    }
}

@Composable
fun setFodderRarityFilter(
    skipFilterDetection: Boolean,
    fodderRarity: Set<Int>,
    onFodderRarityChange: (Set<Int>) -> Unit
) {
    Column {
        Text(
            text = stringResource(id = R.string.ce_bomb_fodder_rarity).uppercase(),
            modifier = Modifier,
            style = bodyTextSize(),
            color = if (skipFilterDetection) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
            else MaterialTheme.colorScheme.onSurface,
            textDecoration = TextDecoration.Underline
        )

        fodderRarityItems(
            modifier = Modifier,
            enabled = !skipFilterDetection,
            items = fodderRarity.toList(),
            onClick = onFodderRarityChange
        )
    }
}

@Composable
fun setTargetRarityFilter(
    skipFilterDetection: Boolean,
    targetRarity: Int,
    onTargetRarityChange: (Int) -> Unit
) {
    Column {
        Text(
            text = stringResource(id = R.string.ce_bomb_target_rarity).uppercase(),
            modifier = Modifier,
            style = bodyTextSize(),
            color = if (skipFilterDetection) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
            else MaterialTheme.colorScheme.onSurface,
            textDecoration = TextDecoration.Underline
        )

        targetRarityItems(
            selected = targetRarity,
            onClick = onTargetRarityChange,
            modifier = Modifier,
            enabled = !skipFilterDetection
        )
    }
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
                    .padding(horizontal = 4.dp, vertical = 1.dp)
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
                    .padding(horizontal = 4.dp, vertical = 1.dp)
            )
        }
    }
}

@Composable
private fun determineSelectionMethod(
    modifier: Modifier = Modifier,
    useDragging: Boolean,
    onUseDraggingChange: (Boolean) -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        listOf(true, false).forEach { input ->
            val isSelected = useDragging == input

            selectedItem(
                text = stringResource(
                    id = if (input) R.string.ce_bomb_dragging
                    else R.string.ce_bomb_clicking
                ).uppercase(),
                isSelected = isSelected,
                onClick = {
                    onUseDraggingChange(input)
                },
                modifier = Modifier
                    .padding(horizontal = 4.dp, vertical = 1.dp)
            )
        }
    }
}

@Composable
private fun longPressAndDragSettings(
    useDragging: Boolean,
    longPressDuration: Int,
    longPressDurationDefault: Int,
    onLongPressDurationChange: (Int) -> Unit,
    dragDuration: Int,
    dragDurationDefault: Int,
    onDragDurationChange: (Int) -> Unit,
) {
    var showDurationSettings by remember {
        mutableStateOf(false)
    }
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        if (useDragging) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 1.dp)
                    .clickable {
                        showDurationSettings = !showDurationSettings
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when (showDurationSettings) {
                        true -> stringResource(id = R.string.ce_bomb_hide_dragging_settings)
                        false -> stringResource(id = R.string.ce_bomb_show_dragging_settings)
                    },
                    style = labelTextSize(),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        AnimatedVisibility(
            visible = useDragging && showDurationSettings,
            enter = slideInVertically() + expandVertically(expandFrom = Alignment.Top) + fadeIn(initialAlpha = 0.3f),
            exit = slideOutVertically() + shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut()
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = stringResource(id = R.string.ce_bomb_duration).uppercase(),
                        style = labelTextSize(),
                        modifier = Modifier.weight(1f),
                        textDecoration = TextDecoration.Underline
                    )
                    TextButton(
                        onClick = {
                            onLongPressDurationChange(longPressDurationDefault)
                            onDragDurationChange(dragDurationDefault)
                        },
                        enabled = longPressDuration != longPressDurationDefault || dragDuration != dragDurationDefault
                    ) {
                        Text(
                            text = stringResource(id = R.string.reset_to_default)
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(id = R.string.ce_bomb_long_press),
                        style = labelTextSize(),
                        modifier = Modifier.weight(1f)
                    )
                    Stepper(
                        value = longPressDuration,
                        onValueChange = onLongPressDurationChange,
                        valueRange = 500..3000
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(id = R.string.ce_bomb_drag),
                        style = labelTextSize(),
                        modifier = Modifier.weight(1f)
                    )
                    Stepper(
                        value = dragDuration,
                        onValueChange = onDragDurationChange,
                        valueRange = 50..1000
                    )
                }
            }
        }
    }
}

@Composable
fun skipReminder(
    show: Boolean,
    text: String
) {
    AnimatedVisibility(
        visible = show,
        enter = slideInVertically() + expandVertically(expandFrom = Alignment.Top) + fadeIn(initialAlpha = 0.3f),
        exit = slideOutVertically() + shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut()
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = labelTextSize(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 1.dp, horizontal = 2.dp),
                textAlign = TextAlign.Justify,
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
            modifier = Modifier.padding(vertical = 1.dp, horizontal = 4.dp),
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