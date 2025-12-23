package io.github.fate_grand_automata.ui.battle_config_item

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.fate_grand_automata.R
import io.github.fate_grand_automata.prefs.core.BattleConfigCore
import io.github.fate_grand_automata.ui.dialog.FgaDialog
import io.github.fate_grand_automata.ui.prefs.remember

@Composable
fun StoryIntro(
    modifier: Modifier = Modifier,
    config: BattleConfigCore
){
    var storyIntroSkip by config.storyIntroSkip.remember()

    val dialog = FgaDialog()

    dialog.build(
        color = MaterialTheme.colorScheme.background
    ) {
        title(stringResource(R.string.p_battle_config_skip_intro))

        message(text = stringResource(R.string.p_battle_config_skip_intro_message))

        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Card(
                shape = RoundedCornerShape(25),
                colors = CardDefaults.cardColors(
                    containerColor =
                    if (storyIntroSkip) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceVariant
                ),
                onClick = {
                    storyIntroSkip = true
                    dialog.hide()
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 2.dp)
            ) {
                Text(
                    text = stringResource(R.string.state_on).uppercase(),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    textAlign = TextAlign.Center,
                    fontWeight = if (storyIntroSkip) FontWeight.Bold else null
                )
            }
            Card(
                shape = RoundedCornerShape(25),
                colors = CardDefaults.cardColors(
                    containerColor =
                    if (!storyIntroSkip) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceVariant
                ),
                onClick = {
                    storyIntroSkip = false
                    dialog.hide()
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 2.dp)
            ) {
                Text(
                    text = stringResource(R.string.state_off).uppercase(),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    textAlign = TextAlign.Center,
                    fontWeight = if (!storyIntroSkip) FontWeight.Bold else null
                )
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .clickable(
                onClick = { dialog.show() }
            )
            .padding(16.dp, 5.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            stringResource(R.string.p_battle_config_skip_intro).uppercase(),
            style = MaterialTheme.typography.bodySmall
        )
        Text(
            text = when (storyIntroSkip) {
                true -> stringResource(R.string.state_on).uppercase()
                false -> stringResource(R.string.state_off).uppercase()
            },
            style = MaterialTheme.typography.bodySmall
        )
    }
}