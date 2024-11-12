package io.github.fate_grand_automata.ui.skill_maker

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
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
fun SkillMakerChoice3(
    onSkillTarget: (ServantTarget) -> Unit
) {
    var choice3Type by remember { mutableStateOf(Choice3Type.Hakunon) }

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(16.dp)
    ) {
        FGATitle(
            stringResource(choice3Type.stringRes)
        )

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            TargetButton(
                onClick = { onSkillTarget(ServantTarget.SpecialTarget.Choice3OptionA) },
                color = colorResource(R.color.colorQuickResist),
                text = stringResource(choice3Type.choice1StringRes)
            )

            TargetButton(
                onClick = { onSkillTarget(ServantTarget.SpecialTarget.Choice3OptionB) },
                color = colorResource(R.color.colorArtsResist),
                text = stringResource(choice3Type.choice2StringRes)
            )

            TargetButton(
                onClick = { onSkillTarget(ServantTarget.SpecialTarget.Choice3OptionC) },
                color = colorResource(R.color.colorBuster),
                text = stringResource(choice3Type.choice3StringRes)
            )
        }

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(
                stringResource(R.string.skill_maker_update_hints).uppercase(),
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
            Choice3Type.entries.forEach { entry ->
                Button(
                    onClick = {
                        choice3Type = entry
                    },
                    enabled = choice3Type != entry,
                    border = if (choice3Type == entry) {
                        BorderStroke(
                            width = Dp.Hairline,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    } else null
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
fun TestChoice3() {
    FGATheme {
        SkillMakerChoice3(onSkillTarget = { })
    }
}


private enum class Choice3Type {
    Hakunon,
    Soujuurou,
    Charlotte
}

private val Choice3Type.stringRes
    get() = when (this) {
        Choice3Type.Hakunon -> R.string.skill_maker_hakunon
        Choice3Type.Soujuurou -> R.string.skill_maker_soujuurou
        Choice3Type.Charlotte -> R.string.skill_maker_charlotte
    }

private val Choice3Type.choice1StringRes
    get() = when (this) {
        Choice3Type.Hakunon -> R.string.skill_maker_hakunon_choice_1
        Choice3Type.Soujuurou -> R.string.skill_maker_quick
        Choice3Type.Charlotte -> R.string.skill_maker_arts
    }

private val Choice3Type.choice2StringRes
    get() = when (this) {
        Choice3Type.Hakunon -> R.string.skill_maker_hakunon_choice_2
        Choice3Type.Soujuurou -> R.string.skill_maker_arts
        Choice3Type.Charlotte -> R.string.skill_maker_charlotte_choice_2
    }

private val Choice3Type.choice3StringRes
    get() = when (this) {
        Choice3Type.Hakunon -> R.string.skill_maker_hakunon_choice_3
        Choice3Type.Soujuurou -> R.string.skill_maker_buster
        Choice3Type.Charlotte -> R.string.skill_maker_charlotte_choice_3
    }