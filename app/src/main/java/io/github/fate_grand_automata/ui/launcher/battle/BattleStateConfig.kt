package io.github.fate_grand_automata.ui.launcher.battle

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import io.github.fate_grand_automata.R
import io.github.fate_grand_automata.scripts.enums.RefillResourceEnum
import io.github.fate_grand_automata.scripts.prefs.IPerServerConfigPrefs
import io.github.fate_grand_automata.scripts.prefs.IPreferences
import io.github.fate_grand_automata.ui.Stepper
import io.github.fate_grand_automata.ui.padding
import io.github.fate_grand_automata.ui.scrollbar
import io.github.fate_grand_automata.util.stringRes
import kotlinx.coroutines.launch

@Composable
fun BattleStateConfig(
    modifier: Modifier = Modifier,
    perServerConfigPref: IPerServerConfigPrefs,
    prefs: IPreferences
) {
    val pagerState = rememberPagerState {
        2
    }
    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxHeight(),
        verticalArrangement = Arrangement.Top
    ) {
        HorizontalPager(
            modifier = Modifier.weight(1f),
            state = pagerState,
            userScrollEnabled = false
        ) { page ->
            when (page) {
                0 -> {
                    Box(
                        modifier = Modifier.clipToBounds()
                    ) {
                        BattleConfigRunState(
                            modifier = Modifier,
                            perServerConfigPref = perServerConfigPref,
                            prefs = prefs
                        )
                    }
                }

                1 -> {
                    Box(
                        modifier = Modifier.clipToBounds()
                    ) {
                        BattleConfigSettings(
                            modifier = Modifier,
                            perServerConfigPref = perServerConfigPref,
                            prefs = prefs
                        )
                    }
                }
            }
        }
        HorizontalDivider()
        pagerTabRow(
            currentPage = pagerState.currentPage,
            onPageChange = { page ->
                scope.launch {
                    pagerState.animateScrollToPage(page)
                }
            }
        )
    }
}

