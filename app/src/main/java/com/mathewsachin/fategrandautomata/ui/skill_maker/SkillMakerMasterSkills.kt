package com.mathewsachin.fategrandautomata.ui.skill_maker

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.scripts.models.Skill

@Composable
fun SkillMakerMasterSkills(
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
                    colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.colorServant3))
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
fun RowScope.SkillButton(
    skill: Skill,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        color = color,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .padding(5.dp)
            .sizeIn(maxWidth = 45.dp)
            .aspectRatio(1f)
            .fillMaxSize()
            .weight(1f, false),
        onClick = onClick
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
    SkillMakerMasterSkills(onMasterSkill = { }, onOrderChange = { })
}