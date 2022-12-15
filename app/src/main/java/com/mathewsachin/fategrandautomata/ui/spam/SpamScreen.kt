package com.mathewsachin.fategrandautomata.ui.spam

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.scripts.enums.SpamEnum
import com.mathewsachin.fategrandautomata.scripts.models.SkillSpamTarget
import com.mathewsachin.fategrandautomata.ui.FGAListItemColors
import com.mathewsachin.fategrandautomata.ui.Heading
import com.mathewsachin.fategrandautomata.ui.HeadingButton
import com.mathewsachin.fategrandautomata.ui.prefs.MultiSelectChip
import com.mathewsachin.fategrandautomata.ui.prefs.SwitchPreference
import com.mathewsachin.fategrandautomata.ui.prefs.listDialog
import com.mathewsachin.fategrandautomata.util.stringRes
import kotlinx.coroutines.launch

@Composable
fun SpamScreen(
    vm: SpamScreenViewModel = viewModel()
) {
    DisposableEffect(vm) {
        onDispose {
            vm.save()
        }
    }

    val pagerState = rememberPagerState()
    val scope = rememberCoroutineScope()

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
                    val isSelected = pagerState.currentPage == it - 1

                    Box(
                        modifier = Modifier
                            .background(
                                color = if (isSelected) MaterialTheme.colorScheme.secondary else Color.Transparent,
                                shape = MaterialTheme.shapes.medium
                            )
                            .clickable { scope.launch { pagerState.animateScrollToPage(it - 1) } }
                            .padding(14.dp, 5.dp)
                    ) {
                        Text(
                            it.toString(),
                            color = if (isSelected) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Divider()
        }

        item {
            HorizontalPager(
                state = pagerState,
                verticalAlignment = Alignment.Top,
                count = vm.spamStates.size
            ) {
                SpamView(
                    selectedConfig = vm.spamStates[it]
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
        Text(stringResource(R.string.spam_np))

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
        entries = SpamEnum.values().associateWith { stringResource(it.stringRes) },
        title = stringResource(R.string.spam)
    )

    ListItem(
        headlineText = { Text(stringResource(R.string.spam)) },
        supportingText = { Text(stringResource(selected.stringRes)) },
        modifier = modifier
            .clickable { dialog.show() },
        colors = FGAListItemColors()
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
        title = stringResource(R.string.spam_target)
    )

    ListItem(
        headlineText = { Text(stringResource(R.string.spam_target)) },
        supportingText = { Text(selected.toString()) },
        modifier = modifier
            .clickable { dialog.show() },
        colors = FGAListItemColors()
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