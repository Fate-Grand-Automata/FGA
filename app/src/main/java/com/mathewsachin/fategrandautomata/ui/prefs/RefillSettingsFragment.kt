package com.mathewsachin.fategrandautomata.ui.prefs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.foundation.ScrollableColumn
import androidx.compose.material.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.mathewsachin.fategrandautomata.R
import androidx.compose.runtime.getValue
import com.mathewsachin.fategrandautomata.prefs.core.PrefsCore
import com.mathewsachin.fategrandautomata.scripts.enums.RefillResourceEnum
import com.mathewsachin.fategrandautomata.ui.prefs.compose.*
import com.mathewsachin.fategrandautomata.ui.prefs.compose.ComposePreferencesTheme
import com.mathewsachin.fategrandautomata.util.stringRes
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class RefillSettingsFragment : Fragment() {
    @Inject
    lateinit var prefsCore: PrefsCore

    val vm: MainSettingsViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        ComposeView(requireContext()).apply {
            val refill = prefsCore.refill

            setContent {
                ComposePreferencesTheme {
                    Surface {
                        ScrollableColumn {
                            PreferenceGroup(title = stringResource(R.string.p_refill)) {
                                refill.enabled.SwitchPreference(
                                    title = stringResource(R.string.p_enable_refill),
                                    icon = vectorResource(R.drawable.ic_check)
                                )

                                val refillEnabled by refill.enabled.dependency()
                                val refillResourcesMessage by vm.refillResources.collectAsState("")

                                refill.resources.MultiSelectListPreference(
                                    title = stringResource(R.string.p_resource),
                                    icon = vectorResource(R.drawable.ic_apple),
                                    entries = RefillResourceEnum.values()
                                        .associate {
                                            it.toString() to stringResource(it.stringRes)
                                        },
                                    enabled = refillEnabled,
                                    summary = { refillResourcesMessage },
                                    hint = "Priority is Bronze > Silver > Gold > SQ"
                                )

                                refill.repetitions.EditNumberPreference(
                                    title = stringResource(R.string.p_repetitions),
                                    icon = vectorResource(R.drawable.ic_repeat),
                                    enabled = refillEnabled
                                )

                                refill.autoDecrement.SwitchPreference(
                                    title = stringResource(R.string.p_auto_decrement),
                                    hint = stringResource(R.string.p_auto_decrement_summary),
                                    icon = vectorResource(R.drawable.ic_minus),
                                    enabled = refillEnabled
                                )
                            }

                            PreferenceGroup(title = stringResource(R.string.p_run_limit)) {
                                refill.shouldLimitRuns.SwitchPreference(
                                    title = stringResource(R.string.p_limit_no_of_runs),
                                    icon = vectorResource(R.drawable.ic_check)
                                )

                                val shouldLimitRuns by refill.shouldLimitRuns.dependency()

                                refill.limitRuns.EditNumberPreference(
                                    title = stringResource(R.string.p_runs),
                                    icon = vectorResource(R.drawable.ic_repeat),
                                    enabled = shouldLimitRuns
                                )

                                refill.autoDecrementRuns.SwitchPreference(
                                    title = stringResource(R.string.p_auto_decrement),
                                    icon = vectorResource(R.drawable.ic_minus),
                                    enabled = shouldLimitRuns
                                )
                            }

                            PreferenceGroup(title = stringResource(R.string.p_mat_limit)) {
                                refill.shouldLimitMats.SwitchPreference(
                                    title = stringResource(R.string.p_mat_limit),
                                    icon = vectorResource(R.drawable.ic_check)
                                )

                                val shouldLimitMats by refill.shouldLimitMats.dependency()

                                refill.limitMats.EditNumberPreference(
                                    title = stringResource(R.string.p_mat_limit_count),
                                    icon = vectorResource(R.drawable.ic_repeat),
                                    enabled = shouldLimitMats
                                )

                                refill.autoDecrementMats.SwitchPreference(
                                    title = stringResource(R.string.p_auto_decrement),
                                    icon = vectorResource(R.drawable.ic_minus),
                                    enabled = shouldLimitMats
                                )
                            }
                        }
                    }
                }
            }
        }
}
