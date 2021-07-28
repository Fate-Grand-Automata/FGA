package com.mathewsachin.fategrandautomata.ui.spam

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.scripts.enums.SpamEnum
import com.mathewsachin.fategrandautomata.scripts.models.SkillSpamTarget
import com.mathewsachin.fategrandautomata.ui.Heading
import com.mathewsachin.fategrandautomata.ui.HeadingButton
import com.mathewsachin.fategrandautomata.ui.prefs.MultiSelectChip
import com.mathewsachin.fategrandautomata.ui.prefs.SwitchPreference
import com.mathewsachin.fategrandautomata.ui.prefs.listDialog

@Composable
fun SpamScreen(
    vm: SpamScreenViewModel = viewModel()
) {
    DisposableEffect(vm) {
        onDispose {
            vm.save()
        }
    }

    LazyColumn {
        item {
            Heading(stringResource(R.string.p_spam_spam))
        }

        item {
            vm.battleConfigCore.autoChooseTarget.SwitchPreference(
                title = stringResource(R.string.p_auto_choose_target),
                summary = stringResource(R.string.p_spam_summary)
            )

            Divider()
        }

        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(16.dp, 5.dp)
            ) {
                Text(
                    "Servant:",
                    modifier = Modifier.padding(end = 16.dp)
                )

                (1..vm.spamStates.size).map {
                    val isSelected = vm.selectedServant == it - 1

                    Box(
                        modifier = Modifier
                            .background(
                                color = if (isSelected) MaterialTheme.colors.secondary else Color.Transparent,
                                shape = MaterialTheme.shapes.medium
                            )
                            .clickable { vm.selectedServant = it - 1 }
                            .padding(14.dp, 5.dp)
                    ) {
                        Text(
                            it.toString(),
                            color = if (isSelected) MaterialTheme.colors.onSecondary else MaterialTheme.colors.onSurface
                        )
                    }
                }
            }

            Divider()
        }

        item {
            val selectedConfig = vm.spamStates[vm.selectedServant]

            AnimatedContent(
                targetState = selectedConfig
            ) {
                SpamView(
                    selectedConfig = it
                )
            }
        }

        item {
            Divider(
                modifier = Modifier.padding(vertical = 16.dp)
            )

            Text(
                "PRESETS",
                modifier = Modifier
                    .padding(16.dp, 5.dp)
            )
        }

        item {
            LazyRow(
                contentPadding = PaddingValues(16.dp, 5.dp)
            ) {
                items(vm.presets) { preset ->
                    HeadingButton(
                        text = preset.name,
                        onClick = { preset.action(vm.spamStates) }
                    )
                }
            }
        }
    }
}

@Composable
private fun NpSpamView(
    spamConfig: SpamScreenViewModel.NpSpamState
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(start = 16.dp)
    ) {
        Text("NP:")

        var selectedSpamMode by spamConfig.spamMode
        var selectedWaves by spamConfig.waves

        SelectSpamMode(
            selected = selectedSpamMode,
            onSelectChange = { selectedSpamMode = it },
            modifier = Modifier.weight(1f)
        )

        if (selectedSpamMode != SpamEnum.None) {
            SelectWaves(
                selected = selectedWaves,
                onSelectChange = { selectedWaves = it },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SkillSpamView(
    index: Int,
    skillConfig: SpamScreenViewModel.SkillSpamState
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(start = 16.dp)
    ) {
        Text("S${index + 1}:")

        var selectedSpamMode by skillConfig.spamMode
        var selectedTarget by skillConfig.target
        var selectedWaves by skillConfig.waves

        SelectSpamMode(
            selected = selectedSpamMode,
            onSelectChange = { selectedSpamMode = it },
            modifier = Modifier.weight(1f)
        )

        if (selectedSpamMode != SpamEnum.None) {
            SelectTarget(
                selected = selectedTarget,
                onSelectChange = { selectedTarget = it },
                modifier = Modifier.weight(1f)
            )

            SelectWaves(
                selected = selectedWaves,
                onSelectChange = { selectedWaves = it },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SpamView(
    selectedConfig: SpamScreenViewModel.SpamState
) {
    Column {
        Card(
            modifier = Modifier
                .padding(16.dp)
        ) {
            NpSpamView(spamConfig = selectedConfig.np)
        }

        Card(
            modifier = Modifier
                .padding(horizontal = 16.dp)
        ) {
            Column {
                selectedConfig.skills.mapIndexed { index, skillConfig ->
                    if (index != 0) {
                        Divider()
                    }

                    SkillSpamView(
                        index = index,
                        skillConfig = skillConfig
                    )
                }
            }
        }
    }
}

@Composable
private fun SelectSpamMode(
    selected: SpamEnum,
    onSelectChange: (SpamEnum) -> Unit,
    modifier: Modifier = Modifier
) {
    val dialog = listDialog(
        selected = selected,
        onSelectedChange = onSelectChange,
        entries = SpamEnum.values().associateWith { it.toString() },
        title = "Spam mode"
    )

    ListItem(
        text = { Text("Mode") },
        secondaryText = { Text(selected.toString()) },
        modifier = modifier
            .clickable { dialog.show() }
    )
}

@Composable
private fun SelectTarget(
    selected: SkillSpamTarget,
    onSelectChange: (SkillSpamTarget) -> Unit,
    modifier: Modifier = Modifier
) {
    val dialog = listDialog(
        selected = selected,
        onSelectedChange = onSelectChange,
        entries = SkillSpamTarget.values().associateWith { it.toString() },
        title = "Target"
    )

    ListItem(
        text = { Text("Target") },
        secondaryText = { Text(selected.toString()) },
        modifier = modifier
            .clickable { dialog.show() }
    )
}

@Composable
private fun SelectWaves(
    selected: Set<Int>,
    onSelectChange: (Set<Int>) -> Unit,
    modifier: Modifier = Modifier
) {
    MultiSelectChip(
        title = "Waves",
        selected = selected,
        onSelectedChange = onSelectChange,
        entries = (1..3).associateWith { "$it" },
        modifier = modifier
    )
}