package com.mathewsachin.fategrandautomata.ui.skill_maker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.scripts.models.Skill
import com.mathewsachin.fategrandautomata.ui.prefs.compose.FgaTheme
import com.mathewsachin.fategrandautomata.util.nav
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SkillMakerMasterSkillsFragment : Fragment() {
    val vm: SkillMakerViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        skillMakerScaffold {
            MasterSkills(
                onMasterSkill = { onSkill(it) },
                onOrderChange = { goToOrderChange() }
            )
        }

    fun onSkill(skill: Skill.Master) {
        vm.initSkill(skill.autoSkillCode)

        val action = SkillMakerMasterSkillsFragmentDirections
            .actionSkillMakerMasterSkillsFragmentToSkillMakerTargetFragment()

        nav(action)
    }

    fun goToOrderChange() {
        val action = SkillMakerMasterSkillsFragmentDirections
            .actionSkillMakerMasterSkillsFragmentToSkillMakerOrderChangeFragment()

        nav(action)
    }
}

@Composable
fun MasterSkills(
    onMasterSkill: (Skill.Master) -> Unit,
    onOrderChange: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(16.dp)
    ) {
        Text(
            stringResource(R.string.skill_maker_master_skills_header),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
        )

        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = onOrderChange,
                    colors = ButtonDefaults.buttonColors(backgroundColor = colorResource(R.color.colorServant3))
                ) {
                    Text(
                        stringResource(R.string.skill_maker_master_skills_order_change),
                        color = Color.White
                    )
                }
            }

            Row {
                Skill.Master.list.forEach {
                    SkillButton(
                        skill = it,
                        color = colorResource(R.color.colorMasterSkill)
                    ) {
                        onMasterSkill(it)
                    }
                }
            }
        }
    }
}

@Composable
fun SkillButton(
    skill: Skill,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        color = color,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .padding(5.dp)
            .clickable(onClick = onClick)
            .size(40.dp)
    ) {
        Box {
            Text(
                skill.autoSkillCode.toString(),
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@Preview(widthDp = 600, heightDp = 300)
@Composable
fun TestMasterSkills() {
    MasterSkills(onMasterSkill = { }, onOrderChange = { })
}