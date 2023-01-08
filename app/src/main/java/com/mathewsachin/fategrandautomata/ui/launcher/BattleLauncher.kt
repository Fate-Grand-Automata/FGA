package com.mathewsachin.fategrandautomata.ui.launcher

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.scripts.enums.GameServerEnum
import com.mathewsachin.fategrandautomata.scripts.enums.RefillResourceEnum
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences
import com.mathewsachin.fategrandautomata.ui.Stepper
import com.mathewsachin.fategrandautomata.ui.scrollbar
import com.mathewsachin.fategrandautomata.util.stringRes

@Composable
fun battleLauncher(
    prefs: IPreferences,
    modifier: Modifier = Modifier
): ScriptLauncherResponseBuilder {
    val configs = remember {
        prefs.battleConfigs
            .filter {
                it.server == null || it.server == prefs.gameServer
            }
    }
    var selectedConfigIndex by remember { mutableStateOf(configs.indexOf(prefs.selectedBattleConfig)) }
    var refillResources by remember { mutableStateOf(prefs.refill.resources.toSet()) }
    var refillCount by remember { mutableStateOf(prefs.refill.repetitions) }
    var shouldLimitRuns by remember { mutableStateOf(prefs.refill.shouldLimitRuns) }
    var limitRuns by remember { mutableStateOf(prefs.refill.limitRuns) }
    var shouldLimitMats by remember { mutableStateOf(prefs.refill.shouldLimitMats) }
    var limitMats by remember { mutableStateOf(prefs.refill.limitMats) }
    var shouldLimitCEs by remember { mutableStateOf(prefs.refill.shouldLimitCEs) }
    var limitCEs by remember { mutableStateOf(prefs.refill.limitCEs) }
    var waitApRegen by remember { mutableStateOf(prefs.waitAPRegen) }

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
                    fixedKnobRatio = 0.69f
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
                        value = refillCount,
                        onValueChange = { refillCount = it },
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
                    //only display bronze option for JP and CN
                    val bronzeApplesEnabled = prefs.gameServer in listOf(GameServerEnum.Jp, GameServerEnum.Cn)
                    if (!bronzeApplesEnabled) {
                        //disable it in the settings otherwise
                        refillResources = refillResources.minus(RefillResourceEnum.Bronze)
                    }
                    //TODO remove
                    if (refillResources.size > 1) {
                        refillResources = setOf(refillResources.first())
                    }
                    val availableRefills = RefillResourceEnum.values()
                        .filter { it != RefillResourceEnum.Bronze || bronzeApplesEnabled }
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

                Divider(modifier = Modifier.padding(top = 10.dp, bottom = 16.dp))
            }

            item {
                Text(
                    stringResource(R.string.p_limit),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
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

    return ScriptLauncherResponseBuilder(
        canBuild = { selectedConfigIndex != -1 },
        build = {
            ScriptLauncherResponse.Battle(
                config = configs[selectedConfigIndex],
                refillResources = refillResources,
                refillCount = refillCount,
                limitRuns = if (shouldLimitRuns) limitRuns else null,
                limitMats = if (shouldLimitMats) limitMats else null,
                limitCEs = if (shouldLimitCEs) limitCEs else null,
                waitApRegen = waitApRegen
            )
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