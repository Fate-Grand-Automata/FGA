package io.github.fate_grand_automata.ui.skill_maker

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import io.github.fate_grand_automata.R
import io.github.fate_grand_automata.scripts.models.AutoSkillAction
import io.github.fate_grand_automata.scripts.models.EnemyFormation
import io.github.fate_grand_automata.scripts.models.Skill
import io.github.fate_grand_automata.ui.icon
import io.github.fate_grand_automata.util.stringRes

@Composable
fun SkillMakerMain(
    vm: SkillMakerViewModel,
    onMasterSkills: () -> Unit,
    onAtk: () -> Unit,
    onSkill: (Skill.Servant) -> Unit,
    onClear: () -> Unit,
    onDone: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
    ) {
        val enemyFormation by vm.enemyFormation

        val currentIndex by vm.currentIndex

        NavigationRail(
            header = {
                FloatingActionButton(
                    onClick = onDone,
                    shape = RoundedCornerShape(12),
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Check"
                    )
                }

                Button(
                    onClick = {
                        vm.changeEnemyFormation()
                    },
                    shape = RoundedCornerShape(12),
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Text(
                        text = stringResource(id = enemyFormation.stringRes),
                        textAlign = TextAlign.Center
                    )
                }

                IconButton(
                    onClick = onClear,
                    enabled = vm.skillCommand.size > 1,
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Icon(
                        icon(R.drawable.ic_clear).asPainter(),
                        contentDescription = "Clear"
                    )
                }

                IconButton(
                    onClick = { vm.onDeleteSelected() },
                    enabled = currentIndex > 0,
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Icon(
                        icon(Icons.Default.Delete).asPainter(),
                        contentDescription = "Delete"
                    )
                }
            }
        ) {

        }

        Row(
            modifier = Modifier
                .padding(vertical = 16.dp)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                val enemyTarget by vm.enemyTarget

                ChooseEnemyTarget(
                    modifier = Modifier.weight(1f),
                    enemyFormation = enemyFormation,
                    selected = enemyTarget,
                    onSelectedChange = { target ->
                        if (target == enemyTarget) {
                            vm.deleteIfLastActionIsTarget(target)
                        } else {
                            vm.setEnemyTarget(target)
                        }
                    }
                )
                SkillHistory(vm)

                Spacer(
                    modifier = Modifier
                        .weight(1f)
                )
                Skills(onSkill = onSkill)

            }

            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    val wave by vm.wave
                    Text(stringResource(R.string.skill_maker_main_wave, wave))

                    val turn by vm.turn
                    Text(stringResource(R.string.skill_maker_main_turn, turn))
                }
                Column(
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier
                        .padding(start = 16.dp, top = 16.dp)
                        .width(IntrinsicSize.Max)
                        .fillMaxHeight()
                ) {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.colorMasterSkill)),
                        onClick = onMasterSkills
                    ) {
                        Text(
                            stringResource(R.string.skill_maker_main_master_skills),
                            textAlign = TextAlign.Center,
                            color = Color.White
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    Button(
                        shape = CircleShape,
                        onClick = onAtk,
                        colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.colorAccent)),
                        modifier = Modifier
                            .fillMaxSize()
                            .aspectRatio(1f)

                    ) {
                        Text(
                            stringResource(R.string.skill_maker_main_attack),
                            color = Color.White
                        )
                    }
                }
            }

        }
    }
}

@Composable
fun ColumnScope.SkillButtons(
    list: List<Skill.Servant>,
    color: Color,
    onSkill: (Skill.Servant) -> Unit
) {
    Row(
        modifier = Modifier.weight(1f, false)
    ) {
        list.map { skill ->
            SkillButton(
                skill = skill,
                color = color,
                onClick = { onSkill(skill) }
            )
        }
    }
}

@Composable
fun Skills(onSkill: (Skill.Servant) -> Unit) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Skill.Servant.list.chunked(3)
            .mapIndexed { index, list ->
                val color = when (index) {
                    0 -> R.color.colorServant1
                    1 -> R.color.colorServant2
                    2 -> R.color.colorServant3
                    else -> R.color.colorAccent
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f, false)
                ) {
                    SkillButtons(
                        list = list,
                        color = colorResource(color),
                        onSkill = onSkill
                    )

                    Text(stringResource(R.string.skill_maker_main_servant, index + 1))
                }
            }
    }
}

val SkillMakerEntry?.colorRes: Int
    get() {
        val defaultColor = R.color.colorAccent

        return when (this) {
            is SkillMakerEntry.Next -> R.color.colorStageChange

            is SkillMakerEntry.Action -> when (this.action) {
                // Master Skill
                is AutoSkillAction.MasterSkill -> R.color.colorMasterSkill

                // Enemy Target
                is AutoSkillAction.TargetEnemy -> R.color.colorEnemyTarget

                // Servants
                is AutoSkillAction.ServantSkill -> when (this.action.skill.autoSkillCode) {
                    'a', 'b', 'c' -> R.color.colorServant1
                    'd', 'e', 'f' -> R.color.colorServant2
                    'g', 'h', 'i' -> R.color.colorServant3
                    else -> defaultColor
                }

                else -> defaultColor
            }

            else -> defaultColor
        }
    }

