package io.github.fate_grand_automata.ui.skill_maker

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.fate_grand_automata.R
import io.github.fate_grand_automata.scripts.models.ServantTarget
import io.github.fate_grand_automata.ui.FGATheme
import io.github.fate_grand_automata.ui.FGATitle

@Composable
fun SkillMakerTarget(
    onSkillTarget: (ServantTarget?) -> Unit,
    showEmiya: Boolean,
    onEmiya: () -> Unit,
    showSpaceIshtar: Boolean,
    onSpaceIshtar: () -> Unit,
    showKukulkan: Boolean,
    onKukulkan: () -> Unit,
    showMelusine: Boolean,
    onMelusine: () -> Unit,
    showSoujuurou: Boolean,
    onSoujuurou: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(16.dp)
    ) {
        FGATitle(
            stringResource(R.string.skill_maker_target_header)
        )

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            TargetButton(
                onClick = { onSkillTarget(ServantTarget.A) },
                color = colorResource(R.color.colorServant1),
                text = stringResource(R.string.skill_maker_target_servant, 1)
            )

            TargetButton(
                onClick = { onSkillTarget(ServantTarget.B) },
                color = colorResource(R.color.colorServant2),
                text = stringResource(R.string.skill_maker_target_servant, 2)
            )

            TargetButton(
                onClick = { onSkillTarget(ServantTarget.C) },
                color = colorResource(R.color.colorServant3),
                text = stringResource(R.string.skill_maker_target_servant, 3)
            )
        }

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            if (showKukulkan) {
                Button(onClick = onKukulkan) {
                    Text(stringResource(R.string.skill_maker_kukulkan))
                }
            }

            if (
                showEmiya) {
                Button(onClick = onEmiya) {
                    Text(stringResource(R.string.skill_maker_emiya))
                }
            }

            if (showSpaceIshtar) {
                Button(onClick = onSpaceIshtar) {
                    Text(stringResource(R.string.skill_maker_space_ishtar))
                }
            }

            if (showMelusine) {
                Button(onClick = onMelusine) {
                    Text(stringResource(R.string.skill_maker_melusine))
                }
            }
            if (showSoujuurou) {
                Button(onClick = onSoujuurou) {
                    Text(stringResource(R.string.skill_maker_soujuurou))
                }
            }

            Button(onClick = { onSkillTarget(null) }) {
                Text(stringResource(R.string.skill_maker_target_none))
            }
        }
    }
}

@Composable
@Preview(name = "Light Mode", widthDp = 600, heightDp = 300)
@Preview(name = "Dark Mode", widthDp = 600, heightDp = 300, uiMode = Configuration.UI_MODE_NIGHT_YES)
fun TestSkillMakerTargetEmiya() = TestSkillMaker(showEmiya = true)

@Composable
@Preview(name = "Light Mode", widthDp = 600, heightDp = 300)
@Preview(name = "Dark Mode", widthDp = 600, heightDp = 300, uiMode = Configuration.UI_MODE_NIGHT_YES)
fun TestSkillMakerTargetIshtar() = TestSkillMaker(showSpaceIshtar = true)

@Composable
@Preview(name = "Light Mode", widthDp = 600, heightDp = 300)
@Preview(name = "Dark Mode", widthDp = 600, heightDp = 300, uiMode = Configuration.UI_MODE_NIGHT_YES)
fun TestSkillMakerOnlyKukulkan() = TestSkillMaker()

@Composable
@Preview(name = "Light Mode", widthDp = 600, heightDp = 300)
@Preview(name = "Dark Mode", widthDp = 600, heightDp = 300, uiMode = Configuration.UI_MODE_NIGHT_YES)
fun TestSkillMakerTargetSoujuurou() = TestSkillMaker(showSoujuurou = true)

@Composable
private fun TestSkillMaker(
    showEmiya: Boolean = false,
    showKukulkan: Boolean = false, 
    showSpaceIshtar: Boolean = false,
    showMelusine: Boolean = showEmiya,
    showSoujuurou: Boolean = false
) {
    FGATheme {
        SkillMakerTarget(
            onSkillTarget = {},
            showEmiya = showEmiya,
            onEmiya = {},
            showSpaceIshtar = showSpaceIshtar,
            onSpaceIshtar = {},
            showKukulkan = showKukulkan,
            onKukulkan = {},
            showMelusine = showMelusine,
            onMelusine = {},
            showSoujuurou = showSoujuurou,
            onSoujuurou = {}
        )
    }
}