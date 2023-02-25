package com.mathewsachin.fategrandautomata.ui.skill_maker

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
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
fun SkillMakerSpaceIshtar(
    onSkillTarget: (ServantTarget) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(16.dp)
    ) {
        FGATitle(
            stringResource(R.string.skill_maker_space_ishtar)
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
                text = stringResource(R.string.skill_maker_quick)
            )

            TargetButton(
                onClick = { onSkillTarget(ServantTarget.B) },
                color = colorResource(R.color.colorArtsResist),
                text = stringResource(R.string.skill_maker_arts)
            )

            TargetButton(
                onClick = { onSkillTarget(ServantTarget.C) },
                color = colorResource(R.color.colorBuster),
                text = stringResource(R.string.skill_maker_buster)
            )
        }
    }
}

@Preview(name = "Light Mode", widthDp = 600, heightDp = 300)
@Preview(name = "Dark Mode", widthDp = 600, heightDp = 300, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun TestSpaceIshtar() {
    FGATheme {
        SkillMakerSpaceIshtar(onSkillTarget = { })
    }
}