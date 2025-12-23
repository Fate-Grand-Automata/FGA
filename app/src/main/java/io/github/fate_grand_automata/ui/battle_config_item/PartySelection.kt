package io.github.fate_grand_automata.ui.battle_config_item

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults.cardColors
import androidx.compose.material3.CardDefaults.cardElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.fate_grand_automata.R
import io.github.fate_grand_automata.prefs.core.BattleConfigCore
import io.github.fate_grand_automata.scripts.enums.GameServer
import io.github.fate_grand_automata.ui.dialog.FgaDialog
import io.github.fate_grand_automata.ui.prefs.remember

@Composable
fun PartySelection(config: BattleConfigCore) {
    var party by config.party.remember()
    val server by config.server.remember()

    val isSelectionExtended by remember {
        derivedStateOf {
            when (server.asGameServer()) {
                null, is GameServer.Jp, GameServer.Cn, is GameServer.En -> true
                else -> false
            }
        }
    }
    LaunchedEffect(key1 = server) {
        if (!isSelectionExtended && party > 9) {
            party = 9
        }
    }

    val dialog = FgaDialog()

    dialog.build(
        color = MaterialTheme.colorScheme.background
    ) {
        title(stringResource(R.string.p_battle_config_party))

        PartySelectionDialogContent(
            isSelectionExtended = isSelectionExtended,
            selected = party,
            onSelectedChange = {
                party = it
                dialog.hide()
            }
        )

        buttons(
            showCancel = false,
            // TODO: Localize
            okLabel = "CLEAR",
            onSubmit = { party = -1 }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .clickable(onClick = { dialog.show() })
            .padding(16.dp, 5.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            stringResource(R.string.p_battle_config_party).uppercase(),
            style = MaterialTheme.typography.bodySmall
        )

        Text(
            if (party == -1) "--" else (party + 1).toString(),
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun PartySelectionItem(
    text: String,
    isSelected: Boolean,
    onSelectedChange: () -> Unit
) {
    val background = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant
    val foreground = if (isSelected) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant

    Card(
        elevation = cardElevation(10.dp),
        shape = CircleShape,
        colors = cardColors(
            containerColor = background,
            contentColor = foreground
        ),
        modifier = Modifier
            .padding(5.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(40.dp)
                .clickable(onClick = onSelectedChange)
        ) {
            Text(text)
        }
    }
}

@Composable
fun PartySelectionDialogContent(
    isSelectionExtended: Boolean = false,
    selected: Int,
    onSelectedChange: (Int) -> Unit
) {
    Column {
        val partyRange = if (isSelectionExtended) 0..14 else 0..9

        partyRange
            .chunked(5)
            .forEach { chunk ->
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    chunk.forEach {
                        PartySelectionItem(
                            text = "${it + 1}",
                            isSelected = selected == it,
                            onSelectedChange = { onSelectedChange(it) }
                        )
                    }
                }
            }
    }
}