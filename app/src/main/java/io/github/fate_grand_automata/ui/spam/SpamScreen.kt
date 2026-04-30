package io.github.fate_grand_automata.ui.spam

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.fate_grand_automata.R
import io.github.fate_grand_automata.scripts.enums.SpamEnum
import io.github.fate_grand_automata.scripts.models.SkillSpamTarget
import io.github.fate_grand_automata.ui.FGAListItemColors
import io.github.fate_grand_automata.ui.Heading
import io.github.fate_grand_automata.ui.HeadingButton
import io.github.fate_grand_automata.ui.prefs.MultiSelectChip
import io.github.fate_grand_automata.ui.prefs.SwitchPreference
import io.github.fate_grand_automata.ui.prefs.listDialog
import io.github.fate_grand_automata.util.stringRes
import kotlinx.coroutines.launch

@Composable
fun SpamScreen(
    vm: SpamScreenViewModel = viewModel(),
) {
    DisposableEffect(vm) {
        onDispose {
            vm.save()
        }
    }

    val pagerState = rememberPagerState(pageCount = { vm.spamStates.size })
    val scope = rememberCoroutineScope()

    LazyColumn {
        item {
            Heading(stringResource(R.string.p_spam_spam))
        }

        item {
            vm.battleConfigCore.autoChooseTarget.SwitchPreference(
                title = stringResource(R.string.p_auto_choose_target),
                summary = stringResource(R.string.p_spam_summary),
            )

            HorizontalDivider()
        }

        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(16.dp, 5.dp),
            ) {
                Text(
                    "Servant:",
                    modifier = Modifier.padding(end = 16.dp),
                )

                (1..vm.spamStates.size).map {
                    val isSelected = pagerState.currentPage == it - 1

                    Box(
                        modifier = Modifier
                            .background(
                                color = if (isSelected) MaterialTheme.colorScheme.secondary else Color.Transparent,
                                shape = MaterialTheme.shapes.medium,
                            )
                            .clickable { scope.launch { pagerState.animateScrollToPage(it - 1) } }
                            .padding(14.dp, 5.dp),
                    ) {
                        Text(
                            it.toString(),
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.onSecondary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        )
                    }
                }
            }

            HorizontalDivider()
        }

        item {
            HorizontalPager(
                state = pagerState,
                verticalAlignment = Alignment.Top,
            ) {
                SpamView(
                    selectedConfig = vm.spamStates[it],
                )
            }
        }

        item {
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 16.dp),
            )

            Text(
                "PRESETS",
                modifier = Modifier
                    .padding(16.dp, 5.dp),
            )
        }

        item {
            LazyRow(
                contentPadding = PaddingValues(10.dp, 5.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                items(vm.presets) { preset ->
                    HeadingButton(
                        text = preset.name,
                        onClick = { preset.action(vm.spamStates) },
                    )
                }
            }
        }
    }
}

@Composable
private fun NpSpamView(
    spamConfig: SpamScreenViewModel.NpSpamState,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(start = 16.dp),
    ) {
        Text(stringResource(R.string.spam_np))

        var selectedSpamMode by spamConfig.spamMode
        var selectedWaves by spamConfig.waves

        SelectSpamMode(
            selected = selectedSpamMode,
            onSelectChange = { selectedSpamMode = it },
            modifier = Modifier.weight(1f),
        )

        if (selectedSpamMode != SpamEnum.None) {
            SelectWaves(
                selected = selectedWaves,
                onSelectChange = { selectedWaves = it },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun SkillSpamView(
    index: Int,
    skillConfig: SpamScreenViewModel.SkillSpamState,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(start = 16.dp),
    ) {
        Text("S${index + 1}:")

        var selectedSpamMode by skillConfig.spamMode
        var selectedTarget by skillConfig.target
        var selectedWaves by skillConfig.waves

        SelectSpamMode(
            selected = selectedSpamMode,
            onSelectChange = { selectedSpamMode = it },
            modifier = Modifier.weight(1f),
        )

        if (selectedSpamMode != SpamEnum.None) {
            SelectTarget(
                selected = selectedTarget,
                onSelectChange = { selectedTarget = it },
                modifier = Modifier.weight(1f),
            )

            SelectWaves(
                selected = selectedWaves,
                onSelectChange = { selectedWaves = it },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun SpamView(
    selectedConfig: SpamScreenViewModel.SpamState,
) {
    Column {
        Card(
            modifier = Modifier
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
        ) {
            NpSpamView(spamConfig = selectedConfig.np)
        }

        Card(
            modifier = Modifier
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
        ) {
            Column {
                selectedConfig.skills.mapIndexed { index, skillConfig ->
                    if (index != 0) {
                        HorizontalDivider()
                    }

                    SkillSpamView(
                        index = index,
                        skillConfig = skillConfig,
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
    modifier: Modifier = Modifier,
) {
    val dialog = listDialog(
        selected = selected,
        onSelectedChange = onSelectChange,
        entries = SpamEnum.entries.associateWith { stringResource(it.stringRes) },
        title = stringResource(R.string.spam),
    )

    ListItem(
        headlineContent = { Text(stringResource(R.string.spam)) },
        supportingContent = { Text(stringResource(selected.stringRes)) },
        modifier = modifier
            .clickable { dialog.show() },
        colors = FGAListItemColors(),
    )
}

@Composable
private fun SelectTarget(
    selected: SkillSpamTarget,
    onSelectChange: (SkillSpamTarget) -> Unit,
    modifier: Modifier = Modifier,
) {
    val dialog = listDialog(
        selected = selected,
        onSelectedChange = onSelectChange,
        entries = SkillSpamTarget.entries.associateWith { it.toString() },
        title = stringResource(R.string.spam_target),
    )

    ListItem(
        headlineContent = { Text(stringResource(R.string.spam_target)) },
        supportingContent = { Text(selected.toString()) },
        modifier = modifier
            .clickable { dialog.show() },
        colors = FGAListItemColors(),
    )
}

@Composable
private fun SelectWaves(
    selected: Set<Int>,
    onSelectChange: (Set<Int>) -> Unit,
    modifier: Modifier = Modifier,
) {
    MultiSelectChip(
        title = "Waves",
        selected = selected,
        onSelectedChange = onSelectChange,
        entries = (1..3).associateWith { "$it" },
        modifier = modifier,
    )
}
