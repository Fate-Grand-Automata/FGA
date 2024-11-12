package io.github.fate_grand_automata.ui.skill_maker

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.fate_grand_automata.scripts.models.Skill

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