package com.mathewsachin.fategrandautomata.ui.prefs

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.scripts.enums.RefillResourceEnum
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences
import com.mathewsachin.fategrandautomata.util.stringRes

@Composable
fun battleLauncher(
    prefs: IPreferences,
    modifier: Modifier = Modifier
): ScriptLauncherResponseBuilder {
    val configs = remember { prefs.battleConfigs }
    var selectedConfigIndex by remember { mutableStateOf(configs.indexOf(prefs.selectedBattleConfig)) }
    var refillResources by remember { mutableStateOf(prefs.refill.resources.toSet()) }
    var refillCount by remember { mutableStateOf(prefs.refill.repetitions) }
    var shouldLimitRuns by remember { mutableStateOf(prefs.refill.shouldLimitRuns) }
    var limitRuns by remember { mutableStateOf(prefs.refill.limitRuns) }
    var shouldLimitMats by remember { mutableStateOf(prefs.refill.shouldLimitMats) }
    var limitMats by remember { mutableStateOf(prefs.refill.limitMats) }

    Row(modifier = modifier) {
        if (configs.isNotEmpty()) {
            // Scrolling the selected config into view
            val configListState = rememberLazyListState()
            LaunchedEffect(true) {
                if (selectedConfigIndex != -1) {
                    configListState.snapToItemIndex(selectedConfigIndex)
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
                .preferredWidth(1.dp)
        ) { }

        Column(
            modifier = Modifier
                .weight(1f)
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

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                RefillResourceEnum.values().map {
                    it.RefillResource(
                        isSelected = it in refillResources,
                        toggle = {
                            refillResources = refillResources.toggle(it)
                        }
                    )
                }
            }

            Divider(modifier = Modifier.padding(top = 10.dp, bottom = 16.dp))

            Row (
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { shouldLimitRuns = !shouldLimitRuns }
            ) {
                Text(
                    stringResource(R.string.p_run_limit),
                    style = MaterialTheme.typography.body2,
                    color = MaterialTheme.colors.secondary
                )

                Switch(
                    checked = shouldLimitRuns,
                    onCheckedChange = { shouldLimitRuns = it }
                )
            }

            Box(
                modifier = Modifier.align(Alignment.End)
            ) {
                Stepper(
                    value = limitRuns,
                    onValueChange = { limitRuns = it },
                    valueRange = 1..999,
                    enabled = shouldLimitRuns
                )
            }

            Divider(modifier = Modifier.padding(bottom = 16.dp))

            Row (
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { shouldLimitMats = !shouldLimitMats }
            ) {
                Text(
                    stringResource(R.string.p_mat_limit),
                    style = MaterialTheme.typography.body2,
                    color = MaterialTheme.colors.secondary
                )

                Switch(
                    checked = shouldLimitMats,
                    onCheckedChange = { shouldLimitMats = it }
                )
            }

            Box(
                modifier = Modifier.align(Alignment.End)
            ) {
                Stepper(
                    value = limitMats,
                    onValueChange = { limitMats = it },
                    valueRange = 1..999,
                    enabled = shouldLimitMats
                )
            }
        }
    }

    return ScriptLauncherResponseBuilder(
        canBuild = { selectedConfigIndex != -1 },
        build = {
            ScriptLauncherResponse.Battle(
                configs[selectedConfigIndex],
                refillResources,
                refillCount,
                if (shouldLimitRuns) limitRuns else null,
                if (shouldLimitMats) limitMats else null
            )
        }
    )
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
            .border(
                width = 1.dp,
                brush = SolidColor(if (isSelected) MaterialTheme.colors.primary else Color.Transparent),
                shape = MaterialTheme.shapes.medium
            )
            .clickable(onClick = onSelected)
            .padding(5.dp, 3.dp)
            .fillMaxWidth()
    ) {
        Text(
            name,
            color = if (isSelected) MaterialTheme.colors.primary else Color.Unspecified
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

fun <T> Set<T>.toggle(item: T) =
    if (item in this)
        this - item
    else this + item