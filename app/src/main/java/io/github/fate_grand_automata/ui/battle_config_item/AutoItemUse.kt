package io.github.fate_grand_automata.ui.battle_config_item

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import io.github.fate_grand_automata.ui.dialog.FgaDialog
import io.github.fate_grand_automata.ui.prefs.remember

@Composable
fun AutoItemUse(
    modifier: Modifier = Modifier,
    config: BattleConfigCore
) {
    var useStormPod by config.useStormPod.remember()
    var useTeapot by config.useTeapot.remember()

    val dialog = FgaDialog()

    dialog.build(
        color = MaterialTheme.colorScheme.background
    ) {
        var currentUseStormPod by remember(useStormPod) { mutableStateOf(useStormPod) }
        var currentUseTeapot by remember(useTeapot) { mutableStateOf(useTeapot) }

        Row {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .alignByBaseline()
            ) {
                title(stringResource(R.string.p_battle_config_auto_item_use))
            }

            TextButton(
                onClick = {
                    currentUseStormPod = config.useStormPod.defaultValue
                    currentUseTeapot = config.useTeapot.defaultValue
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

        message(text = stringResource(R.string.p_battle_config_auto_item_use_message))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            // Storm Pod toggle
            Text(
                text = stringResource(R.string.p_battle_config_storm_pod),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Card(
                    shape = RoundedCornerShape(25),
                    colors = CardDefaults.cardColors(
                        containerColor =
                        if (currentUseStormPod) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    onClick = { currentUseStormPod = true },
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 2.dp)
                ) {
                    Text(
                        text = stringResource(R.string.config_state_on).uppercase(),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        textAlign = TextAlign.Center,
                        fontWeight = if (currentUseStormPod) FontWeight.Bold else null
                    )
                }
                Card(
                    shape = RoundedCornerShape(25),
                    colors = CardDefaults.cardColors(
                        containerColor =
                        if (!currentUseStormPod) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    onClick = { currentUseStormPod = false },
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 2.dp)
                ) {
                    Text(
                        text = stringResource(R.string.config_state_off).uppercase(),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        textAlign = TextAlign.Center,
                        fontWeight = if (!currentUseStormPod) FontWeight.Bold else null
                    )
                }
            }

            // Teapot toggle
            Text(
                text = stringResource(R.string.p_battle_config_teapot),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Card(
                    shape = RoundedCornerShape(25),
                    colors = CardDefaults.cardColors(
                        containerColor =
                        if (currentUseTeapot) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    onClick = { currentUseTeapot = true },
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 2.dp)
                ) {
                    Text(
                        text = stringResource(R.string.config_state_on).uppercase(),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        textAlign = TextAlign.Center,
                        fontWeight = if (currentUseTeapot) FontWeight.Bold else null
                    )
                }
                Card(
                    shape = RoundedCornerShape(25),
                    colors = CardDefaults.cardColors(
                        containerColor =
                        if (!currentUseTeapot) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    onClick = { currentUseTeapot = false },
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 2.dp)
                ) {
                    Text(
                        text = stringResource(R.string.config_state_off).uppercase(),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        textAlign = TextAlign.Center,
                        fontWeight = if (!currentUseTeapot) FontWeight.Bold else null
                    )
                }
            }

            buttons(
                onSubmit = {
                    useStormPod = currentUseStormPod
                    useTeapot = currentUseTeapot
                },
                okLabel = stringResource(R.string.save),
            )
        }
    }

    ConfigSummaryCell(
        label = stringResource(R.string.p_battle_config_auto_item_use),
        onClick = { dialog.show() },
        modifier = modifier
    ) {
        ConfigSummaryValue(
            when {
                useStormPod && useTeapot -> stringResource(R.string.config_state_on)
                useStormPod -> stringResource(R.string.p_battle_config_storm_pod)
                useTeapot -> stringResource(R.string.p_battle_config_teapot)
                else -> stringResource(R.string.config_state_off)
            }
        )
    }
}
