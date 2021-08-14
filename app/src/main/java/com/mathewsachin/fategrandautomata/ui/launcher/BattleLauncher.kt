package com.mathewsachin.fategrandautomata.ui.launcher

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.Checkbox
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.scripts.enums.RefillResourceEnum
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences
import com.mathewsachin.fategrandautomata.ui.Stepper
import com.mathewsachin.fategrandautomata.util.stringRes
import com.mathewsachin.fategrandautomata.util.toggle

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
                modifier = Modifier.weight(1f),
                state = configListState
            ) {
                itemsIndexed(configs) { index, item ->
                    BattleConfigItem(
                        name = item.name,
                        isSelected = selectedConfigIndex == index,
                        onSelected = { selectedConfigIndex = index }
                    )
                }

                item {
                    Spacer(modifier.padding(16.dp))
                }
            }
        }
        else {
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
                .background(MaterialTheme.colors.onSurface.copy(alpha = 0.12f))
                .width(1.dp)
        ) { }

        Column(
            modifier = Modifier
                .weight(1.5f)
                .verticalScroll(rememberScrollState())
                .padding(start = 5.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "${stringResource(R.string.p_refill)}:",
                    style = MaterialTheme.typography.body2,
                    color = MaterialTheme.colors.secondary
                )

                Stepper(
                    value = refillCount,
                    onValueChange = { refillCount = it },
                    valueRange = 0..999,
                    enabled = refillResources.isNotEmpty()
                )
            }

            LazyRow(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                items(RefillResourceEnum.values()) {
                    it.RefillResource(
                        isSelected = it in refillResources,
                        toggle = {
                            refillResources = refillResources.toggle(it)
                        }
                    )
                }
            }

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
                    style = MaterialTheme.typography.body2
                )
            }

            Divider(modifier = Modifier.padding(top = 10.dp, bottom = 16.dp))

            Text(
                stringResource(R.string.p_limit),
                style = MaterialTheme.typography.body2,
                color = MaterialTheme.colors.secondary
            )

            LimitItem(
                shouldLimit = shouldLimitRuns,
                onShouldLimitChange = { shouldLimitRuns = it },
                text = stringResource(R.string.p_runs),
                count = limitRuns,
                onCountChange = { limitRuns = it }
            )

            LimitItem(
                shouldLimit = shouldLimitMats,
                onShouldLimitChange = { shouldLimitMats = it },
                text = stringResource(R.string.p_mats),
                count = limitMats,
                onCountChange = { limitMats = it }
            )
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
    Row (
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
                    .padding(end = 5.dp)
                    .alpha(if (shouldLimit) 1f else 0.7f)
            )

            Text(
                "$text:",
                style = MaterialTheme.typography.body2
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
                color = if (isSelected) MaterialTheme.colors.primary else Color.Transparent,
                shape = MaterialTheme.shapes.medium
            )
            .clickable(onClick = onSelected)
            .padding(11.dp, 3.dp)
            .fillMaxWidth()
    ) {
        Text(
            name,
            color = if (isSelected) MaterialTheme.colors.onPrimary else Color.Unspecified
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
                brush = SolidColor(if (isSelected) MaterialTheme.colors.primary else Color.Transparent),
                shape = MaterialTheme.shapes.medium
            )
            .clickable(onClick = toggle)
            .padding(5.dp, 3.dp)
    ) {
        Text(
            stringResource(stringRes),
            style = MaterialTheme.typography.overline,
            color = if (isSelected) MaterialTheme.colors.primary else Color.Unspecified
        )
    }
}