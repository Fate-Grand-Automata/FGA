package io.github.fate_grand_automata.ui.skill_maker.special

import android.content.res.Configuration
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.fate_grand_automata.R
import io.github.fate_grand_automata.ui.FGATheme
import io.github.fate_grand_automata.ui.FGATitle

@Composable
fun SkillMakerChangeNpType2(
    onTargetLeft: () -> Unit,
    onTargetRight: () -> Unit
) {
    var changeNp2Type by remember { mutableStateOf(ChangeNp2Type.Generic) }

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(16.dp)
    ) {
        FGATitle(
            stringResource(changeNp2Type.stringRes),
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
                text = stringResource(changeNp2Type.targetAStringRes)
            )

            TargetButton(
                onClick = onTargetRight,
                color = colorResource(R.color.colorBuster),
                text = stringResource(changeNp2Type.targetBStringRes)
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
            ChangeNp2Type.entries.filterNot {
                it == ChangeNp2Type.Generic
            }.forEach { entry ->

                val containerColor by animateColorAsState(
                    targetValue = if (changeNp2Type == entry) {
                        MaterialTheme.colorScheme.onSurface.copy(0.12f)
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                    label = "Container Color, if ${entry.name} is selected"
                )
                val contentColor by animateColorAsState(
                    targetValue = if (changeNp2Type == entry) {
                        MaterialTheme.colorScheme.onSurface.copy(0.38f)
                    } else {
                        MaterialTheme.colorScheme.onPrimary
                    },
                    label = "Container Color, if ${entry.name} is selected"
                )
                Button(
                    onClick = {
                        changeNp2Type = if (changeNp2Type != entry) {
                            entry
                        } else {
                            ChangeNp2Type.Generic
                        }
                    },
                    border = if (changeNp2Type == entry) {
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

@Composable
fun TargetButton(
    onClick: () -> Unit,
    color: Color,
    text: String
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = color),
        modifier = Modifier.size(90.dp),
        contentPadding = PaddingValues(1.dp)
    ) {
        Text(
            text,
            color = Color.White,
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Clip
        )
    }
}

@Preview(name = "Light Mode", widthDp = 600, heightDp = 300)
@Preview(name = "Dark Mode", widthDp = 600, heightDp = 300, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun TestChangeNpType2() {
    FGATheme {
        SkillMakerChangeNpType2(onTargetLeft = { }, onTargetRight = { })
    }
}

private enum class ChangeNp2Type {
    Generic,
    Emiya,
    BBDubai
}

private val ChangeNp2Type.stringRes
    get() = when (this) {
        ChangeNp2Type.Generic -> R.string.skill_maker_change_np_type_2
        ChangeNp2Type.Emiya -> R.string.skill_maker_emiya
        ChangeNp2Type.BBDubai -> R.string.skill_maker_bb_dubai
    }

private val ChangeNp2Type.targetAStringRes
    get() = when (this) {
        ChangeNp2Type.Generic -> R.string.skill_maker_option_1
        ChangeNp2Type.Emiya -> R.string.skill_maker_arts
        ChangeNp2Type.BBDubai -> R.string.skill_maker_bb_dubai_target_1
    }

private val ChangeNp2Type.targetBStringRes
    get() = when (this) {
        ChangeNp2Type.Generic -> R.string.skill_maker_option_2
        ChangeNp2Type.Emiya -> R.string.skill_maker_buster
        ChangeNp2Type.BBDubai -> R.string.skill_maker_bb_dubai_target_2
    }