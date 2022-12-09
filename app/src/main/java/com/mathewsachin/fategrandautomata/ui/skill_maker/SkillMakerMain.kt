package com.mathewsachin.fategrandautomata.ui.skill_maker

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.scripts.models.AutoSkillAction
import com.mathewsachin.fategrandautomata.scripts.models.Skill
import com.mathewsachin.fategrandautomata.ui.icon

@Composable
fun SkillMakerMain(
    vm: SkillMakerViewModel,
    onMasterSkills: () -> Unit,
    onAtk: () -> Unit,
    onSkill: (Skill.Servant) -> Unit,
    onClear: () -> Unit,
    onDone: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(vertical = 16.dp)
            .fillMaxSize()
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
        ) {
            val enemyTarget by vm.enemyTarget

            EnemyTarget(
                selected = enemyTarget,
                onSelectedChange = { vm.setEnemyTarget(it) }
            )

            val stage by vm.stage
            Text(stringResource(R.string.skill_maker_main_battle, stage))
        }

        SkillHistory(vm)

        Row(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row {
                    val currentIndex by vm.currentIndex

                    ButtonWithIcon(
                        text = R.string.skill_maker_main_undo,
                        icon = icon(R.drawable.ic_undo),
                        onClick = { vm.onUndo() },
                        enabled = currentIndex > 0,
                        modifier = Modifier.padding(end = 5.dp)
                    )

                    ButtonWithIcon(
                        text = R.string.skill_maker_main_clear,
                        icon = icon(R.drawable.ic_clear),
                        onClick = onClear,
                        enabled = vm.skillCommand.size > 1,
                        modifier = Modifier.padding(end = 5.dp)
                    )

                    ButtonWithIcon(
                        text = R.string.skill_maker_atk_done,
                        icon = icon(Icons.Default.Check),
                        onClick = onDone
                    )
                }

                Skills(onSkill = onSkill)
            }

            Column(
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.End,
                modifier = Modifier
                    .padding(start = 16.dp)
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

// TODO: Scroll to latest item in History when new added?
@Composable
fun SkillHistory(vm: SkillMakerViewModel) {
    val currentIndex by vm.currentIndex
    val skillCommand = vm.skillCommand

    LazyRow(
        contentPadding = PaddingValues(16.dp)
    ) {
        items(skillCommand.size) { index ->
            val item = skillCommand.getOrNull(index)
            val isSelected = index == currentIndex

            val shape =
                if (isSelected)
                    RoundedCornerShape(7.dp)
                else RectangleShape

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
                    .clickable { vm.setCurrentIndex(index) }
                    .padding(horizontal = 4.dp)
            ) {
                val text =
                    if (item is SkillMakerEntry.Start) ">"
                    else item.toString()

                Text(
                    text,
                    color = Color.White
                )
            }
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