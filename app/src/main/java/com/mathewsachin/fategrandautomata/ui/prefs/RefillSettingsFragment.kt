package com.mathewsachin.fategrandautomata.ui.prefs

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.preference.EditTextPreference
import androidx.preference.MultiSelectListPreference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.prefs.core.PrefsCore
import com.mathewsachin.fategrandautomata.scripts.enums.RefillResourceEnum
import com.mathewsachin.fategrandautomata.util.initWith
import com.mathewsachin.fategrandautomata.util.stringRes
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class RefillSettingsFragment : PreferenceFragmentCompat() {
    @Inject
    lateinit var prefsCore: PrefsCore

    private lateinit var repetitions: EditTextPreference
    private lateinit var resources: MultiSelectListPreference
    private lateinit var shouldLimitRuns: SwitchPreferenceCompat
    private lateinit var runLimit: EditTextPreference
    private lateinit var shouldLimitMats: SwitchPreferenceCompat
    private lateinit var matLimit: EditTextPreference

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        val refill = prefsCore.refill

        prefScreen {
            category {
                key = "refill_category"
                title = R.string.p_refill

                refill.enabled.switch {
                    title = R.string.p_enable_refill
                    icon = R.drawable.ic_check
                }

                resources = refill.resources.multiSelect {
                    dependency = refill.enabled
                    icon = R.drawable.ic_apple
                    title = R.string.p_resource
                }.initWith<RefillResourceEnum> { it.stringRes }

                repetitions = refill.repetitions.numeric {
                    dependency = refill.enabled
                    icon = R.drawable.ic_repeat
                    title = R.string.p_repetitions
                }

                refill.autoDecrement.switch {
                    dependency = refill.enabled
                    icon = R.drawable.ic_minus
                    title = R.string.p_auto_decrement
                    summary = R.string.p_auto_decrement_summary
                }
            }

            category {
                key = "run_limit_category"
                title = R.string.p_run_limit

                shouldLimitRuns = refill.shouldLimitRuns.switch {
                    title = R.string.p_limit_no_of_runs
                    icon = R.drawable.ic_check
                }

                runLimit = refill.limitRuns.numeric {
                    dependency = refill.shouldLimitRuns
                    title = R.string.p_runs
                    icon = R.drawable.ic_repeat
                }

                refill.autoDecrementRuns.switch {
                    dependency = refill.shouldLimitRuns
                    title = R.string.p_auto_decrement
                    icon = R.drawable.ic_minus
                }
            }

            category {
                key = "mat_limit_category"
                title = R.string.p_mat_limit

                shouldLimitMats = refill.shouldLimitMats.switch {
                    title = R.string.p_mat_limit
                    icon = R.drawable.ic_check
                }

                matLimit = refill.limitMats.numeric {
                    dependency = refill.shouldLimitMats
                    title = R.string.p_mat_limit_count
                    icon = R.drawable.ic_repeat
                }

                refill.autoDecrementMats.switch {
                    dependency = refill.shouldLimitMats
                    title = R.string.p_auto_decrement
                    icon = R.drawable.ic_minus
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val vm: MainSettingsViewModel by activityViewModels()

        // These don't update automatically
        vm.refillRepetitions.observe(viewLifecycleOwner) {
            repetitions.text = it.toString()
        }

        vm.shouldLimitRuns.observe(viewLifecycleOwner) {
            shouldLimitRuns.isChecked = it
        }

        vm.limitRuns.observe(viewLifecycleOwner) {
            runLimit.text = it.toString()
        }

        vm.shouldLimitMats.observe(viewLifecycleOwner) {
            shouldLimitMats.isChecked = it
        }

        vm.limitMats.observe(viewLifecycleOwner) {
            matLimit.text = it.toString()
        }

        // Refill resources shown with priority
        vm.refillResources.observe(viewLifecycleOwner) {
            resources.summary = it
        }
    }
}
