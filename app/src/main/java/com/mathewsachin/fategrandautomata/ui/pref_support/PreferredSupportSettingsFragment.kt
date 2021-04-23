package com.mathewsachin.fategrandautomata.ui.pref_support

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Card
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.prefs.core.Pref
import com.mathewsachin.fategrandautomata.prefs.core.PrefsCore
import com.mathewsachin.fategrandautomata.prefs.core.SupportPrefsCore
import com.mathewsachin.fategrandautomata.ui.*
import com.mathewsachin.fategrandautomata.ui.prefs.MultiSelectListPreference
import com.mathewsachin.fategrandautomata.ui.prefs.PreferenceGroupHeader
import com.mathewsachin.fategrandautomata.ui.prefs.SwitchPreference
import com.mathewsachin.fategrandautomata.ui.prefs.remember
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PreferredSupportSettingsFragment : Fragment() {
    val args: PreferredSupportSettingsFragmentArgs by navArgs()

    @Inject
    lateinit var prefsCore: PrefsCore

    val vm: PreferredSupportViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        ComposeView(requireContext()).apply {
            val config = prefsCore.forBattleConfig(args.key).support

            setContent {
                PreferredSupport(
                    config = config,
                    vm = vm
                )
            }
        }

    override fun onResume() {
        super.onResume()

        vm.refresh(requireContext())
    }
}

@Composable
fun PreferredSupport(
    config: SupportPrefsCore,
    vm: PreferredSupportViewModel
) {
    FgaScreen {
        val prefServants by config.preferredServants.remember()
        val prefCEs by config.preferredCEs.remember()

        LazyColumn {
            item {
                Heading(stringResource(R.string.p_support_mode_preferred))
            }

            item {
                config.friendsOnly.SwitchPreference(
                    title = stringResource(R.string.p_battle_config_support_friends_only)
                )
            }

            item {
                Card(
                    modifier = Modifier
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(bottom = 16.dp)
                    ) {
                        PreferenceGroupHeader(
                            title = stringResource(R.string.p_battle_config_support_pref_servants)
                        )

                        config.preferredServants.SupportSelectPreference(
                            title = stringResource(R.string.p_battle_config_support_pref_servants),
                            entries = vm.servants
                        )

                        if (prefServants.isNotEmpty()) {
                            config.maxAscended.SwitchPreference(
                                title = stringResource(R.string.p_battle_config_support_max_ascended)
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    stringResource(R.string.p_max_skills),
                                    modifier = Modifier.weight(1f)
                                )

                                MaxSkills(
                                    skills = listOf(
                                        config.skill1Max,
                                        config.skill2Max,
                                        config.skill3Max
                                    )
                                )
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(bottom = 16.dp)
                    ) {
                        PreferenceGroupHeader(
                            title = stringResource(R.string.p_battle_config_support_pref_ces)
                        )

                        config.preferredCEs.SupportSelectPreference(
                            title = stringResource(R.string.p_battle_config_support_pref_ces),
                            entries = vm.ces
                        )

                        if (prefCEs.isNotEmpty()) {
                            config.mlb.SwitchPreference(
                                title = stringResource(R.string.p_battle_config_support_mlb)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MaxSkills(
    skills: List<Pref<Boolean>>
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        fun skillText(max: Boolean) = if (max) "10" else "x"

        skills.forEachIndexed { index, pref ->
            if (index != 0) {
                Text(
                    "/",
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                )
            }

            var max by pref.remember()

            val backgroundColor =
                if (max)
                    MaterialTheme.colors.secondary
                else MaterialTheme.colors.surface

            val foregroundColor =
                if (max)
                    MaterialTheme.colors.onSecondary
                else MaterialTheme.colors.onSurface

            Card(
                elevation = 5.dp,
                backgroundColor = backgroundColor,
                contentColor = foregroundColor
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .clickable { max = !max }
                        .size(40.dp)
                ) {
                    Text(skillText(max))
                }
            }
        }
    }
}

@Composable
fun Pref<Set<String>>.SupportSelectPreference(
    title: String,
    entries: Map<String, String>,
    icon: VectorIcon? = null
) {
    val value by remember()

    MultiSelectListPreference(
        title = title,
        entries = entries,
        icon = icon,
        summary = {
            if (it.isEmpty())
                stringResource(R.string.p_not_set)
            else it.joinToString()
        }
    ) {
        if (value.isNotEmpty()) {
            IconButton(
                onClick = { resetToDefault() }
            ) {
                DimmedIcon(
                    icon(R.drawable.ic_close),
                    contentDescription = "Clear"
                )
            }
        }
    }
}