@Composable
private fun BattleConfigRunState(
    modifier: Modifier = Modifier,
    perServerConfigPref: IPerServerConfigPrefs,
    prefs: IPreferences
) {
    var refillResources by remember { mutableStateOf(perServerConfigPref.resources.toSet()) }
    val hideSQInAPResources by remember { mutableStateOf(prefs.hideSQInAPResources) }
    if (hideSQInAPResources) {
        refillResources = refillResources.minus(RefillResourceEnum.SQ)
    }

    //TODO remove
    if (refillResources.size > 1) {
        refillResources = setOf(refillResources.first())
    }

    val availableRefills = RefillResourceEnum.entries
        .filterNot { it == RefillResourceEnum.SQ && hideSQInAPResources }

    var copperApple by remember { mutableIntStateOf(perServerConfigPref.copperApple) }
    var blueApple by remember { mutableIntStateOf(perServerConfigPref.blueApple) }
    var silverApple by remember { mutableIntStateOf(perServerConfigPref.silverApple) }
    var goldApple by remember { mutableIntStateOf(perServerConfigPref.goldApple) }
    var rainbowApple by remember { mutableIntStateOf(perServerConfigPref.rainbowApple) }

    val refillCount by remember {
        mutableStateOf(
            derivedStateOf {
                when {
                    RefillResourceEnum.Copper in refillResources -> copperApple
                    RefillResourceEnum.Bronze in refillResources -> blueApple
                    RefillResourceEnum.Silver in refillResources -> silverApple
                    RefillResourceEnum.Gold in refillResources -> goldApple
                    RefillResourceEnum.SQ in refillResources -> rainbowApple
                    else -> 0
                }
            }
        )
    }

    var shouldLimitRuns by remember { mutableStateOf(perServerConfigPref.shouldLimitRuns) }
    var limitRuns by remember { mutableIntStateOf(perServerConfigPref.limitRuns) }
    var shouldLimitMats by remember { mutableStateOf(perServerConfigPref.shouldLimitMats) }
    var limitMats by remember { mutableIntStateOf(perServerConfigPref.limitMats) }
    var shouldLimitCEs by remember { mutableStateOf(perServerConfigPref.shouldLimitCEs) }
    var limitCEs by remember { mutableIntStateOf(perServerConfigPref.limitCEs) }
    var waitApRegen by remember { mutableStateOf(perServerConfigPref.waitForAPRegen) }

    var resetAllButton by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose {
            perServerConfigPref.shouldLimitRuns = shouldLimitRuns
            perServerConfigPref.limitRuns = limitRuns
            perServerConfigPref.shouldLimitMats = shouldLimitMats
            perServerConfigPref.limitMats = limitMats
            perServerConfigPref.shouldLimitCEs = shouldLimitCEs
            perServerConfigPref.limitCEs = limitCEs
            perServerConfigPref.copperApple = copperApple
            perServerConfigPref.blueApple = blueApple
            perServerConfigPref.silverApple = silverApple
            perServerConfigPref.goldApple = goldApple
            perServerConfigPref.rainbowApple = rainbowApple
            perServerConfigPref.waitForAPRegen = waitApRegen
            if (refillResources.isNotEmpty()) {
                perServerConfigPref.selectedApple = refillResources.first()
            }
            perServerConfigPref.updateResources(refillResources)
        }
    }

    LaunchedEffect(shouldLimitRuns, limitRuns, shouldLimitMats, limitMats, shouldLimitCEs, limitCEs) {
        resetAllButton = shouldLimitRuns || limitRuns > 1 ||
                shouldLimitMats || limitMats > 1 ||
                shouldLimitCEs || limitCEs > 1
    }

    val mainConfigState = rememberLazyListState()
    LazyColumn(
        modifier = modifier
            .scrollbar(
                state = mainConfigState,
                hiddenAlpha = 0.3f,
                horizontal = false,
                knobColor = MaterialTheme.colorScheme.secondary,
                // needs to be adjusted when adding new items
                fixedKnobRatio = 0.70f
            )
            .padding(start = 5.dp),
        state = mainConfigState
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "${stringResource(R.string.p_refill)}:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )

                Stepper(
                    value = refillCount.value,
                    onValueChange = { value ->
                        when {
                            RefillResourceEnum.Copper in refillResources -> copperApple = value
                            RefillResourceEnum.Bronze in refillResources -> blueApple = value
                            RefillResourceEnum.Silver in refillResources -> silverApple = value
                            RefillResourceEnum.Gold in refillResources -> goldApple = value
                            RefillResourceEnum.SQ in refillResources -> rainbowApple = value
                        }
                    },
                    valueRange = 0..999,
                    enabled = refillResources.isNotEmpty()
                )
            }
        }

        item {
            LazyRow(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {

                items(availableRefills) {
                    it.RefillResource(
                        isSelected = it in refillResources,
                        toggle = {
                            // TODO change back to refillResources.toggle()

                            // if the tapped resource is the only one in the list, disable it. otherwise only select the tapped resource
                            refillResources = if (it in refillResources) emptySet() else setOf(it)
                        }
                    )
                }
            }
        }

        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable { waitApRegen = !waitApRegen }
            ) {
                Checkbox(
                    checked = waitApRegen,
                    onCheckedChange = { waitApRegen = it },
                    modifier = Modifier
                        .alpha(if (waitApRegen) 1f else 0.7f)
                        .padding(end = 5.dp)
                )

                Text(
                    stringResource(R.string.p_wait_ap_regen_text),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            HorizontalDivider(modifier = Modifier.padding(top = 10.dp, bottom = 16.dp))
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.p_limit).uppercase(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp)
                )
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Card(
                        shape = CircleShape,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        ),
                        enabled = resetAllButton,
                        onClick = {
                            shouldLimitRuns = false
                            limitRuns = 1
                            shouldLimitCEs = false
                            limitCEs = 1
                            shouldLimitMats = false
                            limitMats = 1
                        }
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .padding(10.dp, 4.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.reset_all).uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                textAlign = TextAlign.Center,
                                color = if (resetAllButton) MaterialTheme.colorScheme.onSecondaryContainer
                                else MaterialTheme.colorScheme.onSecondaryContainer.copy(0.38f)
                            )
                        }
                    }
                }

            }

        }

        item {
            LimitItem(
                shouldLimit = shouldLimitRuns,
                onShouldLimitChange = { shouldLimitRuns = it },
                text = stringResource(R.string.p_runs),
                count = limitRuns,
                onCountChange = { limitRuns = it }
            )
        }

        item {
            LimitItem(
                shouldLimit = shouldLimitMats,
                onShouldLimitChange = { shouldLimitMats = it },
                text = stringResource(R.string.p_mats),
                count = limitMats,
                onCountChange = { limitMats = it }
            )
        }

        item {
            LimitItem(
                shouldLimit = shouldLimitCEs,
                onShouldLimitChange = { shouldLimitCEs = it },
                text = stringResource(R.string.p_ces),
                count = limitCEs,
                onCountChange = { limitCEs = it }
            )
        }
    }
}

