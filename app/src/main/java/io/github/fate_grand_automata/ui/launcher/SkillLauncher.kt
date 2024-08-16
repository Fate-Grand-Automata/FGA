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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import io.github.fate_grand_automata.prefs.core.PrefsCore
import io.github.fate_grand_automata.prefs.core.SkillPrefsCore
import io.github.fate_grand_automata.scripts.Images
import io.github.fate_grand_automata.scripts.prefs.IPreferences
import io.github.fate_grand_automata.ui.Stepper
import io.github.fate_grand_automata.ui.prefs.remember

@Composable
fun skillLauncher(
    prefs: IPreferences,
    modifier: Modifier = Modifier
): ScriptLauncherResponseBuilder {

    var shouldUpgradeSkillOne by remember {
        mutableStateOf(false)
    }
    val minimumSkillOne by remember{
        mutableIntStateOf(prefs.skill.minimumSkillOne)
    }

    var skillOneUpgradeValue by remember {
        mutableIntStateOf(0)
    }
    var shouldUpgradeSkillTwo by remember {
        mutableStateOf(false)
    }
    val minimumSkillTwo by remember{
        mutableIntStateOf(prefs.skill.minimumSkillTwo)
    }
    var skillTwoUpgradeValue by remember {
        mutableIntStateOf(0)
    }
    val isSkillTwoAvailable by remember {
        mutableStateOf(prefs.skill.isSkillTwoAvailable)
    }

    var shouldUpgradeSkillThree by remember {
        mutableStateOf(false)
    }
    val minimumSkillThree by remember {
        mutableIntStateOf(prefs.skill.minimumSkillThree)
    }
    var skillThreeUpgradeValue by remember {
        mutableIntStateOf(0)
    }
    val isSkillThreeAvailable by remember {
        mutableStateOf(prefs.skill.isSkillThreeAvailable)
    }

    var shouldUpgradeAllSkills by remember {
        mutableStateOf(false)
    }
    val lowestMinimumSkillLevel by remember {
        mutableIntStateOf(
            when {
                isSkillThreeAvailable -> minOf(minimumSkillOne, minimumSkillTwo, minimumSkillThree)
                isSkillTwoAvailable -> minOf(minimumSkillOne, minimumSkillTwo)
                else -> minimumSkillOne
            }
        )
    }
    var targetAllSkillLevel by remember {
        mutableStateOf(
            lowestMinimumSkillLevel
        )
    }


    LaunchedEffect(key1 = targetAllSkillLevel, block = {
        if (minimumSkillOne <= targetAllSkillLevel && shouldUpgradeSkillOne) {
            skillOneUpgradeValue = targetAllSkillLevel - minimumSkillOne
        }
        if (minimumSkillTwo <= targetAllSkillLevel && shouldUpgradeSkillTwo) {
            skillTwoUpgradeValue = targetAllSkillLevel - minimumSkillTwo
        }
        if (minimumSkillThree <= targetAllSkillLevel && shouldUpgradeSkillThree) {
            skillThreeUpgradeValue = targetAllSkillLevel - minimumSkillThree
        }
    })

    LaunchedEffect(key1 = shouldUpgradeAllSkills, block = {
        shouldUpgradeSkillOne = shouldUpgradeAllSkills == true && minimumSkillOne < 10
        shouldUpgradeSkillTwo = shouldUpgradeAllSkills == true && minimumSkillTwo < 10 && isSkillTwoAvailable
        shouldUpgradeSkillThree = shouldUpgradeAllSkills == true && minimumSkillThree < 10 && isSkillThreeAvailable
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
                HorizontalDivider()
            }
        }
        if (prefs.skill.isEmptyEnhance){
            item {
                Text(
                    text = stringResource(id = R.string.empty_servant),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { shouldUpgradeAllSkills = !shouldUpgradeAllSkills }
                ) {
                    Text(
                        stringResource(R.string.skill_upgrade_all_available_question),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Justify
                    )
                    Switch(
                        checked = shouldUpgradeAllSkills,
                        onCheckedChange = {
                            shouldUpgradeAllSkills = it
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
                        enabled = shouldUpgradeAllSkills,
                        onClick = {
                            targetAllSkillLevel = 4
                        }
                    )
                    PresetButton(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        text = "7",
                        enabled = shouldUpgradeAllSkills,
                        onClick = {
                            targetAllSkillLevel = 7
                        }
                    )
                    PresetButton(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        text = "9",
                        enabled = shouldUpgradeAllSkills,
                        onClick = {
                            targetAllSkillLevel = 9
                        }
                    )
                    PresetButton(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        text = "10",
                        enabled = shouldUpgradeAllSkills,
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
                            valueRange = lowestMinimumSkillLevel..10,
                            enabled = shouldUpgradeAllSkills,
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
                        name = stringResource(id = R.string.skill_number, 1),
                        shouldUpgrade = shouldUpgradeSkillOne,
                        onShouldUpgradeChange = {
                            shouldUpgradeSkillOne = it
                        },
                        minimumUpgrade = minimumSkillOne,
                        upgradeLevel = skillOneUpgradeValue,
                        onUpgradeLevelChange = { skillOneUpgradeValue = it - minimumSkillOne },
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 2.dp),
                    )

                    SkillUpgradeItem(
                        name = stringResource(id = R.string.skill_number, 2),
                        shouldUpgrade = shouldUpgradeSkillTwo,
                        onShouldUpgradeChange = {
                            shouldUpgradeSkillTwo = it
                        },
                        minimumUpgrade = minimumSkillTwo,
                        upgradeLevel = skillTwoUpgradeValue,
                        onUpgradeLevelChange = { skillTwoUpgradeValue = it - minimumSkillTwo },
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 2.dp),
                        available = isSkillTwoAvailable
                    )

                    SkillUpgradeItem(
                        name = stringResource(id = R.string.skill_number, 3),
                        shouldUpgrade = shouldUpgradeSkillThree,
                        onShouldUpgradeChange = {
                            shouldUpgradeSkillThree = it
                        },
                        minimumUpgrade = minimumSkillThree,
                        upgradeLevel = skillThreeUpgradeValue,
                        onUpgradeLevelChange = { skillThreeUpgradeValue = it - minimumSkillThree },
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 2.dp),
                        available = isSkillThreeAvailable
                    )
                }
            }
        }
    }

    return ScriptLauncherResponseBuilder(
        canBuild = {
            !prefs.skill.isEmptyEnhance
        },
        build = {
            ScriptLauncherResponse.Skill(
                shouldUpgradeSkillOne = shouldUpgradeSkillOne,
                skillOneUpgradeValue = skillOneUpgradeValue,
                shouldUpgradeSkillTwo = shouldUpgradeSkillTwo,
                skillTwoUpgradeValue = skillTwoUpgradeValue,
                shouldUpgradeSkillThree = shouldUpgradeSkillThree,
                skillThreeUpgradeValue = skillThreeUpgradeValue,
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
private fun SkillUpgradeItem(
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
                    false -> name.uppercase() + "\n" + stringResource(id = R.string.skill_max_level).uppercase()
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