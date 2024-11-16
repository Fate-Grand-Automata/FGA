package io.github.fate_grand_automata.ui.skill_maker

import android.content.res.Configuration
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardDoubleArrowLeft
import androidx.compose.material.icons.filled.KeyboardDoubleArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import io.github.fate_grand_automata.R
import io.github.fate_grand_automata.scripts.models.ServantTarget
import io.github.fate_grand_automata.ui.FGATheme
import io.github.fate_grand_automata.ui.FGATitle
import io.github.fate_grand_automata.ui.dialog.FgaDialog
import io.github.fate_grand_automata.ui.skill_maker.special.TargetButton
import kotlinx.coroutines.launch

@Composable
fun SkillMakerTarget(
    onSkillTarget: (ServantTarget?) -> Unit,
    showTwoTargets: Boolean,
    onTwoTargets: () -> Unit,
    showThreeTargets: Boolean,
    onThreeTargets: () -> Unit,
    showChoice2: Boolean,
    onChoice2: () -> Unit,
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

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ){
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
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

            Button(
                onClick = { onSkillTarget(null) },
                modifier = Modifier.padding(horizontal = 4.dp)
                    .align(Alignment.BottomCenter)
            ) {
                Text(stringResource(R.string.skill_maker_target_none))
            }
        }


        val state = rememberLazyListState()

        val showInitialButton by remember{
            derivedStateOf {
                state.firstVisibleItemIndex > 0
            }
        }
        var showLastButton by remember{
            mutableStateOf(false)
        }
        LaunchedEffect(state){
            snapshotFlow { state.layoutInfo.visibleItemsInfo }
                .collect {
                    showLastButton = it.lastOrNull()?.index != state.layoutInfo.totalItemsCount - 1
                }
        }
        val scope = rememberCoroutineScope()

        val listIsNotEmpty by remember {
            derivedStateOf {
                state.layoutInfo.visibleItemsInfo.isNotEmpty()
            }
        }

        if (listIsNotEmpty) {
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text(
                    stringResource(R.string.skill_maker_special_targets_warning).uppercase(),
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    textDecoration = TextDecoration.Underline
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
        ){
            LazyRow(
                state = state,
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
            ) {
                if (showChoice2) {
                    item {
                        ButtonWithHint(
                            onClick = onChoice2,
                            text = stringResource(R.string.skill_maker_choices_2),
                            imagePath = stringResource(R.string.skill_maker_choices_2_image_path),
                            servants = stringArrayResource(R.array.skill_maker_choices_2_array).joinToString("\n")
                        )
                    }
                }

                if (showTwoTargets) {
                    item {
                        ButtonWithHint(
                            onClick = onTwoTargets,
                            text = stringResource(R.string.skill_maker_two_targets),
                            imagePath = stringResource(R.string.skill_maker_two_targets_image_path),
                            servants = stringArrayResource(R.array.skill_maker_two_targets_array).joinToString("\n")
                        )
                    }
                }

                if (showThreeTargets) {
                    item {
                        ButtonWithHint(
                            onClick = onThreeTargets,
                            text = stringResource(R.string.skill_maker_three_targets),
                            imagePath = stringResource(R.string.skill_maker_three_targets_image_path),
                            servants = stringArrayResource(R.array.skill_maker_three_targets_array).joinToString("\n")
                        )
                    }
                }
                if (showChoice3Slot1 || showChoice3Slot3) {
                    item {
                        ButtonWithHint(
                            onClick = {
                                val slot = if (showChoice3Slot1) SkillSlot.First else SkillSlot.Third
                                onChoice3(slot)
                            },
                            text = stringResource(R.string.skill_maker_choices_3),
                            imagePath = stringResource(R.string.skill_maker_choices_3_image_path),
                            servants = stringArrayResource(
                                if (showChoice3Slot1) R.array.skill_maker_choices_3_array_slot_1
                                else R.array.skill_maker_choices_3_array_slot_3
                            ).joinToString("\n")
                        )
                    }
                }
                if (showTransform) {
                    item {
                        ButtonWithHint(
                            onClick = onTransform,
                            text = stringResource(R.string.skill_maker_transform),
                            imagePath = stringResource(R.string.skill_maker_transform_image_path),
                            servants = stringArrayResource(R.array.skill_maker_transform_array).joinToString("\n")
                        )
                    }
                }
            }

            // fully qualified name for box and AnimatedVisibility, don't know why
            // https://stackoverflow.com/a/69669445/14859274
            androidx.compose.animation.AnimatedVisibility(
                visible = showInitialButton,
                enter = fadeIn() + slideInHorizontally { -it },
                exit = fadeOut() + slideOutHorizontally { -it },
                modifier = Modifier
                    .align(Alignment.CenterStart),
            ) {
                FilledIconButton(
                    onClick = {
                        scope.launch {
                            state.animateScrollToItem(0)
                        }
                    },
                    colors= IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardDoubleArrowLeft,
                        contentDescription = "Go to the first item"
                    )
                }
            }
            androidx.compose.animation.AnimatedVisibility(
                visible = showLastButton && listIsNotEmpty,
                enter = fadeIn() + slideInHorizontally { it },
                exit = fadeOut() + slideOutHorizontally { it },
                modifier = Modifier
                    .align(Alignment.CenterEnd),
            ){
                FilledIconButton (
                    onClick = {
                        scope.launch {
                            state.animateScrollToItem(state.layoutInfo.totalItemsCount - 1)
                        }
                    },
                    colors= IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardDoubleArrowRight,
                        contentDescription = "Go to the last item"
                    )
                }
            }
        }
    }
}

@Composable
private fun ButtonWithHint(
    onClick: () -> Unit,
    text: String,
    imagePath: String,
    servants: String
) {
    val dialog = FgaDialog()

    dialog.build(
        color = MaterialTheme.colorScheme.background,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(servants)

            AsyncImage(
                model = imagePath,
                contentDescription = "Special Skill Target Image",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(0.4f)
            )
        }

        buttons(
            okLabel = stringResource(R.string.dismiss),
            showCancel = false,
            onSubmit = {
                dialog.hide()
            }
        )

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

        IconButton(
            onClick = {
                dialog.show()
            }
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Info"
            )
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
fun TestSkillMakerChoice2() = TestSkillMaker(showChoice2 = true)

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
    showChoice2: Boolean = false,
    showSpaceIshtar: Boolean = false,
    showMelusine: Boolean = showEmiya,
    showChoice3Slot1: Boolean = false,
    showChoice3Slot3: Boolean = false
) {
    FGATheme {
        SkillMakerTarget(
            onSkillTarget = {},
            showTwoTargets = showEmiya,
            onTwoTargets = {},
            showThreeTargets = showSpaceIshtar,
            onThreeTargets = {},
            showChoice2 = showChoice2,
            onChoice2 = {},
            showTransform = showMelusine,
            onTransform = {},
            showChoice3Slot1 = showChoice3Slot1,
            showChoice3Slot3 = showChoice3Slot3,
            onChoice3 = {}
        )
    }
}