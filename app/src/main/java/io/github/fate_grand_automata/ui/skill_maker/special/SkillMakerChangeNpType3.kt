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
fun SkillMakerChangeNpType3(
    onSkillTarget: (ServantTarget) -> Unit
) {
    var changeNp3Type by remember { mutableStateOf(ChangeNp3Type.Generic) }

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(16.dp)
    ) {
        FGATitle(
            stringResource(changeNp3Type.stringRes)
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
                text = stringResource(changeNp3Type.targetAStringRes)
            )

            TargetButton(
                onClick = { onSkillTarget(ServantTarget.B) },
                color = colorResource(R.color.colorArtsResist),
                text = stringResource(changeNp3Type.targetBStringRes)
            )

            TargetButton(
                onClick = { onSkillTarget(ServantTarget.C) },
                color = colorResource(R.color.colorBuster),
                text = stringResource(changeNp3Type.targetCStringRes)
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
            ChangeNp3Type.entries.filterNot {
                it == ChangeNp3Type.Generic
            }.forEach { entry ->

                val containerColor by animateColorAsState(
                    targetValue = if (changeNp3Type == entry) {
                        MaterialTheme.colorScheme.onSurface.copy(0.12f)
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                    label = "Container Color, if ${entry.name} is selected"
                )
                val contentColor by animateColorAsState(
                    targetValue = if (changeNp3Type == entry) {
                        MaterialTheme.colorScheme.onSurface.copy(0.38f)
                    } else {
                        MaterialTheme.colorScheme.onPrimary
                    },
                    label = "Container Color, if ${entry.name} is selected"
                )
                Button(
                    onClick = {
                        changeNp3Type = if (changeNp3Type != entry) {
                            entry
                        } else {
                            ChangeNp3Type.Generic
                        }
                    },
                    border = if (changeNp3Type == entry) {
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
fun TestChangeNpType3() {
    FGATheme {
        SkillMakerChangeNpType3(onSkillTarget = { })
    }
}


private enum class ChangeNp3Type {
    Generic,
    SpaceIshtar,
}

private val ChangeNp3Type.stringRes
    get() = when (this) {
        ChangeNp3Type.Generic -> R.string.skill_maker_change_np_type_3
        ChangeNp3Type.SpaceIshtar -> R.string.skill_maker_space_ishtar
    }

private val ChangeNp3Type.targetAStringRes
    get() = when (this) {
        ChangeNp3Type.Generic -> R.string.skill_maker_option_1
        ChangeNp3Type.SpaceIshtar -> R.string.skill_maker_quick
    }

private val ChangeNp3Type.targetBStringRes
    get() = when (this) {
        ChangeNp3Type.Generic -> R.string.skill_maker_option_2
        ChangeNp3Type.SpaceIshtar -> R.string.skill_maker_arts
    }

private val ChangeNp3Type.targetCStringRes
    get() = when (this) {
        ChangeNp3Type.Generic -> R.string.skill_maker_option_2
        ChangeNp3Type.SpaceIshtar -> R.string.skill_maker_buster
    }