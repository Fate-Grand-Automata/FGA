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
fun SkillMakerChoice2(
    slot: SkillSlot,
    onOption1: () -> Unit,
    onOption2: () -> Unit,
    goToTarget: Boolean,
    onTarget: (firstTarget: ServantTarget) -> Unit,
) {
    val entries by remember {
        derivedStateOf {
            Choice2Type.entries.filter {
                it != Choice2Type.Generic && it.slot.matches(slot)
            }
        }
    }
    var choice2Type by remember { mutableStateOf(Choice2Type.Generic) }

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(16.dp),
    ) {
        FGATitle(
            stringResource(choice2Type.stringRes),
        )

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            TargetButton(
                onClick = if (goToTarget) {
                    (
                        { onTarget(ServantTarget.SpecialTarget.Choice2OptionA) }
                        )
                } else {
                    onOption1
                },
                color = MaterialTheme.colorScheme.primary,
                text = stringResource(choice2Type.targetAStringRes),
            )

            TargetButton(
                onClick = if (goToTarget) {
                    (
                        { onTarget(ServantTarget.SpecialTarget.Choice2OptionB) }
                        )
                } else {
                    onOption2
                },
                color = MaterialTheme.colorScheme.tertiary,
                text = stringResource(choice2Type.targetBStringRes),
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
            entries.filterNot {
                it == Choice2Type.Generic
            }.forEach { entry ->
                val containerColor by animateColorAsState(
                    targetValue = if (choice2Type == entry) {
                        MaterialTheme.colorScheme.onSurface.copy(0.12f)
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                    label = "Container Color, if ${entry.name} is selected",
                )
                val contentColor by animateColorAsState(
                    targetValue = if (choice2Type == entry) {
                        MaterialTheme.colorScheme.onSurface.copy(0.38f)
                    } else {
                        MaterialTheme.colorScheme.onPrimary
                    },
                    label = "Container Color, if ${entry.name} is selected",
                )
                Button(
                    onClick = {
                        choice2Type = if (choice2Type != entry) {
                            entry
                        } else {
                            Choice2Type.Generic
                        }
                    },
                    border = if (choice2Type == entry) {
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

@Composable
fun SkillMakerChoice2Target(
    onSkillTarget: (ServantTarget) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(16.dp),
    ) {
        FGATitle(
            stringResource(R.string.skill_maker_target_header),
        )

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            TargetButton(
                onClick = { onSkillTarget(ServantTarget.A) },
                color = colorResource(R.color.colorServant1),
                text = stringResource(R.string.skill_maker_target_servant, 1),
            )

            TargetButton(
                onClick = { onSkillTarget(ServantTarget.B) },
                color = colorResource(R.color.colorServant2),
                text = stringResource(R.string.skill_maker_target_servant, 2),
            )

            TargetButton(
                onClick = { onSkillTarget(ServantTarget.C) },
                color = colorResource(R.color.colorServant3),
                text = stringResource(R.string.skill_maker_target_servant, 3),
            )
        }
    }
}

@Preview(name = "Light Mode", widthDp = 600, heightDp = 300)
@Preview(name = "Dark Mode", widthDp = 600, heightDp = 300, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun TestChoice2Slot2() {
    FGATheme {
        SkillMakerChoice2(slot = SkillSlot.Second, onOption1 = { }, onOption2 = { }, goToTarget = true, onTarget = { })
    }
}

@Preview(name = "Light Mode", widthDp = 600, heightDp = 300)
@Preview(name = "Dark Mode", widthDp = 600, heightDp = 300, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun TestChoice2Slot3() {
    FGATheme {
        SkillMakerChoice2(slot = SkillSlot.Third, onOption1 = { }, onOption2 = { }, goToTarget = true, onTarget = { })
    }
}

@Preview(name = "Light Mode", widthDp = 600, heightDp = 300)
@Preview(name = "Dark Mode", widthDp = 600, heightDp = 300, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun TestChoice2Target() {
    FGATheme {
        SkillMakerChoice2Target(onSkillTarget = { })
    }
}

private enum class Choice2Type(val slot: SkillSlot) {
    Generic(SkillSlot.ANY),
    Kukulkan(SkillSlot.ANY),
    UDKBarghest(SkillSlot.Third),
}

private val Choice2Type.stringRes
    get() = when (this) {
        Choice2Type.Generic -> R.string.skill_maker_choices_2
        Choice2Type.Kukulkan -> R.string.skill_maker_kukulkan
        Choice2Type.UDKBarghest -> R.string.skill_maker_udk_barghest
    }

private val Choice2Type.targetAStringRes
    get() = when (this) {
        Choice2Type.Generic -> R.string.skill_maker_option_1
        Choice2Type.Kukulkan -> R.string.skill_maker_kukulkan_choice_1
        Choice2Type.UDKBarghest -> R.string.skill_maker_udk_barghest_choice_1
    }

private val Choice2Type.targetBStringRes
    get() = when (this) {
        Choice2Type.Generic -> R.string.skill_maker_option_2
        Choice2Type.Kukulkan -> R.string.skill_maker_kukulkan_choice_2
        Choice2Type.UDKBarghest -> R.string.skill_maker_udk_barghest_choice_2
    }
