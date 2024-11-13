package io.github.fate_grand_automata.ui.skill_maker

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.fate_grand_automata.R
import io.github.fate_grand_automata.ui.FGATheme
import io.github.fate_grand_automata.ui.FGATitle

@Composable
fun SkillMakerTarget2(
    onTargetLeft: () -> Unit,
    onTargetRight: () -> Unit
) {
    var target2Type by remember { mutableStateOf(Target2Type.Emiya) }

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(16.dp)
    ) {
        FGATitle(
            stringResource(target2Type.stringRes),
        )

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            TargetButton(
                onClick = onTargetLeft,
                color = colorResource(R.color.colorArtsResist),
                text = stringResource(target2Type.targetAStringRes)
            )

            TargetButton(
                onClick = onTargetRight,
                color = colorResource(R.color.colorBuster),
                text = stringResource(target2Type.targetBStringRes)
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
            Target2Type.entries.forEach { entry ->
                Button(
                    onClick = {
                        target2Type = entry
                    },
                    enabled = target2Type != entry,
                    border = if (target2Type == entry) {
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

@Preview(name = "Light Mode", widthDp = 600, heightDp = 300)
@Preview(name = "Dark Mode", widthDp = 600, heightDp = 300, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun TestEmiya() {
    FGATheme {
        SkillMakerTarget2(onTargetLeft = { }, onTargetRight = { })
    }
}

private enum class Target2Type {
    Emiya,
    BBDubai
}

private val Target2Type.stringRes
    get() = when (this) {
        Target2Type.Emiya -> R.string.skill_maker_emiya
        Target2Type.BBDubai -> R.string.skill_maker_bb_dubai
    }

private val Target2Type.targetAStringRes
    get() = when (this) {
        Target2Type.Emiya -> R.string.skill_maker_arts
        Target2Type.BBDubai -> R.string.skill_maker_bb_dubai_target_1
    }

private val Target2Type.targetBStringRes
    get() = when (this) {
        Target2Type.Emiya -> R.string.skill_maker_buster
        Target2Type.BBDubai -> R.string.skill_maker_bb_dubai_target_2
    }