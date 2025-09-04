package io.github.fate_grand_automata.ui.spam

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.fate_grand_automata.R
import io.github.fate_grand_automata.scripts.enums.NpGaugeEnum
import io.github.fate_grand_automata.scripts.enums.SpamEnum
import io.github.fate_grand_automata.scripts.enums.StarConditionEnum
import io.github.fate_grand_automata.scripts.models.AutoSkillAction
import io.github.fate_grand_automata.scripts.models.ServantTarget
import io.github.fate_grand_automata.scripts.models.Skill
import io.github.fate_grand_automata.ui.FGAListItemColors
import io.github.fate_grand_automata.ui.Heading
import io.github.fate_grand_automata.ui.HeadingButton
import io.github.fate_grand_automata.ui.dialog.FgaDialog
import io.github.fate_grand_automata.ui.prefs.MultiSelectChip
import io.github.fate_grand_automata.ui.prefs.SingleSelectChip
import io.github.fate_grand_automata.ui.prefs.SwitchPreference
import io.github.fate_grand_automata.ui.prefs.listDialog
import io.github.fate_grand_automata.ui.skill_maker.SkillMakerNav
import io.github.fate_grand_automata.ui.skill_maker.SkillMakerTarget
import io.github.fate_grand_automata.ui.skill_maker.slot
import io.github.fate_grand_automata.ui.skill_maker.special.SkillMakerChangeNpType2
import io.github.fate_grand_automata.ui.skill_maker.special.SkillMakerChangeNpType3
import io.github.fate_grand_automata.ui.skill_maker.special.SkillMakerChoice2
import io.github.fate_grand_automata.ui.skill_maker.special.SkillMakerChoice2Target
import io.github.fate_grand_automata.ui.skill_maker.special.SkillMakerChoice3
import io.github.fate_grand_automata.util.stringRes
import kotlinx.coroutines.launch
import kotlin.collections.associateWith

