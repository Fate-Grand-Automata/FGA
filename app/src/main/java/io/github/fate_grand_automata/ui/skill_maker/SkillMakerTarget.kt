package io.github.fate_grand_automata.ui.skill_maker

import android.content.res.Configuration
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
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
    slot: SkillSlot?,
    onSkillTarget: (ServantTarget?) -> Unit,
    onNpType2: () -> Unit,
    onNpType3: () -> Unit,
    onChoice2: (SkillSlot) -> Unit,
    onChoice3: (SkillSlot) -> Unit,
    onTransform: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(16.dp),
    ) {
        FGATitle(
            stringResource(R.string.skill_maker_target_header),
        )

        Box(
            modifier = Modifier
                .weight(2f)
                .fillMaxWidth(),
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center),
            ) {
                TargetButton(
                    onClick = { onSkillTarget(ServantTarget.A) },
                    color = colorResource(R.color.colorServant1),
                    text = stringResource(R.string.skill_maker_target_servant, 1),
                )

                TargetButton(
                    onClick = { onSkillTarget(ServantTarget.B) },
                    color = colorResource(R.color.colorServant2),
                    text = stringResource(R.string.skill_maker_target_servant, 2),
                )

                TargetButton(
                    onClick = { onSkillTarget(ServantTarget.C) },
                    color = colorResource(R.color.colorServant3),
                    text = stringResource(R.string.skill_maker_target_servant, 3),
                )
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            Button(
                onClick = { onSkillTarget(null) },
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .padding(bottom = 4.dp)
                    .align(Alignment.TopCenter),
            ) {
                Text(stringResource(R.string.skill_maker_target_none))
            }
        }

        val state = rememberLazyListState()

        val showInitialButton by remember {
            derivedStateOf {
                state.firstVisibleItemIndex > 0
            }
        }
        var showLastButton by remember {
            mutableStateOf(false)
        }
        LaunchedEffect(state) {
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

        Box(
            modifier = Modifier
                .fillMaxWidth(),
        ) {
            Column {
                if (slot != null) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier
                            .fillMaxWidth(),
                    ) {
                        Text(
                            stringResource(R.string.skill_maker_special_targets_warning).uppercase(),
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            textDecoration = TextDecoration.Underline,
                        )
                    }

                    LazyRow(
                        state = state,
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier
                            .fillMaxWidth(),
                    ) {
                        if (slot == SkillSlot.Third) {
                            item {
                                ButtonWithHint(
                                    onClick = onNpType2,
                                    text = stringResource(R.string.skill_maker_change_np_type_2),
                                    hint = stringResource(R.string.skill_maker_change_np_type_2_hint),
                                    image = R.drawable.skill_maker_np_type_2,
                                    servants = stringArrayResource(
                                        R.array.skill_maker_change_np_type_2_array,
                                    ).joinToString("\n"),
                                )
                            }
                        }

                        if (slot == SkillSlot.Second) {
                            item {
                                ButtonWithHint(
                                    onClick = onNpType3,
                                    text = stringResource(R.string.skill_maker_change_np_type_3),
                                    hint = stringResource(R.string.skill_maker_change_np_type_3_hint),
                                    image = R.drawable.skill_maker_np_type_3,
                                    servants = stringArrayResource(
                                        R.array.skill_maker_change_np_type_3_array,
                                    ).joinToString("\n"),
                                )
                            }
                        }

                        item {
                            ButtonWithHint(
                                onClick = {
                                    onChoice2(slot)
                                },
                                text = stringResource(R.string.skill_maker_choices_2),
                                hint = stringResource(R.string.skill_maker_choices_2_hint),
                                image = R.drawable.skill_maker_choices_2,
                                servants = stringArrayResource(
                                    when (slot) {
                                        SkillSlot.Third -> R.array.skill_maker_choices_2_array_slot_3
                                        else -> R.array.skill_maker_choices_2_array_slot_1and2
                                    },
                                ).joinToString("\n"),
                            )
                        }

                        if (slot == SkillSlot.First || slot == SkillSlot.Third) {
                            item {
                                ButtonWithHint(
                                    onClick = {
                                        onChoice3(slot)
                                    },
                                    text = stringResource(R.string.skill_maker_choices_3),
                                    hint = stringResource(R.string.skill_maker_choices_3_hint),
                                    image = R.drawable.skill_maker_choices_3,
                                    servants = stringArrayResource(
                                        when (slot) {
                                            SkillSlot.First -> R.array.skill_maker_choices_3_array_slot_1
                                            else -> R.array.skill_maker_choices_3_array_slot_3
                                        },
                                    ).joinToString("\n"),
                                )
                            }
                        }
                        if (slot == SkillSlot.Third) {
                            item {
                                ButtonWithHint(
                                    onClick = onTransform,
                                    text = stringResource(R.string.skill_maker_transform),
                                    hint = stringResource(R.string.skill_maker_transform_hint),
                                    image = R.drawable.skill_maker_transform,
                                    servants = stringArrayResource(
                                        R.array.skill_maker_transform_array,
                                    ).joinToString("\n"),
                                )
                            }
                        }
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
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                    ),
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardDoubleArrowLeft,
                        contentDescription = "Go to the first item",
                    )
                }
            }
            androidx.compose.animation.AnimatedVisibility(
                visible = showLastButton && listIsNotEmpty,
                enter = fadeIn() + slideInHorizontally { it },
                exit = fadeOut() + slideOutHorizontally { it },
                modifier = Modifier
                    .align(Alignment.CenterEnd),
            ) {
                FilledIconButton(
                    onClick = {
                        scope.launch {
                            state.animateScrollToItem(state.layoutInfo.totalItemsCount - 1)
                        }
                    },
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                    ),
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardDoubleArrowRight,
                        contentDescription = "Go to the last item",
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
    hint: String,
    image: Int,
    servants: String,
) {
    val dialog = FgaDialog()

    dialog.build(
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Text(
                text = hint.uppercase(),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth(),
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth(),
            ) {
                Text(
                    servants,
                )

                AsyncImage(
                    model = image,
                    contentDescription = "Special Skill Target Image",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .padding(4.dp)
                        .weight(1f)
                        .fillMaxHeight(0.7f),
                )
            }
        }

        buttons(
            okLabel = stringResource(R.string.dismiss),
            showCancel = false,
            onSubmit = {
                dialog.hide()
            },
        )
    }

    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Button(
            onClick = onClick,
            contentPadding = PaddingValues(7.dp),
        ) {
            Text(
                text = text,
            )
        }

        IconButton(
            onClick = {
                dialog.show()
            },
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Info",
            )
        }
    }
}

@Composable
@Preview(name = "Light Mode", widthDp = 600, heightDp = 300)
@Preview(name = "Dark Mode", widthDp = 600, heightDp = 300, uiMode = Configuration.UI_MODE_NIGHT_YES)
fun TestSkillMakerTargetSlot1() = TestSkillMaker(slot = SkillSlot.First)

@Composable
@Preview(name = "Light Mode", widthDp = 600, heightDp = 300)
fun TestSkillMakerTargetSlot2() = TestSkillMaker(slot = SkillSlot.Second)

@Composable
@Preview(name = "Light Mode", widthDp = 600, heightDp = 300)
fun TestSkillMakerTargetSlot3() = TestSkillMaker(slot = SkillSlot.Third)

@Composable
@Preview(name = "Light Mode", widthDp = 600, heightDp = 300)
fun TestSkillMakerNoSlot() = TestSkillMaker(slot = null)

@Composable
private fun TestSkillMaker(
    slot: SkillSlot?,
) {
    FGATheme {
        SkillMakerTarget(
            slot = slot,
            onSkillTarget = {},
            onNpType2 = {},
            onNpType3 = {},
            onChoice2 = {},
            onTransform = {},
            onChoice3 = {},
        )
    }
}
