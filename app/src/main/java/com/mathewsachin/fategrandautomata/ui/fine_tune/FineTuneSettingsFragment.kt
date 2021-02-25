package com.mathewsachin.fategrandautomata.ui.fine_tune

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.ScrollableColumn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.ExtendedFloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.prefs.core.Pref
import com.mathewsachin.fategrandautomata.prefs.core.PrefsCore
import com.mathewsachin.fategrandautomata.ui.prefs.FgaTheme
import com.mathewsachin.fategrandautomata.ui.prefs.PreferenceGroup
import com.mathewsachin.fategrandautomata.ui.prefs.SeekBarPreference
import com.vanpra.composematerialdialogs.MaterialDialog
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FineTuneSettingsFragment : Fragment() {
    val vm: FineTuneSettingsViewModel by viewModels()

    @Inject
    lateinit var prefs: PrefsCore

    @Composable
    fun Pref<Int>.FineTuneSeekBar(
        @StringRes title: Int,
        @DrawableRes icon: Int,
        valueRange: IntRange = 0..100,
        valueRepresentation: (Int) -> String = { it.toString() },
        // TODO: Localize fine-tune hints
        hint: String = ""
    ) {
        val defaultString = "Default: ${valueRepresentation(defaultValue)}"

        val hintDialog = MaterialDialog()
        hintDialog.build {
            iconTitle(
                textRes = title,
                iconRes = icon,
            )

            message("$defaultString\n\n$hint")

            buttons {
                negativeButton(res = android.R.string.cancel)
                // TODO: Localize 'Reset to default'
                positiveButton("Reset to default") {
                    vm.reset(this@FineTuneSeekBar)
                }
            }
        }

        Row {
            Box(
                modifier = Modifier.weight(1f)
            ) {
                SeekBarPreference(
                    title = stringResource(title),
                    summary = defaultString,
                    valueRange = valueRange,
                    valueRepresentation = valueRepresentation,
                    state = vm.getState(this@FineTuneSeekBar)
                )
            }

            Icon(
                imageVector = vectorResource(id = R.drawable.ic_info),
                modifier = Modifier
                    .padding(end = 16.dp)
                    .size(40.dp)
                    .clickable(onClick = { hintDialog.show() })
            )
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        ComposeView(requireContext()).apply {
            setContent {
                FgaTheme {
                    Box {
                        ScrollableColumn(
                            modifier = Modifier.padding(bottom = 60.dp)
                        ) {
                            SupportGroup(prefs)
                            SimilarityGroup(prefs)
                            ClicksGroup(prefs)
                            SwipesGroup(prefs)
                            WaitGroup(prefs)
                            Spacer(Modifier.padding(30.dp))
                        }

                        ExtendedFloatingActionButton(
                            text = {
                                Text(
                                    stringResource(R.string.fine_tune_menu_reset_to_defaults),
                                    color = Color.White
                                )
                            },
                            onClick = { vm.resetAll() },
                            icon = {
                                Icon(
                                    vectorResource(R.drawable.ic_refresh),
                                    tint = Color.White
                                )
                            },
                            backgroundColor = colorResource(R.color.colorPrimary),
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(32.dp)
                        )
                    }
                }
            }
        }

    @Composable
    fun SupportGroup(prefs: PrefsCore) {
        PreferenceGroup(title = stringResource(R.string.p_fine_tune_support)) {
            prefs.supportSwipesPerUpdate.FineTuneSeekBar(
                title = R.string.p_fine_tune_support_swipes_per_update,
                icon = R.drawable.ic_swipe,
                valueRange = 0..35,
                hint = "Number of times to scroll through support list before refreshing."
            )

            prefs.supportMaxUpdates.FineTuneSeekBar(
                title = R.string.p_fine_tune_support_max_updates,
                icon = R.drawable.ic_refresh,
                valueRange = 0..50,
                hint = "Maximum number of times to refresh in support screen after which the configured fallback option is used."
            )
        }
    }

    @Composable
    fun SimilarityGroup(prefs: PrefsCore) {
        PreferenceGroup(title = stringResource(R.string.p_fine_tune_similarity)) {
            prefs.minSimilarity.FineTuneSeekBar(
                title = R.string.p_fine_tune_min_similarity,
                icon = R.drawable.ic_image_search,
                valueRange = 50..100,
                valueRepresentation = { "$it%" },
                hint = "The similarity threshold used for all image matching. Don't unnecessarily change this."
            )

            prefs.mlbSimilarity.FineTuneSeekBar(
                title = R.string.p_fine_tune_mlb_similarity,
                icon = R.drawable.ic_star,
                valueRange = 50..100,
                valueRepresentation = { "$it%" },
                hint = "Similarity threshold used for matching MLB star. Reduce this by a bit if MLB CEs are not detected."
            )

            prefs.stageCounterSimilarity.FineTuneSeekBar(
                title = R.string.p_fine_tune_stage_counter_similarity,
                icon = R.drawable.ic_counter,
                valueRange = 50..100,
                valueRepresentation = { "$it%" },
                hint = "Similarity threshold for detecting wave change. If your skill commands are used in the wrong wave, tweaking this might help."
            )
        }
    }

    @Composable
    fun ClicksGroup(prefs: PrefsCore) {
        PreferenceGroup(title = stringResource(R.string.p_fine_tune_clicks)) {
            prefs.clickWaitTime.FineTuneSeekBar(
                title = R.string.p_fine_tune_wait_after_clicking,
                icon = R.drawable.ic_click,
                valueRange = 0..2000,
                valueRepresentation = { "${it}ms" },
                hint = "Delay after each click/tap unless clicking repeatedly. Some time is needed for the game's animations to finish."
            )

            prefs.clickDuration.FineTuneSeekBar(
                title = R.string.p_fine_tune_click_duration,
                icon = R.drawable.ic_click,
                valueRange = 1..200,
                valueRepresentation = { "${it}ms" },
                hint = "Every tap/click is like a hold and release performed quickly. This sets the time difference between the two."
            )

            prefs.clickDelay.FineTuneSeekBar(
                title = R.string.p_fine_tune_click_delay,
                icon = R.drawable.ic_click,
                valueRange = 0..50,
                valueRepresentation = { "${it}ms" },
                hint = "Delay between individual taps/clicks when doing so repeatedly like at the end of battles, friend point summon and lottery script."
            )
        }
    }

    @Composable
    fun SwipesGroup(prefs: PrefsCore) {
        PreferenceGroup(title = stringResource(R.string.p_fine_tune_swipes)) {
            prefs.swipeWaitTime.FineTuneSeekBar(
                title = R.string.p_fine_tune_wait_after_swiping,
                icon = R.drawable.ic_swipe,
                valueRange = 50..3000,
                valueRepresentation = { "${it}ms" },
                hint = "Wait after all swipes. Some time is needed for the game's animations to finish."
            )

            prefs.swipeDuration.FineTuneSeekBar(
                title = R.string.p_fine_tune_swipe_duration,
                icon = R.drawable.ic_swipe,
                valueRange = 50..1000,
                valueRepresentation = { "${it}ms" },
                hint = "Time taken to swipe. Swiping faster will scroll more, slower will scroll less."
            )

            prefs.swipeMultiplier.FineTuneSeekBar(
                title = R.string.p_fine_tune_swipe_multiplier,
                icon = R.drawable.ic_swipe,
                valueRange = 50..200,
                valueRepresentation = { "${it}%" },
                hint = "Control the length of swipes. This is multiplied with the number of pixels to swipe over. Use along with swipe duration to tweak it to your needs."
            )
        }
    }

    @Composable
    fun WaitGroup(prefs: PrefsCore) {
        PreferenceGroup(title = stringResource(R.string.p_fine_tune_wait)) {
            prefs.skillDelay.FineTuneSeekBar(
                title = R.string.p_fine_tune_skill_delay,
                icon = R.drawable.ic_wand,
                valueRange = 0..2000,
                valueRepresentation = { "${it}ms" },
                hint = "Delay between pressing on skill and pressing on target servant."
            )

            prefs.waitBeforeTurn.FineTuneSeekBar(
                title = R.string.p_fine_tune_wait_before_turn,
                icon = R.drawable.ic_time,
                valueRange = 0..2000,
                valueRepresentation = { "${it}ms" },
                hint = "Delay before the skill sequence starts after Battle screen is detected. Slower devices might need longer delay."
            )

            prefs.waitBeforeCards.FineTuneSeekBar(
                title = R.string.p_fine_tune_wait_before_cards,
                icon = R.drawable.ic_card,
                valueRange = 0..6000,
                valueRepresentation = { "${it}ms" },
                hint = "Delay between clicking on Attack button and clicking on face-cards/NP. Slower devices might need longer delay."
            )

            prefs.waitMultiplier.FineTuneSeekBar(
                title = R.string.p_fine_tune_wait_multiplier,
                icon = R.drawable.ic_time,
                valueRange = 50..200,
                valueRepresentation = { "${it}%" },
                hint = "This multiples to every wait/delay. So, you can make the overall script slower/faster by using this."
            )
        }
    }
}