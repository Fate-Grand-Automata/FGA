package io.github.fate_grand_automata.ui.skill_maker.special

import android.content.res.Configuration
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.fate_grand_automata.R
import io.github.fate_grand_automata.scripts.models.ServantTarget
import io.github.fate_grand_automata.ui.FGATheme
import io.github.fate_grand_automata.ui.FGATitle
import io.github.fate_grand_automata.ui.skill_maker.SkillSlot

@Composable
fun SkillMakerChoice3(
    slot: SkillSlot,
    onSkillTarget: (ServantTarget) -> Unit,
) {
    val entries by remember {
        derivedStateOf {
            Choice3Type.entries.filter {
                it != Choice3Type.Generic && it.slot.matches(slot)
            }
        }
    }
    var choice3Type by remember { mutableStateOf(Choice3Type.Generic) }

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(16.dp),
    ) {
        FGATitle(
            stringResource(choice3Type.stringRes),
        )

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            TargetButton(
                onClick = { onSkillTarget(ServantTarget.SpecialTarget.Choice3OptionA) },
                color = colorResource(R.color.colorQuickResist),
                text = stringResource(choice3Type.choice1StringRes),
            )

            TargetButton(
                onClick = { onSkillTarget(ServantTarget.SpecialTarget.Choice3OptionB) },
                color = colorResource(R.color.colorArtsResist),
                text = stringResource(choice3Type.choice2StringRes),
            )

            TargetButton(
                onClick = { onSkillTarget(ServantTarget.SpecialTarget.Choice3OptionC) },
                color = colorResource(R.color.colorBuster),
                text = stringResource(choice3Type.choice3StringRes),
            )
        }

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .fillMaxWidth(),
        ) {
            Text(
                stringResource(R.string.skill_maker_update_button_labels).uppercase(),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                textDecoration = TextDecoration.Underline,
            )
        }
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .fillMaxWidth(),
        ) {
            entries.forEach { entry ->
                val containerColor by animateColorAsState(
                    targetValue = if (choice3Type == entry) {
                        MaterialTheme.colorScheme.onSurface.copy(0.12f)
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                    label = "Container Color, if ${entry.name} is selected",
                )
                val contentColor by animateColorAsState(
                    targetValue = if (choice3Type == entry) {
                        MaterialTheme.colorScheme.onSurface.copy(0.38f)
                    } else {
                        MaterialTheme.colorScheme.onPrimary
                    },
                    label = "Container Color, if ${entry.name} is selected",
                )
                Button(
                    onClick = {
                        choice3Type = if (choice3Type != entry) {
                            entry
                        } else {
                            Choice3Type.Generic
                        }
                    },
                    border = if (choice3Type == entry) {
                        BorderStroke(
                            width = Dp.Hairline,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    } else {
                        null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = containerColor,
                        contentColor = contentColor,
                    ),
                ) {
                    Text(stringResource(entry.stringRes))
                }
            }
        }
    }
}

@Preview(name = "Light Mode", widthDp = 600, heightDp = 300)
@Preview(name = "Dark Mode", widthDp = 600, heightDp = 300, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun TestChoice3Slot1() {
    FGATheme {
        SkillMakerChoice3(
            slot = SkillSlot.First,
            onSkillTarget = { },
        )
    }
}

@Preview(name = "Light Mode", widthDp = 600, heightDp = 300)
@Composable
fun TestChoice3Slot3() {
    FGATheme {
        SkillMakerChoice3(
            slot = SkillSlot.Third,
            onSkillTarget = { },
        )
    }
}

private enum class Choice3Type(val slot: SkillSlot) {
    Generic(SkillSlot.ANY),

    // First slot
    VanGogh(SkillSlot.First),

    // Third slot
    Hakuno(SkillSlot.Third),
    Soujuurou(SkillSlot.Third),
    Charlotte(SkillSlot.Third),
}

private val Choice3Type.stringRes
    get() = when (this) {
        Choice3Type.Generic -> R.string.skill_maker_choices_3
        Choice3Type.Hakuno -> R.string.skill_maker_hakuno
        Choice3Type.Soujuurou -> R.string.skill_maker_soujuurou
        Choice3Type.Charlotte -> R.string.skill_maker_charlotte
        Choice3Type.VanGogh -> R.string.skill_maker_van_gogh
    }

private val Choice3Type.choice1StringRes
    get() = when (this) {
        Choice3Type.Generic -> R.string.skill_maker_option_1
        Choice3Type.Hakuno -> R.string.skill_maker_hakuno_choice_1
        Choice3Type.Soujuurou, Choice3Type.VanGogh -> R.string.skill_maker_quick
        Choice3Type.Charlotte -> R.string.skill_maker_arts
    }

private val Choice3Type.choice2StringRes
    get() = when (this) {
        Choice3Type.Generic -> R.string.skill_maker_option_2
        Choice3Type.Hakuno -> R.string.skill_maker_hakuno_choice_2
        Choice3Type.Soujuurou, Choice3Type.VanGogh -> R.string.skill_maker_arts
        Choice3Type.Charlotte -> R.string.skill_maker_charlotte_choice_2
    }

private val Choice3Type.choice3StringRes
    get() = when (this) {
        Choice3Type.Generic -> R.string.skill_maker_option_3
        Choice3Type.Hakuno -> R.string.skill_maker_hakuno_choice_3
        Choice3Type.Soujuurou, Choice3Type.VanGogh -> R.string.skill_maker_buster
        Choice3Type.Charlotte -> R.string.skill_maker_charlotte_choice_3
    }
