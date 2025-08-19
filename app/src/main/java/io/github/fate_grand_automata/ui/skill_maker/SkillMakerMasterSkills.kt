package io.github.fate_grand_automata.ui.skill_maker

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.fate_grand_automata.R
import io.github.fate_grand_automata.scripts.models.Skill
import io.github.fate_grand_automata.ui.FGATheme
import io.github.fate_grand_automata.ui.FGATitle

@Composable
fun SkillMakerMasterSkills(
    onMasterSkill: (Skill.Master) -> Unit,
    onMasterSkillNoTarget: (Skill.Master) -> Unit,
    onOrderChange: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(16.dp),
    ) {
        FGATitle(
            stringResource(R.string.skill_maker_master_skills_header),
        )

        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Button(
                    onClick = onOrderChange,
                    colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.colorServant3)),
                ) {
                    Text(
                        stringResource(R.string.skill_maker_master_skills_order_change),
                        color = Color.White,
                    )
                }
            }

            Row {
                Skill.Master.list.forEach {
                    SkillButton(
                        skill = it,
                        color = colorResource(R.color.colorMasterSkill),
                        onClick = { onMasterSkill(it) },
                        onDoubleClick = { onMasterSkillNoTarget(it) },
                    )
                }
            }
        }
    }
}

@Preview(name = "Light Mode", widthDp = 600, heightDp = 300)
@Preview(name = "Dark Mode", widthDp = 600, heightDp = 300, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun TestMasterSkills() {
    FGATheme {
        SkillMakerMasterSkills(
            onMasterSkill = { },
            onMasterSkillNoTarget = { },
            onOrderChange = { },
        )
    }
}
