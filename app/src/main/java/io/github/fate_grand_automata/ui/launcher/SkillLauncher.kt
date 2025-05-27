package io.github.fate_grand_automata.ui.launcher

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.fate_grand_automata.R
import io.github.fate_grand_automata.prefs.core.SkillPrefsCore
import io.github.fate_grand_automata.scripts.Images
import io.github.fate_grand_automata.scripts.prefs.IPreferences
import kotlin.math.max

@Composable
fun skillLauncher(
    prefs: IPreferences,
    modifier: Modifier = Modifier
): ScriptLauncherResponseBuilder {


    val skillOneCurrentLevel = prefs.skill.skillOneCurrentLevel

    var skillOneTargetLevel by remember {
        mutableIntStateOf(max(skillOneCurrentLevel, prefs.skill.skillOneTargetLevel))
    }

    val skillTwoCurrentLevel = prefs.skill.skillTwoCurrentLevel
    var skillTwoTargetLevel by remember {
        mutableIntStateOf(max(skillTwoCurrentLevel, prefs.skill.skillTwoTargetLevel))
    }
    val isSkillTwoAvailable = prefs.skill.isSkillTwoAvailable

    val skillThreeCurrentLevel = prefs.skill.skillThreeCurrentLevel
    var skillThreeTargetLevel by remember {
        mutableIntStateOf(max(skillThreeCurrentLevel, prefs.skill.skillThreeTargetLevel))
    }
    val isSkillThreeAvailable = prefs.skill.isSkillThreeAvailable

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
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    SkillUpgradeItem(
                        name = stringResource(id = R.string.skill_number, 1),
                        minimumUpgrade = skillOneCurrentLevel,
                        upgradeLevel = skillOneTargetLevel,
                        onUpgradeLevelChange = { skillOneTargetLevel = it.coerceAtLeast(skillOneCurrentLevel) },
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 2.dp),
                    )

                    SkillUpgradeItem(
                        name = stringResource(id = R.string.skill_number, 2),
                        minimumUpgrade = skillTwoCurrentLevel,
                        upgradeLevel = skillTwoTargetLevel,
                        onUpgradeLevelChange = { skillTwoTargetLevel = it.coerceAtLeast(skillTwoCurrentLevel) },
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 2.dp),
                        available = isSkillTwoAvailable
                    )

                    SkillUpgradeItem(
                        name = stringResource(id = R.string.skill_number, 3),
                        minimumUpgrade = skillThreeCurrentLevel,
                        upgradeLevel = skillThreeTargetLevel,
                        onUpgradeLevelChange = { skillThreeTargetLevel = it.coerceAtLeast(skillThreeCurrentLevel) },
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 2.dp),
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
                skillOneTargetLevel = skillOneTargetLevel,
                skillTwoTargetLevel = skillTwoTargetLevel,
                skillThreeTargetLevel = skillThreeTargetLevel,
            )
        }
    )
}

@Composable
private fun SkillUpgradeItem(
    modifier: Modifier = Modifier,
    name: String,
    minimumUpgrade: Int,
    upgradeLevel: Int,
    onUpgradeLevelChange: (Int) -> Unit,
    available: Boolean = true
) {
    Row {
        Column(
            modifier = modifier
                .fillMaxWidth()
        ) {
            Text(text = "$name: $upgradeLevel")
            Slider(
                enabled = available,
                value = upgradeLevel.toFloat(),
                onValueChange = { onUpgradeLevelChange(it.toInt()) },
                valueRange = 1f..10f,
                steps = 8,
            )
        }
    }
}