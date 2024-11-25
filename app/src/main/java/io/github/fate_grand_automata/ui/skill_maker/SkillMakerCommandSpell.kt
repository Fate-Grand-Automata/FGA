package io.github.fate_grand_automata.ui.skill_maker

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.text.style.TextAlign
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
import io.github.fate_grand_automata.ui.skill_maker.special.TargetButton
import io.github.fate_grand_automata.util.stringRes

@Composable
fun SkillMakerCommandSpells(
    remaining: Int,
    onCommandSpell: (Skill.CommandSpell) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(16.dp)
    ) {
        FGATitle(
            stringResource(R.string.skill_maker_command_spell, remaining)
        )

        if (remaining > 0) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Skill.CommandSpell.list.forEach {
                    TargetButton(
                        onClick = {
                            onCommandSpell(it)
                        },
                        color = colorResource(R.color.colorCommandSpell),
                        text = stringResource(it.stringRes)
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Text(
                    stringResource(R.string.skill_maker_command_spell_warning),
                    modifier = Modifier
                        .align(Alignment.Center),
                )
                Button(
                    onClick = onBack,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                ) {
                    Text(stringResource(R.string.skill_maker_target_none))
                }
            }
        }
    }
}


@Preview(name = "Light Mode", widthDp = 600, heightDp = 300)
@Preview(name = "Dark Mode", widthDp = 600, heightDp = 300, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun TestCommandSpell() {
    FGATheme {
        SkillMakerCommandSpells(
            remaining = 3,
            onCommandSpell = { },
            onBack = { }
        )
    }
}

@Preview(name = "Light Mode", widthDp = 600, heightDp = 300)
@Preview(name = "Dark Mode", widthDp = 600, heightDp = 300, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun TestCommandSpellZero() {
    FGATheme {
        SkillMakerCommandSpells(
            remaining = 0,
            onCommandSpell = { },
            onBack = { }
        )
    }
}