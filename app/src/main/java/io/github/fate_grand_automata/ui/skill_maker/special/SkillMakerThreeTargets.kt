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

@Composable
fun SkillMakerThreeTargets(
    onSkillTarget: (ServantTarget) -> Unit
) {
    var threeTargetsType by remember { mutableStateOf(ThreeTargetsType.Generic) }

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(16.dp)
    ) {
        FGATitle(
            stringResource(threeTargetsType.stringRes)
        )

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            TargetButton(
                onClick = { onSkillTarget(ServantTarget.A) },
                color = colorResource(R.color.colorQuickResist),
                text = stringResource(threeTargetsType.targetAStringRes)
            )

            TargetButton(
                onClick = { onSkillTarget(ServantTarget.B) },
                color = colorResource(R.color.colorArtsResist),
                text = stringResource(threeTargetsType.targetBStringRes)
            )

            TargetButton(
                onClick = { onSkillTarget(ServantTarget.C) },
                color = colorResource(R.color.colorBuster),
                text = stringResource(threeTargetsType.targetCStringRes)
            )
        }

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(
                stringResource(R.string.skill_maker_update_button_labels).uppercase(),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                textDecoration = TextDecoration.Underline
            )
        }
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            ThreeTargetsType.entries.filterNot {
                it == ThreeTargetsType.Generic
            }.forEach { entry ->

                val containerColor by animateColorAsState(
                    targetValue = if (threeTargetsType == entry) {
                        MaterialTheme.colorScheme.onSurface.copy(0.12f)
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                    label = "Container Color, if ${entry.name} is selected"
                )
                val contentColor by animateColorAsState(
                    targetValue = if (threeTargetsType == entry) {
                        MaterialTheme.colorScheme.onSurface.copy(0.38f)
                    } else {
                        MaterialTheme.colorScheme.onPrimary
                    },
                    label = "Container Color, if ${entry.name} is selected"
                )
                Button(
                    onClick = {
                        threeTargetsType = if (threeTargetsType != entry) {
                            entry
                        } else {
                            ThreeTargetsType.Generic
                        }
                    },
                    border = if (threeTargetsType == entry) {
                        BorderStroke(
                            width = Dp.Hairline,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    } else null,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = containerColor,
                        contentColor = contentColor
                    )
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
fun TestThreeTargets() {
    FGATheme {
        SkillMakerThreeTargets(onSkillTarget = { })
    }
}


private enum class ThreeTargetsType {
    Generic,
    SpaceIshtar,
}

private val ThreeTargetsType.stringRes
    get() = when (this) {
        ThreeTargetsType.Generic -> R.string.skill_maker_three_targets
        ThreeTargetsType.SpaceIshtar -> R.string.skill_maker_space_ishtar
    }

private val ThreeTargetsType.targetAStringRes
    get() = when (this) {
        ThreeTargetsType.Generic -> R.string.skill_maker_option_1
        ThreeTargetsType.SpaceIshtar -> R.string.skill_maker_quick
    }

private val ThreeTargetsType.targetBStringRes
    get() = when (this) {
        ThreeTargetsType.Generic -> R.string.skill_maker_option_2
        ThreeTargetsType.SpaceIshtar -> R.string.skill_maker_arts
    }

private val ThreeTargetsType.targetCStringRes
    get() = when (this) {
        ThreeTargetsType.Generic -> R.string.skill_maker_option_2
        ThreeTargetsType.SpaceIshtar -> R.string.skill_maker_buster
    }