package io.github.fate_grand_automata.ui.battle_config_item

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.fate_grand_automata.R
import io.github.fate_grand_automata.prefs.core.BattleConfigCore
import io.github.fate_grand_automata.scripts.enums.ShuffleCardsEnum
import io.github.fate_grand_automata.ui.prefs.ListPreference
import io.github.fate_grand_automata.ui.prefs.PreferenceGroupHeader
import io.github.fate_grand_automata.ui.prefs.StepperPreference
import io.github.fate_grand_automata.ui.prefs.remember
import io.github.fate_grand_automata.util.stringRes

@Composable
fun ShuffleCardsGroup(config: BattleConfigCore) {
    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(bottom = 16.dp)
        ) {
            PreferenceGroupHeader(
                title = stringResource(R.string.p_shuffle_cards)
            )

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                config.shuffleCards.ListPreference(
                    title = stringResource(R.string.p_shuffle_cards_when),
                    entries = ShuffleCardsEnum.values()
                        .associateWith { stringResource(it.stringRes) },
                    modifier = Modifier.weight(1f)
                )

                val shuffleType by config.shuffleCards.remember()

                if (shuffleType != ShuffleCardsEnum.None) {
                    config.shuffleCardsWave.StepperPreference(
                        title = stringResource(R.string.p_shuffle_cards_wave),
                        valueRange = 1..3,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}