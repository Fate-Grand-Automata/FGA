package io.github.fate_grand_automata.ui.launcher

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Switch
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import io.github.fate_grand_automata.R
import io.github.fate_grand_automata.scripts.prefs.IPreferences
import io.github.fate_grand_automata.ui.Stepper


@Composable
fun skillLauncher(
    prefs: IPreferences,
    modifier: Modifier = Modifier
): ScriptLauncherResponseBuilder {

    val skillUpgrade = prefs.skillUpgrade

    var shouldUpgrade1 by remember {
        mutableStateOf(false)
    }
    val minSkill1 by remember {
        mutableStateOf(skillUpgrade.minSkill1)
    }
    var upgradeSkill1 by remember {
        mutableStateOf(0)
    }
    var shouldUpgrade2 by remember {
        mutableStateOf(false)
    }
    val minSkill2 by remember {
        mutableStateOf(skillUpgrade.minSkill2)
    }
    var upgradeSkill2 by remember {
        mutableStateOf(0)
    }
    val skill2Available by remember {
        mutableStateOf(skillUpgrade.skill2Available)
    }

    var shouldUpgrade3 by remember {
        mutableStateOf(false)
    }
    val minSkill3 by remember {
        mutableStateOf(skillUpgrade.minSkill3)
    }
    var upgradeSkill3 by remember {
        mutableStateOf(0)
    }
    val skill3Available by remember {
        mutableStateOf(skillUpgrade.skill3Available)
    }

    var shouldUpgradeAll by remember {
        mutableStateOf(false)
    }
    val lowestMinSkill = listOf(minSkill1, minSkill2, minSkill3).min()
    var targetAllSkillLevel by remember {
        mutableStateOf(
            lowestMinSkill
        )
    }


    LaunchedEffect(key1 = targetAllSkillLevel, block = {
        if (minSkill1 <= targetAllSkillLevel && shouldUpgrade1) {
            upgradeSkill1 = targetAllSkillLevel - minSkill1
        }
        if (minSkill2 <= targetAllSkillLevel && shouldUpgrade2) {
            upgradeSkill2 = targetAllSkillLevel - minSkill2
        }
        if (minSkill3 <= targetAllSkillLevel && shouldUpgrade3) {
            upgradeSkill3 = targetAllSkillLevel - minSkill3
        }
    })

    LaunchedEffect(key1 = shouldUpgradeAll, block = {
        shouldUpgrade1 = shouldUpgradeAll == true && minSkill1 < 10
        shouldUpgrade2 = shouldUpgradeAll == true && minSkill2 < 10
        shouldUpgrade3 = shouldUpgradeAll == true && minSkill3 < 10
    })

    LazyColumn(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .padding(top = 5.dp)
    ) {
        stickyHeader {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.background
                    )
            ) {
                Text(
                    text = stringResource(id = R.string.skill_upgrade),
                    style = MaterialTheme.typography.headlineSmall
                )
                Divider()
            }
        }
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { shouldUpgradeAll = !shouldUpgradeAll }
            ) {
                Text(
                    stringResource(R.string.skill_enhancement_all_question),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Justify
                )
                Switch(
                    checked = shouldUpgradeAll,
                    onCheckedChange = {
                        shouldUpgradeAll = it
                    },
                )
            }
        }
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                PresetButton(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    text = "4",
                    enabled = shouldUpgradeAll,
                    onClick = {
                        targetAllSkillLevel = 4
                    }
                )
                PresetButton(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    text = "7",
                    enabled = shouldUpgradeAll,
                    onClick = {
                        targetAllSkillLevel = 7
                    }
                )
                PresetButton(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    text = "9",
                    enabled = shouldUpgradeAll,
                    onClick = {
                        targetAllSkillLevel = 9
                    }
                )
                PresetButton(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    text = "10",
                    enabled = shouldUpgradeAll,
                    onClick = {
                        targetAllSkillLevel = 10
                    }
                )
                Box(
                    modifier = Modifier
                        .weight(1.5f)
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Stepper(
                        value = targetAllSkillLevel,
                        onValueChange = { targetAllSkillLevel = it },
                        valueRange = lowestMinSkill..10,
                        enabled = shouldUpgradeAll,
                        textStyle = MaterialTheme.typography.bodyMedium,
                        valueRepresentation = { "Lv. $it" }
                    )
                }
            }
        }

        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                SkillUpgradeItem(
                    name = stringResource(id = R.string.skill_1),
                    shouldUpgrade = shouldUpgrade1,
                    onShouldUpgradeChange = {
                        shouldUpgrade1 = it
                    },
                    minimumUpgrade = minSkill1,
                    upgradeLevel = upgradeSkill1,
                    onUpgradeLevelChange = { upgradeSkill1 = it - minSkill1 },
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 2.dp),
                )

                SkillUpgradeItem(
                    name = stringResource(id = R.string.skill_2),
                    shouldUpgrade = shouldUpgrade2,
                    onShouldUpgradeChange = {
                        shouldUpgrade2 = it
                    },
                    minimumUpgrade = minSkill2,
                    upgradeLevel = upgradeSkill2,
                    onUpgradeLevelChange = { upgradeSkill2 = it - minSkill2 },
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 2.dp),
                    available = skill2Available
                )

                SkillUpgradeItem(
                    name = stringResource(id = R.string.skill_3),
                    shouldUpgrade = shouldUpgrade3,
                    onShouldUpgradeChange = {
                        shouldUpgrade3 = it
                    },
                    minimumUpgrade = minSkill3,
                    upgradeLevel = upgradeSkill3,
                    onUpgradeLevelChange = { upgradeSkill3 = it - minSkill3 },
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 2.dp),
                    available = skill3Available
                )
            }
        }
    }

    return ScriptLauncherResponseBuilder(
        canBuild = { true },
        build = {
            ScriptLauncherResponse.SkillUpgrade(
                shouldUpgradeSkill1 = shouldUpgrade1,
                upgradeSkill1 = upgradeSkill1,
                shouldUpgradeSkill2 = shouldUpgrade2,
                upgradeSkill2 = upgradeSkill2,
                shouldUpgradeSkill3 = shouldUpgrade3,
                upgradeSkill3 = upgradeSkill3,
            )
        }
    )
}

