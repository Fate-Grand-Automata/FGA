package io.github.fate_grand_automata.ui.launcher

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.toMutableStateList
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.fate_grand_automata.R
import io.github.fate_grand_automata.scripts.models.SkillUpgradeState
import io.github.fate_grand_automata.scripts.modules.SkillLevel
import io.github.fate_grand_automata.scripts.modules.maxSkillLevel
import io.github.fate_grand_automata.ui.Stepper

@Composable
fun skillLauncher(
    state: SkillUpgradeState,
    modifier: Modifier = Modifier
): ScriptLauncherResponseBuilder {
    val currentLevels = state.currentLevels

    // null if the skill is locked or its level couldn't be read
    val knownLevels = currentLevels.map { (it as? SkillLevel.Known)?.level }

    // Starts at the current levels, so nothing is enhanced unless chosen for this Servant
    val targetLevels = remember {
        knownLevels.map { it ?: 0 }.toMutableStateList()
    }

    val hasAnythingToUpgrade = knownLevels.zip(targetLevels).any { (current, target) ->
        current != null && target > current
    }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(top = 5.dp)
    ) {
        Text(
            stringResource(R.string.skill_upgrade),
            style = MaterialTheme.typography.titleLarge
        )

        HorizontalDivider(
            modifier = Modifier
                .padding(5.dp)
                .padding(bottom = 16.dp)
        )

        if (state.isNothingSelected) {
            Text(
                stringResource(R.string.servant_enhancement_none_selected),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        } else {
            PresetRow(
                knownLevels = knownLevels,
                onPresetClick = { preset ->
                    knownLevels.forEachIndexed { index, current ->
                        if (current != null) {
                            targetLevels[index] = maxOf(current, preset)
                        }
                    }
                }
            )

            currentLevels.forEachIndexed { index, currentLevel ->
                SkillRow(
                    skillNumber = index + 1,
                    currentLevel = currentLevel,
                    targetLevel = targetLevels[index],
                    onTargetLevelChange = { targetLevels[index] = it }
                )
            }

            if (!hasAnythingToUpgrade) {
                Text(
                    stringResource(R.string.skill_upgrade_nothing_selected),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
    }

    return ScriptLauncherResponseBuilder(
        canBuild = { !state.isNothingSelected && hasAnythingToUpgrade },
        build = { ScriptLauncherResponse.Skill(targetLevels.toList()) }
    )
}

/**
 * Buttons that set every unlocked skill to the same target level. A skill already above the preset
 * keeps its current level.
 */
@Composable
private fun PresetRow(
    knownLevels: List<Int?>,
    onPresetClick: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            stringResource(R.string.skill_upgrade_all_skills),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )

        listOf(6, 9, 10).forEach { preset ->
            OutlinedButton(
                onClick = { onPresetClick(preset) },
                // Only useful if at least one unlocked skill is below the preset
                enabled = knownLevels.any { it != null && it < preset }
            ) {
                Text(stringResource(R.string.skill_upgrade_current_level, preset))
            }
        }
    }
}

@Composable
private fun SkillRow(
    skillNumber: Int,
    currentLevel: SkillLevel,
    targetLevel: Int,
    onTargetLevelChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            // Same height as a row with a Stepper, so all rows line up
            .heightIn(min = 48.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            stringResource(R.string.skill_upgrade_skill_number, skillNumber),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )

        when (currentLevel) {
            SkillLevel.Locked -> StatusText(stringResource(R.string.skill_upgrade_locked))

            SkillLevel.Unreadable -> StatusText(stringResource(R.string.skill_upgrade_unreadable))

            is SkillLevel.Known -> if (currentLevel.level >= maxSkillLevel) {
                StatusText(stringResource(R.string.skill_upgrade_max_level))
            } else {
                Text(
                    stringResource(R.string.skill_upgrade_current_level, currentLevel.level) + "  →",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )

                Stepper(
                    value = targetLevel,
                    onValueChange = onTargetLevelChange,
                    valueRange = currentLevel.level..maxSkillLevel
                )
            }
        }
    }
}

@Composable
private fun StatusText(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.secondary,
        modifier = Modifier.padding(end = 20.dp)
    )
}
