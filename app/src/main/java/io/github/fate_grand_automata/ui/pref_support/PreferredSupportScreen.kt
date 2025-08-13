package io.github.fate_grand_automata.ui.pref_support

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardDefaults.cardElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.fate_grand_automata.R
import io.github.fate_grand_automata.prefs.core.Pref
import io.github.fate_grand_automata.prefs.core.SupportPrefsCore
import io.github.fate_grand_automata.scripts.enums.BondCEEffectEnum
import io.github.fate_grand_automata.scripts.enums.CEMatchCountEnum
import io.github.fate_grand_automata.ui.Heading
import io.github.fate_grand_automata.ui.OnResume
import io.github.fate_grand_automata.ui.icon
import io.github.fate_grand_automata.ui.prefs.Preference
import io.github.fate_grand_automata.ui.prefs.PreferenceGroupHeader
import io.github.fate_grand_automata.ui.prefs.SingleSelectChipPreference
import io.github.fate_grand_automata.ui.prefs.SwitchPreference
import io.github.fate_grand_automata.ui.prefs.remember
import io.github.fate_grand_automata.util.stringRes
import kotlinx.coroutines.Dispatchers
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
        scope.launch(Dispatchers.IO) {
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
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
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

                        config.grandServant.SwitchPreference(
                            title = stringResource(R.string.p_battle_config_support_grand_servant)
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
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
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
                        entries = vm.ces + mapOf(
                            BondCEEffectEnum.Default.value to stringResource(R.string.p_battle_config_support_pref_bond_ce_effect_default),
                            BondCEEffectEnum.NP.value to stringResource(R.string.p_battle_config_support_pref_bond_ce_effect_np)
                        )
                    )

                    if (prefCEs.isNotEmpty()) {
                        config.mlb.SwitchPreference(
                            title = stringResource(R.string.p_battle_config_support_mlb)
                        )
                        Row {
                            config.ceMatchCount.SingleSelectChipPreference(
                                title = stringResource(R.string.p_battle_config_support_ce_match_count),
                                entries = listOf(
                                    CEMatchCountEnum.One,
                                    CEMatchCountEnum.Two,
                                    CEMatchCountEnum.Three
                                ).associateWith { stringResource(it.stringRes) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Column {
                    PreferenceGroupHeader(
                        title = stringResource(R.string.p_support_mode_friend)
                    )

                    config.friendsOnly.SwitchPreference(
                        title = stringResource(R.string.p_battle_config_support_friends_only)
                    )

                    val friendsOnly by config.friendsOnly.remember()

                    AnimatedVisibility (friendsOnly) {
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
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
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
        Text(
            """
                1. You can add more images using 'Support Image Maker' script by clicking the PLAY button on support or friend list screens.
                
                2. For event CEs, it is better to use the in-game filters in FGO for CEs and MLB setting.
                """.trimIndent(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Light
        )
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
                    MaterialTheme.colorScheme.secondary
                else MaterialTheme.colorScheme.surfaceVariant

            val foregroundColor =
                if (max)
                    MaterialTheme.colorScheme.onSecondary
                else MaterialTheme.colorScheme.onSurfaceVariant

            Card(
                elevation = cardElevation(5.dp),
                colors = CardDefaults.cardColors(
                    containerColor = backgroundColor,
                    contentColor = foregroundColor
                )
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