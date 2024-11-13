package io.github.fate_grand_automata.ui.skill_maker

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.fate_grand_automata.R
import io.github.fate_grand_automata.scripts.models.ServantTarget
import io.github.fate_grand_automata.ui.FGATheme
import io.github.fate_grand_automata.ui.FGATitle
import io.github.fate_grand_automata.ui.skill_maker.special.TargetButton
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SkillMakerTarget(
    onSkillTarget: (ServantTarget?) -> Unit,
    showTarget2: Boolean,
    onTarget2: () -> Unit,
    showSpaceIshtar: Boolean,
    onSpaceIshtar: () -> Unit,
    showKukulkan: Boolean,
    onKukulkan: () -> Unit,
    showTransform: Boolean,
    onTransform: () -> Unit,
    showChoice3Slot1: Boolean,
    showChoice3Slot3: Boolean,
    onChoice3: (SkillSlot) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(16.dp)
    ) {
        FGATitle(
            stringResource(R.string.skill_maker_target_header)
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

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            if (showKukulkan) {
                Button(onClick = onKukulkan) {
                    Text(stringResource(R.string.skill_maker_kukulkan))
                }
            }

            if (showTarget2) {
                ButtonWithHint(
                    onClick = onTarget2,
                    text = stringResource(R.string.skill_maker_target_2),
                    hint = stringArrayResource(R.array.skill_maker_target_2_array).joinToString("\n")
                )
            }

            if (showSpaceIshtar) {
                Button(onClick = onSpaceIshtar) {
                    Text(stringResource(R.string.skill_maker_space_ishtar))
                }
            }

            if (showTransform) {
                ButtonWithHint(
                    onClick = onTransform,
                    text = stringResource(R.string.skill_maker_transform),
                    hint = stringArrayResource(R.array.skill_maker_transform_array).joinToString("\n")
                )
            }
            if (showChoice3Slot1 || showChoice3Slot3) {
                ButtonWithHint(
                    onClick = {
                        val slot = if (showChoice3Slot1) SkillSlot.First else SkillSlot.Third
                        onChoice3(slot)
                    },
                    text = stringResource(R.string.skill_maker_tri_choice),
                    hint = stringArrayResource(
                        if (showChoice3Slot1) R.array.skill_maker_tri_choice_array_slot_1
                        else R.array.skill_maker_tri_choice_array_slot_3
                    ).joinToString("\n")
                )
            }

            Button(onClick = { onSkillTarget(null) }) {
                Text(stringResource(R.string.skill_maker_target_none))
            }
        }
    }
}

@Composable
private fun ButtonWithHint(
    onClick: () -> Unit,
    text: String,
    hint: String
) {
    val state = rememberTooltipState(isPersistent = true)
    val scope = rememberCoroutineScope()

    LaunchedEffect(state.isVisible) {
        if (state.isVisible) {
            delay(5000)
            if (state.isVisible) {
                state.dismiss()
            }
        }
    }

    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = onClick
        ) {
            Text(text)
        }
        Box {
            TooltipBox(
                positionProvider = TooltipDefaults.rememberRichTooltipPositionProvider(),
                tooltip = {
                    PlainTooltip(
                        content = {
                            Text(
                                text = hint,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        },
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant

                    )
                },
                state = state
            ) {
                IconButton(
                    onClick = {
                        scope.launch {
                            state.show()
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Info"
                    )
                }
            }
        }
    }

}

@Composable
@Preview(name = "Light Mode", widthDp = 600, heightDp = 300)
@Preview(name = "Dark Mode", widthDp = 600, heightDp = 300, uiMode = Configuration.UI_MODE_NIGHT_YES)
fun TestSkillMakerTargetEmiya() = TestSkillMaker(showEmiya = true)

@Composable
@Preview(name = "Light Mode", widthDp = 600, heightDp = 300)
@Preview(name = "Dark Mode", widthDp = 600, heightDp = 300, uiMode = Configuration.UI_MODE_NIGHT_YES)
fun TestSkillMakerTargetIshtar() = TestSkillMaker(showSpaceIshtar = true)

@Composable
@Preview(name = "Light Mode", widthDp = 600, heightDp = 300)
@Preview(name = "Dark Mode", widthDp = 600, heightDp = 300, uiMode = Configuration.UI_MODE_NIGHT_YES)
fun TestSkillMakerOnlyKukulkan() = TestSkillMaker()

@Composable
@Preview(name = "Light Mode", widthDp = 600, heightDp = 300)
@Preview(name = "Dark Mode", widthDp = 600, heightDp = 300, uiMode = Configuration.UI_MODE_NIGHT_YES)
fun TestSkillMakerTargetChoice3Slot1() = TestSkillMaker(showChoice3Slot1 = true)

@Composable
@Preview(name = "Light Mode", widthDp = 600, heightDp = 300)
@Preview(name = "Dark Mode", widthDp = 600, heightDp = 300, uiMode = Configuration.UI_MODE_NIGHT_YES)
fun TestSkillMakerTargetChoice3Slot3() = TestSkillMaker(showChoice3Slot3 = true)

@Composable
private fun TestSkillMaker(
    showEmiya: Boolean = false,
    showKukulkan: Boolean = false,
    showSpaceIshtar: Boolean = false,
    showMelusine: Boolean = showEmiya,
    showChoice3Slot1: Boolean = false,
    showChoice3Slot3: Boolean = false
) {
    FGATheme {
        SkillMakerTarget(
            onSkillTarget = {},
            showTarget2 = showEmiya,
            onTarget2 = {},
            showSpaceIshtar = showSpaceIshtar,
            onSpaceIshtar = {},
            showKukulkan = showKukulkan,
            onKukulkan = {},
            showTransform = showMelusine,
            onTransform = {},
            showChoice3Slot1 = showChoice3Slot1,
            showChoice3Slot3 = showChoice3Slot3,
            onChoice3 = {}
        )
    }
}