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
import io.github.fate_grand_automata.scripts.prefs.IPreferences
import io.github.fate_grand_automata.ui.prefs.remember
import io.github.fate_grand_automata.ui.CustomSlider


@Composable
fun skillLauncher(
    prefsCore: PrefsCore,
    prefs: IPreferences,
    modifier: Modifier = Modifier
): ScriptLauncherResponseBuilder {

    val emptyServant by prefsCore.emptyEnhance.remember()

    var shouldUpgradeSkillOne by remember {
        mutableStateOf(false)
    }
    val minimumSkillOne by remember{
        mutableIntStateOf(prefs.skill.minimumSkillOne)
    }

    var skillOneUpgradeValue by remember {
        mutableStateOf(0)
    }
    var shouldUpgradeSkillTwo by remember {
        mutableStateOf(false)
    }
    val minimumSkillTwo by remember{
        mutableIntStateOf(prefs.skill.minimumSkillTwo)
    }
    var skillTwoUpgradeValue by remember {
        mutableStateOf(0)
    }
    val isSkillTwoAvailable by prefsCore.skill.isSkillTwoAvailable.remember()

    var shouldUpgradeSkillThree by remember {
        mutableStateOf(false)
    }
    val minimumSkillThree by remember {
        mutableIntStateOf(prefs.skill.minimumSkillThree)
    }
    var skillThreeUpgradeValue by remember {
        mutableStateOf(0)
    }
    val isSkillThreeAvailable by prefsCore.skill.isSkillThreeAvailable.remember()

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
        if (emptyServant){
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
            }
            item {
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
            }
            item {
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

    return ScriptLauncherResponseBuilder(
        canBuild = {
            !emptyServant
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
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                enabled = minimumUpgrade < 10 && available,
                onClick = {
                    onShouldUpgradeChange(!shouldUpgrade)
                }
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        if (available) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (minimumUpgrade < 10) {
                    Checkbox(
                        checked = shouldUpgrade,
                        onCheckedChange = {
                            onShouldUpgradeChange(!shouldUpgrade)
                        },
                    )
                }
                Text(
                    text = name.uppercase(),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    color = when (shouldUpgrade) {
                        true -> MaterialTheme.colorScheme.onBackground
                        false -> MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                    },
                    textDecoration = TextDecoration.Underline
                )
                if (minimumUpgrade < 10) {
                    TextButton(
                        onClick = { onUpgradeLevelChange(minimumUpgrade) },
                        enabled = shouldUpgrade && (upgradeLevel + minimumUpgrade) != minimumUpgrade,
                    ) {
                        Text(text = stringResource(id = R.string.reset).uppercase())
                    }
                }
            }
            if (minimumUpgrade < 10) {
                CustomSlider(
                    value = (upgradeLevel + minimumUpgrade),
                    onValueChange = { onUpgradeLevelChange(it) },
                    valueRange = minimumUpgrade..10,
                    enabled = shouldUpgrade,
                    modifier = Modifier.weight(1f),
                    showIndicator = true,
                )
            } else {
                Text(
                    text = stringResource(id = R.string.skill_max_level).uppercase(),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    color = when (shouldUpgrade) {
                        true -> MaterialTheme.colorScheme.onBackground
                        false -> MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                    },
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.weight(1f)
                )
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