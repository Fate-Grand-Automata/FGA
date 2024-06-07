package io.github.fate_grand_automata.ui.battle_config_item

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
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
fun OutOfCommandsExitScreen(
    modifier: Modifier = Modifier,
    config: BattleConfigCore
) {
    var exitOnOutOfCommands by config.exitOnOutOfCommands.remember()

    val dialog = FgaDialog()

    dialog.build(
        color = MaterialTheme.colorScheme.background
    ) {
        title(stringResource(R.string.p_exit_on_out_of_commands))

        message(text = stringResource(R.string.p_exit_on_out_of_commands_summary))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(
                horizontal = 4.dp
            )
        ) {
            Card(
                shape = RoundedCornerShape(25),
                colors = CardDefaults.cardColors(
                    containerColor =
                    if (exitOnOutOfCommands) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceVariant
                ),
                onClick = {
                    exitOnOutOfCommands = true
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
                    fontWeight = if (exitOnOutOfCommands) FontWeight.Bold else null
                )
            }
            Card(
                shape = RoundedCornerShape(25),
                colors = CardDefaults.cardColors(
                    containerColor =
                    if (!exitOnOutOfCommands) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceVariant
                ),
                onClick = {
                    exitOnOutOfCommands = false
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
                    fontWeight = if (!exitOnOutOfCommands) FontWeight.Bold else null
                )
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .clickable(
                onClick = { dialog.show() }
            )
            .padding(
                horizontal = 0.dp,
                vertical = 4.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.p_exit_on_out_of_commands).uppercase(),
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
            maxLines = 1
        )
        Text(
            text = when (exitOnOutOfCommands) {
                true -> stringResource(R.string.state_on).uppercase()
                false -> stringResource(R.string.state_off).uppercase()
            },
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}