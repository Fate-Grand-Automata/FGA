package io.github.fate_grand_automata.ui.skill_maker

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.fate_grand_automata.R
import io.github.fate_grand_automata.scripts.models.ServantTarget
import io.github.fate_grand_automata.ui.FGATheme
import io.github.fate_grand_automata.ui.FGATitle


@Composable
fun SkillMakerChoice3(
    onSkillTarget: (ServantTarget) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(16.dp)
    ) {
        FGATitle(
            stringResource(R.string.skill_maker_soujuurou)
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
                text = stringResource(R.string.skill_maker_tri_choice_1)
            )

            TargetButton(
                onClick = { onSkillTarget(ServantTarget.SpecialTarget.Choice3OptionB) },
                color = colorResource(R.color.colorArtsResist),
                text = stringResource(R.string.skill_maker_tri_choice_2)
            )

            TargetButton(
                onClick = { onSkillTarget(ServantTarget.SpecialTarget.Choice3OptionC) },
                color = colorResource(R.color.colorBuster),
                text = stringResource(R.string.skill_maker_tri_choice_3)
            )
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