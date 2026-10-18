package io.github.fate_grand_automata.ui.battle_config_item

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.fate_grand_automata.R
import io.github.fate_grand_automata.prefs.core.BattleConfigCore
import io.github.fate_grand_automata.ui.prefs.PreferenceGroupHeader
import io.github.fate_grand_automata.ui.prefs.StepperPreference
import io.github.fate_grand_automata.ui.prefs.SwitchPreference
import io.github.fate_grand_automata.ui.prefs.remember

@Composable
fun TeapotsGroup(config: BattleConfigCore) {
    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            PreferenceGroupHeader(
                title = stringResource(R.string.p_should_use_teapots)
            )

            config.shouldUseTeapots.SwitchPreference(
                title = stringResource(R.string.p_should_use_teapots),
                summary = stringResource(R.string.p_should_use_teapots_summary)
            )

            val shouldUseTeapots by config.shouldUseTeapots.remember()

            if (shouldUseTeapots) {
                config.limitTeapots.StepperPreference(
                    title = stringResource(R.string.p_limit_teapots),
                    valueRange = 0..999,
                    valueRepresentation = { if (it == 0) "No Limit" else it.toString() }
                )
            }
        }
    }
}
