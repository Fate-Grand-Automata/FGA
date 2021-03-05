package com.mathewsachin.fategrandautomata.ui.skill_maker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.RadioButton
import androidx.compose.material.Text
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
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.scripts.models.AutoSkillAction
import com.mathewsachin.fategrandautomata.scripts.models.Skill
import com.mathewsachin.fategrandautomata.util.nav
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SkillMakerMainFragment : Fragment() {
    val viewModel: SkillMakerViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        skillMakerScaffold {
            SkillMakerMain(
                vm = viewModel,
                onMasterSkills = { goToMasterSkills() },
                onAtk = { goToAtk() },
                onSkill = { onSkill(it.autoSkillCode) },
                onClear = { onClear() },
                onDone = { onDone() }
            )
        }

    // TODO: Scroll to latest item in History when new added?

    fun onClear() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.skill_maker_confirm_clear_title)
            .setMessage(R.string.skill_maker_confirm_clear_message)
            .setNegativeButton(android.R.string.no, null)
            .setPositiveButton(android.R.string.yes) { _, _ -> viewModel.clearAll() }
            .show()
    }

    fun goToAtk() {
        val action = SkillMakerMainFragmentDirections
            .actionSkillMakerMainFragmentToSkillMakerAtkFragment()

        nav(action)
    }

    fun goToMasterSkills() {
        val action = SkillMakerMainFragmentDirections
            .actionSkillMakerMainFragmentToSkillMakerMasterSkillsFragment()

        nav(action)
    }

    fun onSkill(SkillCode: Char) {
        viewModel.initSkill(SkillCode)

        val showSpaceIshtar = SkillCode in listOf('b', 'e', 'h')
        val showEmiya = SkillCode in listOf('c', 'f', 'i')

        val action = SkillMakerMainFragmentDirections
            .actionSkillMakerMainFragmentToSkillMakerTargetFragment(
                showSpaceIshtar,
                showEmiya
            )

        nav(action)
    }

    fun onDone() {
        viewModel.battleConfig.skillCommand = viewModel.finish()
        activity?.finish()
    }
}

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
            .padding(16.dp)
            .fillMaxSize()
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
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

        Row(modifier = Modifier.weight(1f)) {
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
                        icon = R.drawable.ic_undo,
                        onClick = { vm.onUndo() },
                        enabled = currentIndex > 0,
                        modifier = Modifier.padding(end = 5.dp)
                    )

                    ButtonWithIcon(
                        text = R.string.skill_maker_main_clear,
                        icon = R.drawable.ic_clear,
                        onClick = onClear,
                        enabled = currentIndex > 0,
                        modifier = Modifier.padding(end = 5.dp)
                    )

                    ButtonWithIcon(
                        text = R.string.skill_maker_atk_done,
                        icon = R.drawable.ic_check,
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
                    .fillMaxHeight()
            ) {
                Button(
                    colors = ButtonDefaults.buttonColors(backgroundColor = colorResource(R.color.colorMasterSkill)),
                    onClick = onMasterSkills
                ) {
                    Text(
                        stringResource(R.string.skill_maker_main_master_skills),
                        textAlign = TextAlign.Center,
                        color = Color.White
                    )
                }

                Button(
                    shape = CircleShape,
                    onClick = onAtk,
                    colors = ButtonDefaults.buttonColors(backgroundColor = colorResource(R.color.colorAccent)),
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .size(120.dp)
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
fun SkillButtons(
    list: List<Skill.Servant>,
    color: Color,
    onSkill: (Skill.Servant) -> Unit
) {
    Row {
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
    Column {
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
                        horizontalAlignment = Alignment.CenterHorizontally
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
}

val SkillMakerEntry?.colorRes: Int get() {
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

    LazyRow(
        modifier = Modifier.padding(vertical = 16.dp)
    ) {
        // TODO: Compose is bugged on item removal in beta1: https://issuetracker.google.com/issues/163069767
        //itemsIndexed(skillCommand) { index, item ->
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
                    .clickable(onClick = onClick)
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