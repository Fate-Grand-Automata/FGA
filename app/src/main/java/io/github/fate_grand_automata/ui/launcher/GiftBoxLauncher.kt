package io.github.fate_grand_automata.ui.launcher

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.fate_grand_automata.R
import io.github.fate_grand_automata.scripts.prefs.IPreferences
import io.github.fate_grand_automata.ui.Stepper

@Composable
fun ColumnScope.GiftBoxLauncherContent(
    maxGoldEmberStackSize: Int,
    changeMaxGoldEmberStackSize: (Int) -> Unit,
    maxGoldEmberTotalCount: Int,
    changeMaxGoldEmberTotalCount: (Int) -> Unit
) {
    Text(
        text = stringResource(R.string.p_script_mode_gift_box_warning),
        modifier= Modifier.fillMaxWidth(),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.secondary
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            stringResource(R.string.p_max_gold_ember_set_size),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary
        )

        Stepper(
            value = maxGoldEmberStackSize,
            onValueChange = changeMaxGoldEmberStackSize,
            valueRange = 0..100
        )
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            stringResource(R.string.p_max_gold_ember_total_count),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary
        )

        Stepper(
            value = maxGoldEmberTotalCount,
            onValueChange = changeMaxGoldEmberTotalCount,
            valueRange = 1..600
        )
    }
}

@Composable
fun giftBoxLauncher(
    prefs: IPreferences,
    modifier: Modifier = Modifier
): ScriptLauncherResponseBuilder {
    var maxGoldEmberStackSize by remember { mutableStateOf(prefs.maxGoldEmberStackSize) }
    var maxGoldEmberTotalCount by remember { mutableStateOf(prefs.maxGoldEmberTotalCount) }

    Column(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .padding(top = 5.dp)
    ) {
        Text(
            stringResource(R.string.p_script_mode_gift_box),
            style = MaterialTheme.typography.titleLarge
        )

        Divider(
            modifier = Modifier
                .padding(5.dp)
                .padding(bottom = 16.dp)
        )

        GiftBoxLauncherContent(
            maxGoldEmberStackSize = maxGoldEmberStackSize,
            changeMaxGoldEmberStackSize = { maxGoldEmberStackSize = it },
            maxGoldEmberTotalCount = maxGoldEmberTotalCount,
            changeMaxGoldEmberTotalCount = { maxGoldEmberTotalCount = it }
        )
    }

    return ScriptLauncherResponseBuilder(
        canBuild = { true },
        build = { ScriptLauncherResponse.GiftBox(maxGoldEmberStackSize, maxGoldEmberTotalCount) }
    )
}