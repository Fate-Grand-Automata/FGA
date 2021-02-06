package com.mathewsachin.fategrandautomata.ui.prefs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.foundation.ScrollableColumn
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.prefs.core.Pref
import com.mathewsachin.fategrandautomata.prefs.core.PrefsCore
import com.mathewsachin.fategrandautomata.ui.prefs.compose.*
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
                    ScrollableColumn {
                        config.friendsOnly.SwitchPreference(
                            title = stringResource(R.string.p_battle_config_support_friends_only),
                            icon = vectorResource(R.drawable.ic_friend)
                        )

                        PreferenceGroup(title = stringResource(R.string.p_battle_config_support_pref_servants)) {
                            config.preferredServants.SupportSelectPreference(
                                title = stringResource(R.string.p_battle_config_support_pref_servants),
                                entries = vm.servants,
                                icon = vectorResource(R.drawable.ic_crown)
                            )

                            val prefServants by config.preferredServants.collect()

                            if (prefServants.isNotEmpty()) {
                                config.maxAscended.SwitchPreference(
                                    title = stringResource(R.string.p_battle_config_support_max_ascended),
                                    icon = vectorResource(R.drawable.ic_star)
                                )

                                Preference(
                                    title = { Text(stringResource(R.string.p_max_skills)) },
                                    icon = vectorResource(R.drawable.ic_wand),
                                    summary = {
                                        MaxSkills(
                                            skills = listOf(
                                                config.skill1Max,
                                                config.skill2Max,
                                                config.skill3Max
                                            )
                                        )
                                    }
                                )
                            }
                        }

                        PreferenceGroup(title = stringResource(R.string.p_battle_config_support_pref_ces)) {
                            config.preferredCEs.SupportSelectPreference(
                                title = stringResource(R.string.p_battle_config_support_pref_ces),
                                entries = vm.ces,
                                icon = vectorResource(R.drawable.ic_card)
                            )

                            val prefCEs by config.preferredCEs.collect()

                            if (prefCEs.isNotEmpty()) {
                                config.mlb.SwitchPreference(
                                    title = stringResource(R.string.p_battle_config_support_mlb),
                                    icon = vectorResource(R.drawable.ic_star)
                                )
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
        Row {
            fun skillText(max: Boolean) = if (max) "10" else "x"
            fun Pref<Boolean>.toggle() =
                set(!get())

            skills.forEachIndexed { index, pref ->
                if (index != 0) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .padding(top = 25.dp)
                            .padding(horizontal = 16.dp)
                    ) {
                        Text("/")
                    }
                }

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .padding(7.dp)
                        .padding(top = 9.dp)
                        .border(
                            width = 1.dp,
                            brush = SolidColor(MaterialTheme.colors.onBackground.copy(alpha = ContentAlpha.medium)),
                            shape = MaterialTheme.shapes.medium
                        )
                        .clickable { pref.toggle() }
                        .padding(8.dp)
                ) {
                    val max by pref.collect()
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
    icon: ImageVector? = null
) {
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
        Icon(
            imageVector = vectorResource(id = R.drawable.ic_close),
            modifier = Modifier
                .size(40.dp)
                .clickable(onClick = { resetToDefault() })
        )
    }
}