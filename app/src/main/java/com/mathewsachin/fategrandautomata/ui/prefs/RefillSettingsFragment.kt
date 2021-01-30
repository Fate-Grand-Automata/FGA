package com.mathewsachin.fategrandautomata.ui.prefs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.foundation.ScrollableColumn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.mathewsachin.fategrandautomata.R
import androidx.compose.runtime.*
import androidx.compose.runtime.savedinstancestate.savedInstanceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mathewsachin.fategrandautomata.prefs.core.PrefsCore
import com.mathewsachin.fategrandautomata.prefs.core.RefillPrefsCore
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
                            RefillGroup(refill = refill, vm = vm)
                            RunLimitGroup(refill = refill)
                            MatLimitGroup(refill = refill)
                        }
                    }
                }
            }
        }
}

@Composable
fun RefillGroup(
    refill: RefillPrefsCore,
    vm: MainSettingsViewModel
) {
    PreferenceGroup(title = stringResource(R.string.p_refill)) {
        val refillEnabled by refill.enabled.collect()
        val refillResourcesMessage by vm.refillResources.collectAsState("")

        Row {
            val refillResources by refill.resources
                .asFlow()
                .collectAsState(refill.resources.get())

            val refillResourcesDialog = multiSelectListDialog(
                selected = refillResources,
                selectedChange = { refill.resources.set(it) },
                entries = RefillResourceEnum.values()
                    .associate {
                        it.toString() to stringResource(it.stringRes)
                    },
                title = stringResource(R.string.p_resource)
            )

            ListItem(
                icon = {
                    Checkbox(
                        checked = refillEnabled,
                        onCheckedChange = { refill.enabled.set(it) },
                        modifier = Modifier.size(40.dp)
                    )
                },
                modifier = Modifier
                    .clickable {
                        if (refillEnabled) {
                            refillResourcesDialog.show()
                        }
                    },
                trailing = {
                    var showRepetitionDialog by savedInstanceState { false }
                    val refillRepetitions by refill.repetitions
                        .asFlow()
                        .collectAsState(refill.repetitions.get())

                    StatusWrapper(refillEnabled) {
                        Box(
                            modifier = Modifier
                                .clickable {
                                    if (refillEnabled) {
                                        showRepetitionDialog = true
                                    }
                                }
                                .preferredHeight(40.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Text(
                                text = "x$refillRepetitions",
                                style = MaterialTheme.typography.subtitle1,
                                modifier = Modifier
                                    .padding(horizontal = 16.dp)
                            )
                        }
                    }

                    EditTextPreferenceDialog(
                        title = stringResource(R.string.p_repetitions),
                        showDialog = showRepetitionDialog,
                        value = refillRepetitions.toString(),
                        valueChange = {
                            val pref = refill.repetitions
                            val value = it.toIntOrNull()?.coerceIn(0, 999)
                            pref.set(value ?: pref.defaultValue)
                        },
                        closeDialog = { showRepetitionDialog = false }
                    )
                }
            ) {
                StatusWrapper(refillEnabled) {
                    Text(refillResourcesMessage)
                }
            }
        }

        if (refillEnabled) {
            refill.autoDecrement.SwitchPreference(
                title = stringResource(R.string.p_auto_decrement),
                summary = stringResource(R.string.p_auto_decrement_summary),
                icon = vectorResource(R.drawable.ic_minus)
            )
        }
    }
}

@Composable
fun RunLimitGroup(
    refill: RefillPrefsCore
) {
    PreferenceGroup(title = stringResource(R.string.p_run_limit)) {
        val shouldLimitRuns by refill.shouldLimitRuns.collect()

        Row {
            Checkbox(
                checked = shouldLimitRuns,
                onCheckedChange = { refill.shouldLimitRuns.set(it) },
                modifier = Modifier
                    .padding(start = 16.dp)
                    .align(Alignment.CenterVertically)
                    .size(40.dp)
            )

            refill.limitRuns.EditNumberPreference(
                title = stringResource(R.string.p_limit_no_of_runs),
                enabled = shouldLimitRuns
            )
        }

        if (shouldLimitRuns) {
            refill.autoDecrementRuns.SwitchPreference(
                title = stringResource(R.string.p_auto_decrement),
                icon = vectorResource(R.drawable.ic_minus)
            )
        }
    }
}

@Composable
fun MatLimitGroup(
    refill: RefillPrefsCore
) {
    PreferenceGroup(title = stringResource(R.string.p_mat_limit)) {
        val shouldLimitMats by refill.shouldLimitMats.collect()

        Row {
            Checkbox(
                checked = shouldLimitMats,
                onCheckedChange = { refill.shouldLimitMats.set(it) },
                modifier = Modifier
                    .padding(start = 16.dp)
                    .align(Alignment.CenterVertically)
                    .size(40.dp)
            )

            refill.limitMats.EditNumberPreference(
                title = stringResource(R.string.p_mat_limit),
                enabled = shouldLimitMats
            )
        }

        if (shouldLimitMats) {
            refill.autoDecrementMats.SwitchPreference(
                title = stringResource(R.string.p_auto_decrement),
                icon = vectorResource(R.drawable.ic_minus)
            )
        }
    }
}