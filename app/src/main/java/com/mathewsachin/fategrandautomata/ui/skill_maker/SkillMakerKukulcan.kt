package com.mathewsachin.fategrandautomata.ui.skill_maker

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.scripts.models.ServantTarget
import com.mathewsachin.fategrandautomata.ui.FGATheme
import com.mathewsachin.fategrandautomata.ui.FGATitle

@Composable
fun SkillMakerKukulcan(
    onOption1: () -> Unit,
    onOption2: () -> Unit,
    goToTarget: Boolean,
    onTarget: (firstTarget: ServantTarget) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(16.dp)
    ) {
        FGATitle(
            stringResource(R.string.skill_maker_kukulcan)
        )

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            TargetButton(
                onClick = if (goToTarget) (
                        { onTarget(ServantTarget.Option1) }
                        ) else onOption1,
                color = MaterialTheme.colorScheme.primary,
                text = stringResource(R.string.skill_maker_option_1)
            )

            TargetButton(
                onClick = if (goToTarget) (
                        { onTarget(ServantTarget.Option2) }
                        ) else onOption2,
                color = MaterialTheme.colorScheme.tertiary,
                text = stringResource(R.string.skill_maker_option_2)
            )
        }
    }
}

@Composable
fun SkillMakerKukulcanTarget(
    onSkillTarget: (ServantTarget) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(16.dp)
    ) {
        FGATitle(
            stringResource(R.string.skill_maker_kukulcan)
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
                color = colorResource(R.color.colorServant1),
                text = stringResource(R.string.skill_maker_target_servant, 1)
            )

            TargetButton(
                onClick = { onSkillTarget(ServantTarget.B) },
                color = colorResource(R.color.colorServant2),
                text = stringResource(R.string.skill_maker_target_servant, 2)
            )

            TargetButton(
                onClick = { onSkillTarget(ServantTarget.C) },
                color = colorResource(R.color.colorServant3),
                text = stringResource(R.string.skill_maker_target_servant, 3)
            )
        }
    }
}

@Preview(name = "Light Mode", widthDp = 600, heightDp = 300)
@Preview(name = "Dark Mode", widthDp = 600, heightDp = 300, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun TestKukulcan() {
    FGATheme {
        SkillMakerKukulcan(onOption1 = { }, onOption2 = { }, goToTarget = true, onTarget = { })
    }
}

@Preview(name = "Light Mode", widthDp = 600, heightDp = 300)
@Preview(name = "Dark Mode", widthDp = 600, heightDp = 300, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun TestKukulcanTarget() {
    FGATheme {
        SkillMakerKukulcanTarget(onSkillTarget = { })
    }
}