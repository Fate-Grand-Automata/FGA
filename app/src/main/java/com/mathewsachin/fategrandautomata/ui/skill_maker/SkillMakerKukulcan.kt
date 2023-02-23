package com.mathewsachin.fategrandautomata.ui.skill_maker

import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.scripts.models.ServantTarget

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
        Text(
            stringResource(R.string.skill_maker_kukulcan),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
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
                color = colorResource(R.color.colorPrimaryDark),
                text = stringResource(R.string.skill_maker_option_1)
            )

            TargetButton(
                onClick = if (goToTarget) (
                        { onTarget(ServantTarget.Option2) }
                        ) else onOption2,
                color = colorResource(R.color.colorPrimaryDark),
                text = stringResource(R.string.skill_maker_option_2)
            )
        }
    }
}

@Preview(widthDp = 600, heightDp = 300)
@Composable
fun TestKukulcan() {
    SkillMakerKukulcan(onOption1 = { }, onOption2 = { }, goToTarget = true, onTarget = { })
}