package com.mathewsachin.fategrandautomata.ui.pref_support

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.prefs.core.Pref
import com.mathewsachin.fategrandautomata.prefs.core.SupportPrefsCore
import com.mathewsachin.fategrandautomata.ui.Heading
import com.mathewsachin.fategrandautomata.ui.OnResume
import com.mathewsachin.fategrandautomata.ui.icon
import com.mathewsachin.fategrandautomata.ui.prefs.Preference
import com.mathewsachin.fategrandautomata.ui.prefs.PreferenceGroupHeader
import com.mathewsachin.fategrandautomata.ui.prefs.SwitchPreference
import com.mathewsachin.fategrandautomata.ui.prefs.remember
import kotlinx.coroutines.launch

@Composable
fun PreferredSupportScreen(
    vm: PreferredSupportViewModel = viewModel(),
    supportVm: SupportViewModel = viewModel()
) {
    PreferredSupport(
        config = vm.supportPrefs,
        vm = supportVm
    )

    val context = LocalContext.current

    val scope = rememberCoroutineScope()

    OnResume {
        scope.launch {
            if (supportVm.shouldExtractSupportImages) {
                supportVm.performSupportImageExtraction(context)
            } else supportVm.refresh(context)
        }
    }
}

@Composable
private fun PreferredSupport(
    config: SupportPrefsCore,
    vm: SupportViewModel
) {
    val prefServants by config.preferredServants.remember()
    val prefCEs by config.preferredCEs.remember()

    LazyColumn {
        item {
            Heading(stringResource(R.string.p_support_mode_preferred))
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

        item {
            Card(
                modifier = Modifier
                    .padding(16.dp)
            ) {
                Column {
                    PreferenceGroupHeader(
                        title = stringResource(R.string.p_support_mode_friend)
                    )

                    config.requireFriends.SwitchPreference(
                        title = stringResource(R.string.p_battle_config_support_friends_only)
                    )

                    val requireFriends by config.requireFriends.remember()

                    AnimatedVisibility (requireFriends) {
                        if (vm.friends.isNotEmpty()) {
                            config.friendNames.SupportSelectPreference(
                                title = stringResource(R.string.p_battle_config_support_friend_names),
                                entries = vm.friends
                            )
                        } else {
                            Preference(
                                icon = icon(R.drawable.ic_info),
                                title = stringResource(R.string.p_battle_config_support_friend_names),
                                summary = stringResource(R.string.p_battle_config_support_friend_name_hint)
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
                PreferredSupportHelp()
            }
        }
    }
}

@Composable
private fun PreferredSupportHelp() {
    Column(
        modifier = Modifier
            .padding(16.dp)
    ) {
        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
            Text(
                """
                1. You can add more images using 'Support Image Maker' script by clicking the PLAY button on support or friend list screens.
                
                2. For event CEs, it is better to use the in-game filters in FGO for CEs and MLB setting.
                """.trimIndent(),
                style = MaterialTheme.typography.body2
            )
        }
    }
}

@Composable
private fun MaxSkills(
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