@Composable
fun SkillHistory(vm: SkillMakerViewModel) {
    val currentIndex by vm.currentIndex
    val skillCommand = vm.skillCommand

    val state = rememberLazyListState()

    LaunchedEffect(key1 = skillCommand.size) {
        snapshotFlow { skillCommand.size }
            .collect {
                if (skillCommand.size > 0) {
                    state.animateScrollToItem(skillCommand.lastIndex)
                }
            }
    }

    LazyRow(
        contentPadding = PaddingValues(16.dp),
        state = state,
    ) {
        (0..skillCommand.lastIndex).map { index ->
            val item = skillCommand.getOrNull(index)
            val isSelected = index == currentIndex
            val shape = when (isSelected) {
                true -> RoundedCornerShape(7.dp)
                false -> RectangleShape
            }
            val text = if (item is SkillMakerEntry.Start) ">"
            else item.toString()

            if (item is SkillMakerEntry.Start) {
                stickyHeader {
                    HistoryItem(
                        item = item,
                        isSelected = isSelected,
                        shape = shape,
                        text = text,
                        onClick = { vm.setCurrentIndex(index) }
                    )
                }
            } else {
                item {
                    HistoryItem(
                        item = item,
                        isSelected = isSelected,
                        shape = shape,
                        text = text,
                        onClick = { vm.setCurrentIndex(index) }
                    )
                }
            }
        }
    }
}

@Composable
fun HistoryItem(
    item: SkillMakerEntry?,
    isSelected: Boolean,
    shape: Shape,
    onClick: () -> Unit,
    text: String,
) {
    Box(
        modifier = Modifier
            .padding(end = 5.dp)
            .let {
                if (isSelected) {
                    it.border(
                        2.dp,
                        color = colorResource(android.R.color.darker_gray),
                        shape = shape
                    )
                } else it
            }
            .background(colorResource(item.colorRes), shape)
            .clickable(
                onClick = onClick
            )
            .padding(horizontal = 4.dp)
    ) {

        Text(
            text,
            color = Color.White
        )
    }
}

@Composable
fun ChooseEnemyTarget(
    modifier: Modifier = Modifier,
    enemyFormation: EnemyFormation,
    selected: Int?,
    onSelectedChange: (Int) -> Unit
) {
    val slideIn = slideInHorizontally(initialOffsetX = { width -> width }) +
            expandHorizontally(
                expandFrom = Alignment.End,
                initialWidth = { w -> w }
            )
    val slideOut = slideOutHorizontally(
        targetOffsetX = { width -> -width }) +
            shrinkHorizontally(
                shrinkTowards = Alignment.Start,
                animationSpec = spring(
                    stiffness = Spring.StiffnessMediumLow,
                    visibilityThreshold = IntSize.VisibilityThreshold
                )
            )
    AnimatedContent(
        targetState = enemyFormation,
        label = "Enemy Formation Change",
        transitionSpec = {
            slideIn togetherWith (slideOut)
        },
        modifier = modifier
    ) { formation ->
        when (formation) {
            EnemyFormation.THREE -> EnemyTarget(selected, onSelectedChange)
            EnemyFormation.SIX -> SixEnemyTarget(selected, onSelectedChange)
        }
    }
}

@Composable
fun EnemyTarget(
    selected: Int?,
    onSelectedChange: (Int) -> Unit
) {
    Row {
        (1..3).map {
            val isSelected = selected == it
            val onClick = { onSelectedChange(it) }

            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 10.dp)
                    .clickable(onClick = onClick),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = isSelected,
                    onClick = onClick
                )

                Text(
                    stringResource(R.string.skill_maker_main_enemy, it),
                    modifier = Modifier
                        .padding(start = 5.dp)
                )
            }
        }
    }
}

@Composable
fun SixEnemyTarget(
    selected: Int?,
    onSelectedChange: (Int) -> Unit
) {
    FlowRow(
        maxItemsInEachRow = 3,
        modifier = Modifier
            .fillMaxWidth()
    ) {
        (4..9).map { target ->
            val isSelected = selected == target
            val onClick = { onSelectedChange(target) }

            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 10.dp)
                    .clickable(onClick = onClick),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = isSelected,
                    onClick = onClick
                )

                Text(
                    stringResource(R.string.skill_maker_main_enemy, target),
                    modifier = Modifier
                        .padding(start = 5.dp)
                )
            }
        }
    }
}