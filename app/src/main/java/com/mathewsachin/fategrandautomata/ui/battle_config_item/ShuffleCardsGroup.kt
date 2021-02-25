package com.mathewsachin.fategrandautomata.ui.battle_config_item

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.prefs.core.BattleConfigCore
import com.mathewsachin.fategrandautomata.scripts.enums.ShuffleCardsEnum
import com.mathewsachin.fategrandautomata.ui.prefs.ListPreference
import com.mathewsachin.fategrandautomata.ui.prefs.PreferenceGroup
import com.mathewsachin.fategrandautomata.ui.prefs.StepperPreference
import com.mathewsachin.fategrandautomata.ui.prefs.collect
import com.mathewsachin.fategrandautomata.util.stringRes

@Composable
fun ShuffleCardsGroup(config: BattleConfigCore) {
    PreferenceGroup(title = stringResource(R.string.p_shuffle_cards)) {
        Row {
            Box(modifier = Modifier.weight(1f)) {
                config.shuffleCards.ListPreference(
                    title = stringResource(R.string.p_shuffle_cards_when),
                    entries = ShuffleCardsEnum.values()
                        .associateWith { stringResource(it.stringRes) }
                )
            }

            val shuffleType by config.shuffleCards.collect()

            if (shuffleType != ShuffleCardsEnum.None) {
                Box(modifier = Modifier.weight(1f)) {
                    config.shuffleCardsWave.StepperPreference(
                        title = stringResource(R.string.p_shuffle_cards_wave),
                        valueRange = 1..3
                    )
                }
            }
        }
    }
}