@Composable
private fun BattleConfigSettings(
    modifier: Modifier = Modifier,
    perServerConfigPref: IPerServerConfigPrefs,
    prefs: IPreferences
) {
    var debugMode by remember { mutableStateOf(prefs.debugMode) }
    var exitOnOutOfCommands by remember { mutableStateOf(perServerConfigPref.exitOnOutOfCommands) }
    var storySkip by remember { mutableStateOf(prefs.storySkip) }

    DisposableEffect(Unit) {
        onDispose {
            prefs.debugMode = debugMode
            perServerConfigPref.exitOnOutOfCommands = exitOnOutOfCommands
            prefs.storySkip = storySkip
        }
    }

    val lazyState = rememberLazyListState()
    LazyColumn(
        modifier = modifier
            .fillMaxHeight()
            .scrollbar(
                state = lazyState,
                hiddenAlpha = 0.3f,
                horizontal = false,
                knobColor = MaterialTheme.colorScheme.secondary,
                // needs to be adjusted when adding new items
                fixedKnobRatio = 0.70f
            )
            .padding(start = 5.dp),
    ) {
        item {
            SettingsCheckBox(
                settings = exitOnOutOfCommands,
                text = stringResource(R.string.p_exit_on_out_of_commands),
                supportingText = stringResource(R.string.p_exit_on_out_of_commands_summary),
                onSettingsChange = { exitOnOutOfCommands = it }
            )
        }
        item {
            HorizontalDivider()
        }
        item {
            SettingsCheckBox(
                settings = storySkip,
                text = stringResource(R.string.p_story_skip),
                onSettingsChange = { storySkip = it }
            )
        }
        item {
            SettingsCheckBox(
                settings = debugMode,
                text = stringResource(R.string.p_debug_mode),
                onSettingsChange = { debugMode = it }
            )
        }
    }
}


@Composable
fun LimitItem(
    shouldLimit: Boolean,
    onShouldLimitChange: (Boolean) -> Unit,
    text: String,
    count: Int,
    onCountChange: (Int) -> Unit,
    valueRange: IntRange = 1..999
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onShouldLimitChange(!shouldLimit) }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
        ) {
            Checkbox(
                checked = shouldLimit,
                onCheckedChange = onShouldLimitChange,
                modifier = Modifier
                    .alpha(if (shouldLimit) 1f else 0.7f)
            )

            Text(
                "$text:",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Stepper(
            value = count,
            onValueChange = onCountChange,
            valueRange = valueRange,
            enabled = shouldLimit
        )
    }
}

@Composable
fun RefillResourceEnum.RefillResource(
    isSelected: Boolean,
    toggle: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .padding(vertical = 3.dp)
            .padding(end = 6.dp)
            .border(
                width = 1.dp,
                brush = SolidColor(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent),
                shape = MaterialTheme.shapes.medium
            )
            .clickable(onClick = toggle)
            .padding(5.dp, 3.dp)
    ) {
        Text(
            stringResource(stringRes),
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Unspecified
        )
    }
}

@Composable
private fun pagerTabRow(
    currentPage: Int,
    onPageChange: (Int) -> Unit,
) {
    Row(
        modifier = Modifier
            .height(IntrinsicSize.Max)
            .padding(
                vertical = MaterialTheme.padding.smallest
            )
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        pagerTab(
            modifier = animatedTab(isSelected = currentPage == 0),
            text = stringResource(R.string.p_battle_launcher_pager_run_options),
            imageVector = Icons.AutoMirrored.Filled.DirectionsRun,
            isActive = currentPage == 0,
            onClick = {
                onPageChange(0)
            }
        )
        VerticalDivider()

        pagerTab(
            modifier = animatedTab(isSelected = currentPage == 2),
            text = stringResource(R.string.p_battle_launcher_pager_more_settings),
            isActive = currentPage == 1,
            imageVector = Icons.Filled.Settings,
            onClick = {
                onPageChange(1)
            }
        )
    }
}

private fun RowScope.animatedTab(
    isSelected: Boolean
): Modifier = Modifier
    .weight(
        if (isSelected) 2f else 1f
    )
    .animateContentSize()

@Composable
private fun pagerTab(
    modifier: Modifier = Modifier,
    text: String,
    imageVector: ImageVector,
    isActive: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
            .clickable(onClick = onClick)
    ) {
        val slideIn = slideInHorizontally(initialOffsetX = { width -> width }) +
                expandHorizontally(
                    expandFrom = Alignment.End,
                    initialWidth = { w -> w }
                )
        val slideOut = slideOutHorizontally(
            targetOffsetX = { width -> -width }) +
                shrinkHorizontally(
                    shrinkTowards = Alignment.Start,
                    animationSpec = spring(
                        stiffness = Spring.StiffnessMediumLow,
                        visibilityThreshold = IntSize.VisibilityThreshold
                    )
                )
        AnimatedContent(
            targetState = isActive,
            label = "Change from text to icon",
            transitionSpec = {
                slideIn togetherWith (slideOut)
            },
            contentAlignment = Alignment.Center
        ) { active ->
            when (active) {
                true -> {
                    Text(
                        text = text.uppercase(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary,
                        maxLines = 1
                    )
                }

                false -> {
                    Icon(
                        imageVector = imageVector,
                        contentDescription = "icon for the tab"
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsCheckBox(
    settings: Boolean,
    text: String,
    supportingText: String = "",
    onSettingsChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onSettingsChange(!settings)
            }
    ) {
        Checkbox(
            checked = settings,
            onCheckedChange = {
                onSettingsChange(it)
            },
            modifier = Modifier
        )
        Column(
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
            )
            if (supportingText.isNotEmpty()) {
                Text(
                    text = supportingText,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}