package com.mathewsachin.fategrandautomata.ui.battle_config_item

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
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
    preferredSummary: String,
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
            Preference(
                title = stringResource(R.string.p_support_mode_preferred),
                summary = preferredSummary,
                onClick = goToPreferred
            )
        }

        if (friendMode) {
            if (friendEntries.isNotEmpty()) {
                config.support.friendNames.SupportSelectPreference(
                    title = stringResource(R.string.p_battle_config_support_friend_names),
                    entries = friendEntries
                )
            } else {
                Preference(
                    icon = vectorResource(R.drawable.ic_info),
                    title = stringResource(R.string.p_battle_config_support_friend_names),
                    summary = stringResource(R.string.p_battle_config_support_friend_name_hint)
                )
            }
        }
    }
}