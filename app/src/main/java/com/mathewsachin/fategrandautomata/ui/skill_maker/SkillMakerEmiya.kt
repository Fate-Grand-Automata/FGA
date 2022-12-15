package com.mathewsachin.fategrandautomata.ui.skill_maker

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mathewsachin.fategrandautomata.R

@Composable
fun SkillMakerEmiya(
    onArts: () -> Unit,
    onBuster: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(16.dp)
    ) {
        Text(
            stringResource(R.string.skill_maker_emiya),
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
                onClick = onArts,
                color = colorResource(R.color.colorArtsResist),
                text = stringResource(R.string.skill_maker_arts)
            )

            TargetButton(
                onClick = onBuster,
                color = colorResource(R.color.colorBuster),
                text = stringResource(R.string.skill_maker_buster)
            )
        }
    }
}

@Composable
fun TargetButton(
    onClick: () -> Unit,
    color: Color,
    text: String
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = color),
        modifier = Modifier.size(120.dp)
    ) {
        Text(
            text,
            color = Color.White,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(widthDp = 600, heightDp = 300)
@Composable
fun TestEmiya() {
    SkillMakerEmiya(onArts = { }, onBuster = { })
}