@Composable
fun SpamScreen(
    vm: SpamScreenViewModel = viewModel()
) {
    DisposableEffect(vm) {
        onDispose {
            vm.save()
        }
    }

    val pagerState = rememberPagerState(pageCount = { vm.spamStates.size })
    val scope = rememberCoroutineScope()
    var picker by remember { mutableStateOf<TargetPickerRequest?>(null) }

    Box(modifier = Modifier.fillMaxSize())
    {
        LazyColumn {
            item {
                Heading(stringResource(R.string.spam))
            }

            item {
                vm.battleConfigCore.autoChooseTarget.SwitchPreference(
                    title = stringResource(R.string.p_auto_choose_target),
                    summary = stringResource(R.string.p_spam_summary)
                )

                HorizontalDivider()
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

                HorizontalDivider()
            }

            item {
                HorizontalPager(
                    state = pagerState,
                    verticalAlignment = Alignment.Top,
                ) {
                    SpamView(
                        selectedConfig = vm.spamStates[it],
                        onOpenTargetSelector = { req -> picker = req }
                    )
                }
            }

            item {
                HorizontalDivider(
                    modifier = Modifier.padding(top = 16.dp)
                )

                val skillList = remember {
                    vm.spamStates.flatMapIndexed { servantIndex, spamState ->
                        spamState.skills.mapIndexed { skillIndex, skillState ->
                            SkillWithServantRef(
                                servantIndex = servantIndex,
                                skillIndex = skillIndex,
                                state = skillState
                            )
                        }
                    }.sortedBy { it.state.priority.intValue }
                    .toMutableStateList()
                }

                SpamSkillPriorityView(
                    skillList,
                    onSkillDragged = vm::onSkillDragged
                )
            }

            item {
                HorizontalDivider(
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
                    contentPadding = PaddingValues(10.dp, 5.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
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

        // FullScreen target choice UI
        picker?.let { req ->
            TargetSelectorOverlay(
                skill = req.skill,
                onConfirm = { targets ->
                    req.onConfirm(targets)
                    picker = null
                },
                onDismiss = { picker = null }
            )
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
        Text(stringResource(R.string.spam_np),
            color = MaterialTheme.colorScheme.onSurface)

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

private fun Int.toOrdinal(): String {
    if (this <= 0) return this.toString()
    return when (this % 100) {
        11, 12, 13 -> "${this}th"
        else -> when (this % 10) {
            1 -> "${this}st"
            2 -> "${this}nd"
            3 -> "${this}rd"
            else -> "${this}th"
        }
    }
}

@Composable
private fun SkillSpamView(
    index: Int,
    skillConfig: SpamScreenViewModel.SkillSpamState,
    onOpenTargetSelector: (TargetPickerRequest) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(start = 16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "S${index + 1}:",
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 4.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                (skillConfig.priority.intValue + 1).toOrdinal(),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        var selectedSpamMode by skillConfig.spamMode
        var selectedNpMode by skillConfig.npMode
        var selectedStarCond by skillConfig.starCond
        var selectedWaves by skillConfig.waves
        var selectedAction by skillConfig.act
        var selectedRepeat by skillConfig.repeatLimit

        SelectSkillSpamMode(
            selectedSpam = selectedSpamMode,
            selectedNp = selectedNpMode,
            selectedStar = selectedStarCond,
            selectedMaxRepeat = selectedRepeat,
            onSelectChange = { spam, np, star, repeat ->
                selectedSpamMode = spam
                selectedNpMode = np
                selectedStarCond = star
                selectedRepeat = repeat
            },
            modifier = Modifier.weight(1f)
        )

        if (selectedSpamMode != SpamEnum.None) {
            val skill = Skill.Servant.list.take(3)[index]
            SelectTarget(
                skill = skill,
                targets = (selectedAction as? AutoSkillAction.ServantSkill)?.targets.orEmpty(),
                onSelectChange = { targets ->
                    selectedAction = when (targets.size) {
                        0 -> null
                        else -> AutoSkillAction.ServantSkill(skill, targets)
                    }
                },
                onOpenTargetSelector = onOpenTargetSelector,
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
    selectedConfig: SpamScreenViewModel.SpamState,
    onOpenTargetSelector: (TargetPickerRequest) -> Unit,
) {
    Column {
        Card(
            modifier = Modifier
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        ) {
            NpSpamView(spamConfig = selectedConfig.np)
        }

        Card(
            modifier = Modifier
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        ) {
            Column {
                selectedConfig.skills.mapIndexed { index, skillConfig ->
                    if (index != 0) {
                        HorizontalDivider()
                    }

                    SkillSpamView(
                        index = index,
                        skillConfig = skillConfig,
                        onOpenTargetSelector = onOpenTargetSelector
                    )
                }
            }
        }
    }
}

@Composable
private fun SelectSkillSpamMode(
    selectedSpam: SpamEnum,
    selectedNp: NpGaugeEnum,
    selectedStar: StarConditionEnum,
    selectedMaxRepeat: Int,
    onSelectChange: (SpamEnum, NpGaugeEnum, StarConditionEnum, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var tempSpam by remember { mutableStateOf(selectedSpam) }
    var tempNp by remember { mutableStateOf(selectedNp) }
    var tempStar by remember { mutableStateOf(selectedStar) }
    var tempMaxRepeat by remember { mutableStateOf(selectedMaxRepeat) }
    var infiniteRepeat by remember { mutableStateOf(selectedMaxRepeat == 99) }

    val dialog = FgaDialog()

    dialog.build {
        // Header
        Row (
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ){
            Box(
                modifier = Modifier
                    .weight(1f)
                    .alignByBaseline()
            ) {
                title(stringResource(R.string.spam))
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.padding(end = 16.dp)
            ) {
                Text(
                    text = stringResource(R.string.p_spam_repeat_until_used),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Switch(
                    checked = infiniteRepeat,
                    onCheckedChange = { checked ->
                        infiniteRepeat = checked
                        tempMaxRepeat = if (checked) 99 else 1
                    }
                )
            }
        }

        Card(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 0.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        ) {
            // SpamEnum selection
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(start = 16.dp)
            ) {
                SingleSelectChip(
                    title = stringResource(R.string.p_spam_mode),
                    selected = tempSpam,
                    onSelectedChange = { tempSpam = it },
                    entries = SpamEnum.entries.associateWith { stringResource(it.stringRes) }
                )
            }
            // NpGaugeEnum
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(start = 16.dp)
            ) {
                SingleSelectChip(
                    title = stringResource(R.string.p_np_condition),
                    selected = tempNp,
                    onSelectedChange = { tempNp = it },
                    entries = NpGaugeEnum.entries.associateWith { stringResource(it.stringRes) }
                )
            }
            // StarConditionEnum
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(start = 16.dp)
            ) {
                SingleSelectChip(
                    title = stringResource(R.string.p_star_condition),
                    selected = tempStar,
                    onSelectedChange = { tempStar = it },
                    entries = StarConditionEnum.entries.associateWith { stringResource(it.stringRes) }
                )
            }
        }

        buttons(
            onSubmit = { onSelectChange(tempSpam, tempNp, tempStar, tempMaxRepeat) },
            onCancel = {
                tempSpam = selectedSpam
                tempNp = selectedNp
                tempStar = selectedStar
                tempMaxRepeat = selectedMaxRepeat
                infiniteRepeat = selectedMaxRepeat == 99
            }
        )
    }

    val supportingText = when (selectedSpam) {
        SpamEnum.None -> stringResource(R.string.p_spam_supporting_none_hint) // Tap to change
        else -> {
            val npText = when (selectedNp) {
                NpGaugeEnum.None -> ""
                else -> stringResource(selectedNp.stringRes)
            }

            val starText = when (selectedStar) {
                StarConditionEnum.None -> ""
                else -> stringResource(selectedStar.stringRes)
            }

            val repeatText = when (selectedMaxRepeat) {
                99 -> "∞"
                else -> ""
            }


            listOf(npText, starText, repeatText)
                .filter { it.isNotEmpty() }
                .joinToString(" / ")
        }
    }

    val headlineText = when (selectedSpam) {
        SpamEnum.None -> stringResource(R.string.p_spam_headline_none)
        SpamEnum.Spam -> stringResource(R.string.p_spam_headline_spam)
        SpamEnum.Danger -> stringResource(R.string.p_spam_headline_danger)
    }

    ListItem(
        headlineContent = { Text(headlineText) },
        supportingContent = {
            if (supportingText.isNotEmpty()) {
                Text(supportingText)
            }
        },
        modifier = modifier.clickable { dialog.show() },
        colors = FGAListItemColors()
    )
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
        entries = SpamEnum.entries.associateWith { stringResource(it.stringRes) },
        title = stringResource(R.string.spam)
    )

    ListItem(
        headlineContent = { Text(stringResource(R.string.spam)) },
        supportingContent = { Text(stringResource(selected.stringRes)) },
        modifier = modifier
            .clickable { dialog.show() },
        colors = FGAListItemColors()
    )
}

@Composable
private fun SelectTarget(
    skill: Skill,
    targets: List<ServantTarget>,
    onSelectChange: (List<ServantTarget>) -> Unit,
    onOpenTargetSelector: (TargetPickerRequest) -> Unit,
    modifier: Modifier = Modifier
) {

    ListItem(
        headlineContent = { Text(stringResource(R.string.spam_target)) },
        supportingContent = { Text(targetsToLabel(targets)) },
        modifier = modifier.clickable {
            onOpenTargetSelector(
                TargetPickerRequest(
                    skill = skill,
                    onConfirm = onSelectChange
                )
            )
        },
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

@Composable
private fun TargetSelectorOverlay(
    skill: Skill,
    onConfirm: (List<ServantTarget>) -> Unit,
    onDismiss: () -> Unit
) {
    var currentNav by remember { mutableStateOf<SkillMakerNav>(SkillMakerNav.SkillTarget(skill)) }
    val confirm by rememberUpdatedState(onConfirm)

    BackHandler {
        onDismiss()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {

        Crossfade(
            currentNav,
            animationSpec = spring()
        ) { nav: SkillMakerNav ->
            when (nav) {
                is SkillMakerNav.SkillTarget -> SkillMakerTarget(
                    slot = nav.skill.slot(),
                    onSkillTarget = {
                        confirm(listOfNotNull(it))
                        onDismiss()
                    },
                    onNpType2 = { currentNav = SkillMakerNav.ChangeNpType2(nav.skill) },
                    onNpType3 = { currentNav = SkillMakerNav.ChangeNpType3(nav.skill) },
                    onChoice2 = { currentNav = SkillMakerNav.Choice2(nav.skill, it) },
                    onChoice3 = { currentNav = SkillMakerNav.Choice3(nav.skill, it) },
                    onTransform = {
                        confirm(listOf(ServantTarget.Transform))
                        onDismiss()
                    }
                )

                is SkillMakerNav.Choice2 -> SkillMakerChoice2(
                    slot = nav.slot,
                    onOption1 = {
                        confirm(listOf(ServantTarget.SpecialTarget.Choice2OptionA))
                        onDismiss()
                    },
                    onOption2 = {
                        confirm(listOf(ServantTarget.SpecialTarget.Choice2OptionB))
                        onDismiss()
                    },
                    goToTarget = nav.skill in Skill.Servant.skill2,
                    onTarget = { firstTarget ->
                        currentNav = SkillMakerNav.Choice2Target(nav.skill, firstTarget)
                    }
                )

                is SkillMakerNav.Choice2Target -> SkillMakerChoice2Target(
                    onSkillTarget = { secondTarget ->
                        confirm(listOf(nav.firstTarget, secondTarget))
                        onDismiss()
                    }
                )

                is SkillMakerNav.Choice3 -> SkillMakerChoice3(
                    slot = nav.slot,
                    onSkillTarget = { servantTarget ->
                        confirm(listOf(servantTarget))
                        onDismiss()
                    }
                )

                is SkillMakerNav.ChangeNpType2 -> SkillMakerChangeNpType2(
                    onTargetLeft = {
                        confirm(listOf(ServantTarget.Left))
                        onDismiss()
                    },
                    onTargetRight = {
                        confirm(listOf(ServantTarget.Right))
                        onDismiss()
                    }
                )

                is SkillMakerNav.ChangeNpType3 -> SkillMakerChangeNpType3(
                    onSkillTarget = { servantTarget ->
                        confirm(listOf(servantTarget))
                        onDismiss()
                    }
                )

                else -> {
                    throw IllegalStateException("nav = ${nav::class.simpleName}")
                }
            }
        }

    }
}

@Composable
private fun targetsToLabel(targets: List<ServantTarget>): String {

    if (targets.isEmpty()) return stringResource(R.string.spam_target_none)

    val servantPosition = targets.lastOrNull()?.let {
        when (it) {
            ServantTarget.A -> stringResource(R.string.spam_target_left)
            ServantTarget.B -> stringResource(R.string.spam_target_center)
            ServantTarget.C -> stringResource(R.string.spam_target_right)
            ServantTarget.Left -> stringResource(R.string.spam_target_left)
            ServantTarget.Right -> stringResource(R.string.spam_target_right)
            ServantTarget.Transform -> stringResource(R.string.spam_target_transform)
            else -> ""
        }
    } ?: ""

    val choiceLabel = targets.firstOrNull()?.let {
        when (it) {
            is ServantTarget.SpecialTarget.Choice2OptionA -> stringResource(R.string.spam_target_option_1)
            is ServantTarget.SpecialTarget.Choice2OptionB -> stringResource(R.string.spam_target_option_2)
            is ServantTarget.SpecialTarget.Choice3OptionA -> stringResource(R.string.spam_target_option_1)
            is ServantTarget.SpecialTarget.Choice3OptionB -> stringResource(R.string.spam_target_option_2)
            is ServantTarget.SpecialTarget.Choice3OptionC -> stringResource(R.string.spam_target_option_3)
            else -> ""
        }
    } ?: ""

    val arrow = if (targets.size > 1) " → " else " "

    return "$choiceLabel$arrow$servantPosition".trim()
}