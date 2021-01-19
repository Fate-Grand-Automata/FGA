package com.mathewsachin.fategrandautomata.ui.prefs

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.viewModels
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SeekBarPreference
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.prefs.core.PrefsCore
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FineTuneSettingsFragment : PreferenceFragmentCompat() {
    val vm: FineTuneSettingsViewModel by viewModels()

    @Inject
    lateinit var prefs: PrefsCore

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        prefScreen {
            category {
                key = "support_swipe_category"
                title = R.string.p_fine_tune_support

                prefs.supportSwipesPerUpdate.seekBar {
                    title = R.string.p_fine_tune_support_swipes_per_update
                    max = 35
                    icon = R.drawable.ic_swipe
                }

                prefs.supportMaxUpdates.seekBar {
                    title = R.string.p_fine_tune_support_max_updates
                    max = 50
                    icon = R.drawable.ic_refresh
                }
            }

            category {
                key = "similarity_category"
                title = R.string.p_fine_tune_similarity

                prefs.minSimilarity.seekBar {
                    title = R.string.p_fine_tune_min_similarity
                    min = 50
                    max = 100
                    icon = R.drawable.ic_image_search
                }

                prefs.mlbSimilarity.seekBar {
                    title = R.string.p_fine_tune_mlb_similarity
                    min = 50
                    max = 100
                    icon = R.drawable.ic_star
                }

                prefs.stageCounterSimilarity.seekBar {
                    title = R.string.p_fine_tune_stage_counter_similarity
                    min = 50
                    max = 100
                    icon = R.drawable.ic_counter
                }
            }

            category {
                key = "click_category"
                title = R.string.p_fine_tune_clicks

                prefs.clickWaitTime.seekBar {
                    title = R.string.p_fine_tune_wait_after_clicking
                    max = 2000
                    icon = R.drawable.ic_click
                }

                prefs.clickDuration.seekBar {
                    title = R.string.p_fine_tune_click_duration
                    min = 1
                    max = 200
                    icon = R.drawable.ic_click
                }

                prefs.clickDelay.seekBar {
                    title = R.string.p_fine_tune_click_delay
                    max = 50
                    icon = R.drawable.ic_click
                }
            }

            category {
                key = "swipes_category"
                title = R.string.p_fine_tune_swipes

                prefs.swipeWaitTime.seekBar {
                    title = R.string.p_fine_tune_wait_after_swiping
                    min = 50
                    max = 3000
                    icon = R.drawable.ic_swipe
                }

                prefs.swipeDuration.seekBar {
                    title = R.string.p_fine_tune_swipe_duration
                    min = 50
                    max = 1000
                    icon = R.drawable.ic_swipe
                }

                prefs.swipeMultiplier.seekBar {
                    title = R.string.p_fine_tune_swipe_multiplier
                    min = 50
                    max = 200
                    icon = R.drawable.ic_swipe
                }
            }

            category {
                key = "wait_category"
                title = R.string.p_fine_tune_wait

                prefs.skillDelay.seekBar {
                    title = R.string.p_fine_tune_skill_delay
                    max = 2000
                    icon = R.drawable.ic_wand
                }

                prefs.waitBeforeTurn.seekBar {
                    title = R.string.p_fine_tune_wait_before_turn
                    max = 2000
                    icon = R.drawable.ic_time
                }

                prefs.waitBeforeCards.seekBar {
                    title = R.string.p_fine_tune_wait_before_cards
                    max = 6000
                    icon = R.drawable.ic_card
                }

                prefs.waitMultiplier.seekBar {
                    title = R.string.p_fine_tune_wait_multiplier
                    min = 50
                    max = 200
                    icon = R.drawable.ic_time
                }
            }
        }

        setHasOptionsMenu(true)

        for (pref in vm.fineTunePrefs) {
            findPreference<Preference>(pref.key)?.let {
                it.summary = getString(R.string.p_fine_tune_default, pref.defaultValue)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        for ((key, liveData) in vm.liveDataMap) {
            findPreference<SeekBarPreference>(key)?.let {
                liveData.observe(viewLifecycleOwner) { value ->
                    it.value = value
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.fine_tune_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.reset_fine_tune -> {
                vm.resetAll()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
