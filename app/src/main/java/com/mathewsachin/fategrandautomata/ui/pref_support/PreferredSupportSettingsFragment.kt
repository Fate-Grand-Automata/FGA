package com.mathewsachin.fategrandautomata.ui.pref_support

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.prefs.core.Pref
import com.mathewsachin.fategrandautomata.prefs.core.PrefsCore
import com.mathewsachin.fategrandautomata.ui.DimmedIcon
import com.mathewsachin.fategrandautomata.ui.FgaTheme
import com.mathewsachin.fategrandautomata.ui.Heading
import com.mathewsachin.fategrandautomata.ui.VectorIcon
import com.mathewsachin.fategrandautomata.ui.prefs.MultiSelectListPreference
import com.mathewsachin.fategrandautomata.ui.prefs.PreferenceGroup
import com.mathewsachin.fategrandautomata.ui.prefs.SwitchPreference
import com.mathewsachin.fategrandautomata.ui.prefs.collect
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
                FgaTheme {
                    LazyColumn {
                        item {
                            Heading(stringResource(R.string.p_support_mode_preferred))
                        }

                        item {
                            config.friendsOnly.SwitchPreference(
                                title = stringResource(R.string.p_battle_config_support_friends_only)
                            )

                            Divider()
                        }

                        item {
                            PreferenceGroup(title = stringResource(R.string.p_battle_config_support_pref_servants)) {
                                config.preferredServants.SupportSelectPreference(
                                    title = stringResource(R.string.p_battle_config_support_pref_servants),
                                    entries = vm.servants
                                )

                                val prefServants by config.preferredServants.collect()

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

                            Divider()
                        }

                        item {
                            PreferenceGroup(title = stringResource(R.string.p_battle_config_support_pref_ces)) {
                                config.preferredCEs.SupportSelectPreference(
                                    title = stringResource(R.string.p_battle_config_support_pref_ces),
                                    entries = vm.ces
                                )

                                val prefCEs by config.preferredCEs.collect()

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

    override fun onResume() {
        super.onResume()

        vm.refresh(requireContext())
    }

    @Composable
    fun MaxSkills(
        skills: List<Pref<Boolean>>
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            fun skillText(max: Boolean) = if (max) "10" else "x"
            fun Pref<Boolean>.toggle() =
                set(!get())

            skills.forEachIndexed { index, pref ->
                if (index != 0) {
                    Text(
                        "/",
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                    )
                }

                val max by pref.collect()

                val backgroundColor by animateColorAsState(
                    if (max)
                        MaterialTheme.colors.secondary
                    else MaterialTheme.colors.surface,
                )

                val foregroundColor =
                    if (max)
                        MaterialTheme.colors.onSecondary
                    else MaterialTheme.colors.onSurface

                Card(
                    modifier = Modifier
                        .clickable { pref.toggle() }
                        .size(40.dp),
                    backgroundColor = backgroundColor,
                    contentColor = foregroundColor
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Text(skillText(max))
                    }
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
    val value by collect()

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
            DimmedIcon(
                painterResource(R.drawable.ic_close),
                contentDescription = "Clear",
                modifier = Modifier
                    .size(40.dp)
                    .clickable(onClick = { resetToDefault() })
                    .padding(7.dp)
            )
        }
    }
}