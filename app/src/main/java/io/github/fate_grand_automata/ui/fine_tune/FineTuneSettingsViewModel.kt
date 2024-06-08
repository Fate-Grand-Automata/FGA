package io.github.fate_grand_automata.ui.fine_tune

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TouchApp
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
                    hint = "Number of times to scroll through support list before refreshing."
                ),
                FineTuneItem(
                    pref = prefs.supportMaxUpdates,
                    name = R.string.p_fine_tune_support_max_updates,
                    icon = icon(R.drawable.ic_refresh),
                    valueRange = 0..50,
                    hint = "Maximum number of times to refresh in support screen after which the configured fallback option is used."
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
                    hint = "The similarity threshold used for all image matching. Don't unnecessarily change this."
                ),
                FineTuneItem(
                    pref = prefs.mlbSimilarity,
                    name = R.string.p_fine_tune_mlb_similarity,
                    icon = icon(Icons.Default.Star),
                    valueRange = 50..100,
                    valueRepresentation = { "$it%" },
                    hint = "Similarity threshold used for matching MLB star. Reduce this by a bit if MLB CEs are not detected."
                ),
                FineTuneItem(
                    pref = prefs.stageCounterSimilarity,
                    name = R.string.p_fine_tune_stage_counter_similarity,
                    icon = icon(R.drawable.ic_counter),
                    valueRange = 50..100,
                    valueRepresentation = { "$it%" },
                    hint = "Similarity threshold for detecting wave change. If your skill commands are used in the wrong wave, tweaking this might help."
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
                    valueRange = 0..2000,
                    valueRepresentation = { "${it}ms" },
                    hint = "Delay after each click/tap unless clicking repeatedly. Some time is needed for the game's animations to finish."
                ),
                FineTuneItem(
                    pref = prefs.clickDuration,
                    name = R.string.p_fine_tune_click_duration,
                    icon = icon(R.drawable.ic_click),
                    valueRange = 1..200,
                    valueRepresentation = { "${it}ms" },
                    hint = "Every tap/click is like a hold and release performed quickly. This sets the time difference between the two."
                ),
                FineTuneItem(
                    pref = prefs.clickDelay,
                    name = R.string.p_fine_tune_click_delay,
                    icon = icon(R.drawable.ic_click),
                    valueRange = 0..50,
                    valueRepresentation = { "${it}ms" },
                    hint = "Delay between individual taps/clicks when doing so repeatedly like at the end of battles, friend point summon and lottery script."
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
                    valueRepresentation = { "${it}ms" },
                    hint = "Wait after all swipes. Some time is needed for the game's animations to finish."
                ),
                FineTuneItem(
                    pref = prefs.swipeDuration,
                    name = R.string.p_fine_tune_swipe_duration,
                    icon = icon(R.drawable.ic_swipe),
                    valueRange = 50..1000,
                    valueRepresentation = { "${it}ms" },
                    hint = "Time taken to swipe. Swiping faster will scroll more, slower will scroll less."
                ),
                FineTuneItem(
                    pref = prefs.swipeMultiplier,
                    name = R.string.p_fine_tune_swipe_multiplier,
                    icon = icon(R.drawable.ic_swipe),
                    valueRange = 50..200,
                    valueRepresentation = { "${it}%" },
                    hint = "Control the length of swipes. This is multiplied with the number of pixels to swipe over. Use along with swipe duration to tweak it to your needs."
                )
            )
        ),
        FineTuneGroup(
            name = R.string.p_fine_tune_drag,
            items = listOf(
                FineTuneItem(
                    pref = prefs.longPressDuration,
                    name = R.string.p_fine_tune_long_press_duration,
                    icon = icon(Icons.Default.TouchApp),
                    valueRange = 500..3000,
                    valueRepresentation = { "${it}ms" },
                    hint = "Duration of long press. This is used for dragging CEs in enhancement screen."
                ),
                FineTuneItem(
                    pref = prefs.dragDuration,
                    name = R.string.p_fine_tune_drag_duration,
                    icon = icon(R.drawable.ic_swipe),
                    valueRange = 50..1000,
                    valueRepresentation = { "${it}ms" },
                    hint = "Duration of drag. This is used for dragging CEs in enhancement screen."
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
                    valueRange = 0..2000,
                    valueRepresentation = { "${it}ms" },
                    hint = "Delay between pressing on skill and pressing on target servant."
                ),
                FineTuneItem(
                    pref = prefs.waitBeforeTurn,
                    name = R.string.p_fine_tune_wait_before_turn,
                    icon = icon(R.drawable.ic_time),
                    valueRange = 0..2000,
                    valueRepresentation = { "${it}ms" },
                    hint = "Delay before the skill sequence starts after Battle screen is detected. Slower devices might need longer delay."
                ),
                FineTuneItem(
                    pref = prefs.waitBeforeCards,
                    name = R.string.p_fine_tune_wait_before_cards,
                    icon = icon(R.drawable.ic_card),
                    valueRange = 0..6000,
                    valueRepresentation = { "${it}ms" },
                    hint = "Delay between clicking on Attack button and clicking on face-cards/NP. Slower devices might need longer delay."
                ),
                FineTuneItem(
                    pref = prefs.waitMultiplier,
                    name = R.string.p_fine_tune_wait_multiplier,
                    icon = icon(R.drawable.ic_time),
                    valueRange = 50..200,
                    valueRepresentation = { "${it}%" },
                    hint = "This multiples to every wait/delay. So, you can make the overall script slower/faster by using this."
                )
            )
        )
    )

    fun resetAll() =
        groups.forEach { group ->
            group.items.forEach { it.reset() }
        }
}