package io.github.fate_grand_automata.ui.fine_tune

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.fate_grand_automata.R
import io.github.fate_grand_automata.prefs.core.PrefsCore
import io.github.fate_grand_automata.ui.icon
import javax.inject.Inject

@HiltViewModel
class FineTuneSettingsViewModel @Inject constructor(
    val prefs: PrefsCore
) : ViewModel() {
    val groups = listOf(
        FineTuneGroup(
            name = R.string.p_fine_tune_support,
            items = listOf(
                FineTuneItem(
                    pref = prefs.supportSwipesPerUpdate,
                    name = R.string.p_fine_tune_support_swipes_per_update,
                    icon = icon(R.drawable.ic_swipe),
                    valueRange = 0..35,
                    hint = R.string.p_fine_tune_support_swipes_per_update_hint
                ),
                FineTuneItem(
                    pref = prefs.supportMaxUpdates,
                    name = R.string.p_fine_tune_support_max_updates,
                    icon = icon(R.drawable.ic_refresh),
                    valueRange = 0..50,
                    hint = R.string.p_fine_tune_support_max_updates_hint
                )
            )
        ),
        FineTuneGroup(
            name = R.string.p_fine_tune_similarity,
            items = listOf(
                FineTuneItem(
                    pref = prefs.minSimilarity,
                    name = R.string.p_fine_tune_min_similarity,
                    icon = icon(R.drawable.ic_image_search),
                    valueRange = 50..100,
                    valueRepresentation = { "$it%" },
                    hint = R.string.p_fine_tune_min_similarity_hint
                ),
                FineTuneItem(
                    pref = prefs.mlbSimilarity,
                    name = R.string.p_fine_tune_mlb_similarity,
                    icon = icon(Icons.Default.Star),
                    valueRange = 50..100,
                    valueRepresentation = { "$it%" },
                    hint = R.string.p_fine_tune_mlb_similarity_hint
                ),
                FineTuneItem(
                    pref = prefs.stageCounterSimilarity,
                    name = R.string.p_fine_tune_stage_counter_similarity,
                    icon = icon(R.drawable.ic_counter),
                    valueRange = 50..100,
                    valueRepresentation = { "$it%" },
                    hint = R.string.p_fine_tune_stage_counter_similarity_hint
                )
            )
        ),
        FineTuneGroup(
            name = R.string.p_fine_tune_clicks,
            items = listOf(
                FineTuneItem(
                    pref = prefs.clickWaitTime,
                    name = R.string.p_fine_tune_wait_after_clicking,
                    icon = icon(R.drawable.ic_click),
                    valueRange = 50..2000,
                    valueRepresentation = { "${it}ms" },
                    step = 50,
                    hint = R.string.p_fine_tune_wait_after_clicking_hint
                ),
                FineTuneItem(
                    pref = prefs.clickDuration,
                    name = R.string.p_fine_tune_click_duration,
                    icon = icon(R.drawable.ic_click),
                    valueRange = 5..200,
                    step = 5,
                    valueRepresentation = { "${it}ms" },
                    hint = R.string.p_fine_tune_click_duration_hint
                ),
                FineTuneItem(
                    pref = prefs.clickDelay,
                    name = R.string.p_fine_tune_click_delay,
                    icon = icon(R.drawable.ic_click),
                    valueRange = 5..50,
                    step = 5,
                    valueRepresentation = { "${it}ms" },
                    hint = R.string.p_fine_tune_click_delay_hint
                )
            )
        ),
        FineTuneGroup(
            name = R.string.p_fine_tune_swipes,
            items = listOf(
                FineTuneItem(
                    pref = prefs.swipeWaitTime,
                    name = R.string.p_fine_tune_wait_after_swiping,
                    icon = icon(R.drawable.ic_swipe),
                    valueRange = 50..3000,
                    step = 50,
                    valueRepresentation = { "${it}ms" },
                    hint = R.string.p_fine_tune_wait_after_swiping_hint
                ),
                FineTuneItem(
                    pref = prefs.swipeDuration,
                    name = R.string.p_fine_tune_swipe_duration,
                    icon = icon(R.drawable.ic_swipe),
                    valueRange = 50..1000,
                    step = 50,
                    valueRepresentation = { "${it}ms" },
                    hint = R.string.p_fine_tune_swipe_duration_hint
                ),
                FineTuneItem(
                    pref = prefs.swipeMultiplier,
                    name = R.string.p_fine_tune_swipe_multiplier,
                    icon = icon(R.drawable.ic_swipe),
                    valueRange = 50..200,
                    step = 5,
                    valueRepresentation = { "${it}%" },
                    hint = R.string.p_fine_tune_swipe_multiplier_hint
                )
            )
        ),
        FineTuneGroup(
            name = R.string.p_fine_tune_wait,
            items = listOf(
                FineTuneItem(
                    pref = prefs.skillDelay,
                    name = R.string.p_fine_tune_skill_delay,
                    icon = icon(R.drawable.ic_wand),
                    valueRange = 50..2000,
                    step = 50,
                    valueRepresentation = { "${it}ms" },
                    hint = R.string.p_fine_tune_skill_delay_hint
                ),
                FineTuneItem(
                    pref = prefs.waitBeforeTurn,
                    name = R.string.p_fine_tune_wait_before_turn,
                    icon = icon(R.drawable.ic_time),
                    valueRange = 50..2000,
                    step = 50,
                    valueRepresentation = { "${it}ms" },
                    hint = R.string.p_fine_tune_wait_before_turn_hint
                ),
                FineTuneItem(
                    pref = prefs.waitBeforeCards,
                    name = R.string.p_fine_tune_wait_before_cards,
                    icon = icon(R.drawable.ic_card),
                    valueRange = 50..6000,
                    step = 50,
                    valueRepresentation = { "${it}ms" },
                    hint = R.string.p_fine_tune_wait_before_cards_hint
                ),
                FineTuneItem(
                    pref = prefs.waitMultiplier,
                    name = R.string.p_fine_tune_wait_multiplier,
                    icon = icon(R.drawable.ic_time),
                    valueRange = 50..200,
                    step = 5,
                    valueRepresentation = { "${it}%" },
                    hint = R.string.p_fine_tune_wait_multiplier_hint
                )
            )
        )
    )

    init {
        groups.forEach { group ->
            group.items.forEach { it.normalizeStoredValue() }
        }
    }

    fun resetAll() =
        groups.forEach { group ->
            group.items.forEach { it.reset() }
        }
}