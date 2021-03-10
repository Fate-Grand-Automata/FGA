package com.mathewsachin.fategrandautomata.ui.battle_config_item

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.prefs.core.BattleConfigCore
import com.mathewsachin.fategrandautomata.scripts.enums.SupportClass
import com.mathewsachin.fategrandautomata.scripts.enums.SupportSelectionModeEnum
import com.mathewsachin.fategrandautomata.ui.pref_support.SupportSelectPreference
import com.mathewsachin.fategrandautomata.ui.prefs.ListPreference
import com.mathewsachin.fategrandautomata.ui.prefs.Preference
import com.mathewsachin.fategrandautomata.ui.prefs.PreferenceGroup
import com.mathewsachin.fategrandautomata.ui.prefs.collect
import com.mathewsachin.fategrandautomata.util.stringRes

@Composable
fun SupportGroup(
    config: BattleConfigCore,
    maxSkillText: String,
    friendEntries: Map<String, String>,
    goToPreferred: () -> Unit
) {
    PreferenceGroup(title = stringResource(R.string.p_battle_config_support)) {
        config.support.supportClass.ListPreference(
            title = stringResource(R.string.p_battle_config_support_class),
            entries = SupportClass.values()
                .associateWith { stringResource(it.stringRes) }
        )

        val supportMode by config.support.selectionMode.collect()
        val preferredMode = supportMode == SupportSelectionModeEnum.Preferred
        val friendMode = supportMode == SupportSelectionModeEnum.Friend

        Row {
            Box(
                modifier = Modifier.weight(1f)
            ) {
                config.support.selectionMode.ListPreference(
                    title = stringResource(R.string.p_battle_config_support_selection_mode),
                    entries = SupportSelectionModeEnum.values()
                        .associateWith { stringResource(it.stringRes) }
                )
            }

            if (preferredMode || friendMode) {
                Box(
                    modifier = Modifier.weight(1f)
                ) {
                    config.support.fallbackTo.ListPreference(
                        title = stringResource(R.string.p_battle_config_support_fallback_selection_mode),
                        entries = listOf(
                            SupportSelectionModeEnum.First,
                            SupportSelectionModeEnum.Manual
                        ).associateWith { stringResource(it.stringRes) }
                    )
                }
            }
        }

        if (preferredMode) {
            val servants by config.support.preferredServants.collect()
            val ces by config.support.preferredCEs.collect()

            Preference(
                title = { Text(stringResource(R.string.p_support_mode_preferred)) },
                summary = {
                    PreferredSummary(
                        config = config,
                        maxSkillText = maxSkillText,
                        servants = servants,
                        ces = ces
                    )
                },
                onClick = goToPreferred
            )

            if (servants.isEmpty() && ces.isEmpty()) {
                PreferenceError(
                    stringResource(R.string.support_selection_preferred_not_set)
                )
            }
        }

        if (friendMode) {
            if (friendEntries.isNotEmpty()) {
                config.support.friendNames.SupportSelectPreference(
                    title = stringResource(R.string.p_battle_config_support_friend_names),
                    entries = friendEntries
                )
            } else {
                Preference(
                    icon = painterResource(R.drawable.ic_info),
                    title = stringResource(R.string.p_battle_config_support_friend_names),
                    summary = stringResource(R.string.p_battle_config_support_friend_name_hint)
                )
            }

            val friendNames by config.support.friendNames.collect()

            if (friendNames.isEmpty()) {
                PreferenceError(
                    stringResource(R.string.support_selection_friend_not_set)
                )
            }
        }
    }
}

@Composable
fun PreferredSummary(
    config: BattleConfigCore,
    maxSkillText: String,
    servants: Set<String>,
    ces: Set<String>
) {
    Column(
        modifier = Modifier
            .padding(vertical = 5.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(vertical = 2.dp)
        ) {
            Icon(
                painterResource(R.drawable.ic_crown),
                contentDescription = "crown"
            )

            val text = if (servants.isNotEmpty())
                servants.joinToString()
            else stringResource(R.string.battle_config_support_any)

            Text(
                text,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            )

            if (servants.isNotEmpty()) {
                Text(
                    maxSkillText,
                    style = MaterialTheme.typography.caption,
                    modifier = Modifier
                        .border(
                            0.5.dp,
                            MaterialTheme.colors.onSurface,
                            MaterialTheme.shapes.small
                        )
                        .padding(5.dp, 1.dp)
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(vertical = 2.dp)
        ) {
            Icon(
                painterResource(R.drawable.ic_card),
                contentDescription = "card"
            )

            val text = if (ces.isNotEmpty())
                ces.joinToString()
            else stringResource(R.string.battle_config_support_any)

            Text(
                text,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
            )

            if (ces.isNotEmpty()) {
                val mlb by config.support.mlb.collect()

                if (mlb) {
                    Icon(
                        painterResource(R.drawable.ic_star),
                        contentDescription = "MLB",
                        tint = MaterialTheme.colors.secondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}