@Composable
private fun PresetButton(
    modifier: Modifier = Modifier,
    text: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    SuggestionChip(
        onClick = onClick,
        modifier = modifier,
        label = {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = when (enabled) {
                    true -> MaterialTheme.colorScheme.secondary
                    false -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)
                },
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        enabled = enabled,
    )
}

@Composable
fun SkillUpgradeItem(
    modifier: Modifier = Modifier,
    name: String,
    shouldUpgrade: Boolean,
    onShouldUpgradeChange: (Boolean) -> Unit,
    minimumUpgrade: Int,
    upgradeLevel: Int,
    onUpgradeLevelChange: (Int) -> Unit,
    available: Boolean = true
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .clickable(
                enabled = minimumUpgrade < 10 && available,
                onClick = {
                    onShouldUpgradeChange(!shouldUpgrade)
                }
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (available) {
            if (minimumUpgrade < 10) {
                Checkbox(
                    checked = shouldUpgrade,
                    onCheckedChange = {
                        onShouldUpgradeChange(!shouldUpgrade)
                    },
                )
            }
            Text(
                text = when (minimumUpgrade < 10) {
                    true -> name.uppercase()
                    false -> name.uppercase() + "\n" + stringResource(id = R.string.skill_max).uppercase()
                },
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                color = when (shouldUpgrade) {
                    true -> MaterialTheme.colorScheme.onBackground
                    false -> MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                },
                textDecoration = TextDecoration.Underline
            )
            if (minimumUpgrade < 10) {
                Stepper(
                    value = (upgradeLevel + minimumUpgrade),
                    onValueChange = { onUpgradeLevelChange(it) },
                    valueRange = minimumUpgrade..10,
                    enabled = shouldUpgrade,
                    textStyle = MaterialTheme.typography.bodySmall,
                    valueRepresentation = { "Lv. $it" }
                )
                TextButton(
                    onClick = { onUpgradeLevelChange(minimumUpgrade) },
                    enabled = shouldUpgrade && (upgradeLevel + minimumUpgrade) != minimumUpgrade,
                ) {
                    Text(text = stringResource(id = R.string.reset).uppercase())
                }
            }
        } else {
            Text(
                text = name.uppercase(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                textDecoration = TextDecoration.Underline
            )
            Text(
                text = stringResource(id = R.string.skill_not_available).uppercase(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
            )
        }
    }
}