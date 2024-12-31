package io.github.fate_grand_automata.ui.battle_config_item

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.fate_grand_automata.R
import io.github.fate_grand_automata.prefs.core.BattleConfigCore
import io.github.fate_grand_automata.ui.Stepper
import io.github.fate_grand_automata.ui.dialog.FgaDialog
import io.github.fate_grand_automata.ui.prefs.remember

@Composable
fun RaidDelay(
    modifier: Modifier = Modifier,
    config: BattleConfigCore
) {
    var addRaidTurnDelay by config.addRaidTurnDelay.remember()

    var raidTurnDelaySeconds by config.raidTurnDelaySeconds.remember()

    val dialog = FgaDialog()

    dialog.build(
        color = MaterialTheme.colorScheme.background
    ) {
        var currentAddRaidTurnDelay by remember(addRaidTurnDelay) { mutableStateOf(addRaidTurnDelay) }
        var currentRaidTurnDelaySeconds by remember(raidTurnDelaySeconds) { mutableStateOf(raidTurnDelaySeconds) }

        Row {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .alignByBaseline()
            ) {
                title(stringResource(R.string.p_battle_config_raid_delay))
            }

            TextButton(
                onClick = {
                    currentAddRaidTurnDelay = config.addRaidTurnDelay.defaultValue
                    currentRaidTurnDelaySeconds = config.raidTurnDelaySeconds.defaultValue
                },
                modifier = Modifier
                    .padding(16.dp, 5.dp)
                    .alignByBaseline(),
            ) {
                Text(
                    stringResource(id = R.string.reset).uppercase()
                )
            }
        }

        message(text = stringResource(R.string.p_battle_config_raid_delay_message))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Card(
                    shape = RoundedCornerShape(25),
                    colors = CardDefaults.cardColors(
                        containerColor =
                        if (currentAddRaidTurnDelay) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    onClick = {
                        currentAddRaidTurnDelay = true
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
                        fontWeight = if (currentAddRaidTurnDelay) FontWeight.Bold else null
                    )
                }
                Card(
                    shape = RoundedCornerShape(25),
                    colors = CardDefaults.cardColors(
                        containerColor =
                        if (!currentAddRaidTurnDelay) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    onClick = {
                        currentAddRaidTurnDelay = false
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
                        fontWeight = if (!currentAddRaidTurnDelay) FontWeight.Bold else null
                    )
                }
            }

            if (currentAddRaidTurnDelay) {
                Stepper(
                    value = currentRaidTurnDelaySeconds,
                    onValueChange = {
                        currentRaidTurnDelaySeconds = it
                    },
                    valueRange = 1..10,
                    valueRepresentation = {
                        "${it}s"
                    }
                )
            }
            buttons(
                onSubmit = {
                    addRaidTurnDelay = currentAddRaidTurnDelay
                    raidTurnDelaySeconds = currentRaidTurnDelaySeconds
                },
                okLabel = stringResource(R.string.save),
            )
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
            stringResource(R.string.p_battle_config_raid).uppercase(),
            style = MaterialTheme.typography.bodySmall
        )

        Text(
            text = when (addRaidTurnDelay) {
                true -> "${raidTurnDelaySeconds}s"
                false -> stringResource(R.string.state_off).uppercase()
            },
            style = MaterialTheme.typography.bodySmall
        )
    }
}