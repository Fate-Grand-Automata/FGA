package io.github.fate_grand_automata.ui.launcher

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.fate_grand_automata.R
import io.github.fate_grand_automata.scripts.enums.GameServer
import io.github.fate_grand_automata.scripts.enums.RefillResourceEnum
import io.github.fate_grand_automata.scripts.prefs.IPreferences
import io.github.fate_grand_automata.ui.Stepper
import io.github.fate_grand_automata.ui.scrollbar
import io.github.fate_grand_automata.util.stringRes

@Composable
fun battleLauncher(
    prefs: IPreferences,
    modifier: Modifier = Modifier
): ScriptLauncherResponseBuilder {
    val configs = remember {
        prefs.battleConfigs
            .filter {
                when (it.server) {
                    // always show if no server is set
                    null -> true
                    // ignore betterFgo for En and Jp
                    is GameServer.En -> prefs.gameServer is GameServer.En
                    is GameServer.Jp -> prefs.gameServer is GameServer.Jp
                    GameServer.Cn, GameServer.Kr, GameServer.Tw -> it.server == prefs.gameServer
                }
            }
    }
    var selectedConfigIndex by remember { mutableIntStateOf(configs.indexOf(prefs.selectedBattleConfig)) }

    val perServerConfigPref by remember {
        mutableStateOf(
            prefs.getPerServerConfigPref(prefs.gameServer)
        )
    }

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
    var sendSupportFriendRequest by remember { mutableStateOf(perServerConfigPref.sendSupportFriendRequest) }

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
            perServerConfigPref.sendSupportFriendRequest = sendSupportFriendRequest
            if (refillResources.isNotEmpty()) {
                perServerConfigPref.selectedApple = refillResources.first()
            }
            perServerConfigPref.updateResources(refillResources)

            if (selectedConfigIndex > -1) {
                prefs.selectedBattleConfig = configs[selectedConfigIndex]
            }
        }
    }

    LaunchedEffect(shouldLimitRuns, limitRuns, shouldLimitMats, limitMats, shouldLimitCEs, limitCEs) {
        resetAllButton = shouldLimitRuns || limitRuns > 1 ||
                shouldLimitMats || limitMats > 1 ||
                shouldLimitCEs || limitCEs > 1
    }

    Row(
        modifier = modifier
            .padding(start = 5.dp, end = 5.dp, top = 5.dp)
    ) {
        if (configs.isNotEmpty()) {
            // Scrolling the selected config into view
            val configListState = rememberLazyListState()
            LaunchedEffect(true) {
                if (selectedConfigIndex != -1) {
                    configListState.scrollToItem(selectedConfigIndex)
                }
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .scrollbar(
                        state = configListState,
                        hiddenAlpha = 0.3f,
                        horizontal = false,
                        knobColor = MaterialTheme.colorScheme.secondary
                    ),
                state = configListState
            ) {
                itemsIndexed(configs) { index, item ->
                    BattleConfigItem(
                        name = item.name,
                        isSelected = selectedConfigIndex == index,
                        onSelected = { selectedConfigIndex = index }
                    )
                }
            }
        } else {
            Text(
                stringResource(R.string.battle_config_list_no_items),
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxHeight()
                .padding(5.dp, 2.dp)
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
                .width(1.dp)
        ) { }

        val mainConfigState = rememberLazyListState()
        LazyColumn(
            modifier = Modifier
                .weight(1.5f)
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

            item {
                HorizontalDivider(modifier = Modifier.padding(top = 10.dp, bottom = 16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { sendSupportFriendRequest = !sendSupportFriendRequest }
                ) {
                    Checkbox(
                        checked = sendSupportFriendRequest,
                        onCheckedChange = { sendSupportFriendRequest = it },
                        modifier = Modifier
                            .alpha(if (sendSupportFriendRequest) 1f else 0.7f)
                            .padding(end = 5.dp)
                    )

                    Text(
                        stringResource(R.string.p_send_support_friend_request),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }


        }
    }

    return ScriptLauncherResponseBuilder(
        canBuild = { selectedConfigIndex != -1 },
        build = {
            ScriptLauncherResponse.Battle
        }
    )
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
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable { onShouldLimitChange(!shouldLimit) }
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
fun BattleConfigItem(
    name: String,
    isSelected: Boolean,
    onSelected: () -> Unit,
) {
    Row(
        modifier = Modifier
            .padding(3.dp)
            .background(
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = MaterialTheme.shapes.medium
            )
            .clickable(onClick = onSelected)
            .padding(11.dp, 3.dp)
            .fillMaxWidth()
    ) {
        Text(
            name,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else Color.Unspecified
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