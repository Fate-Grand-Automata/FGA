package com.mathewsachin.fategrandautomata.ui.more

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.prefs.core.PrefsCore
import com.mathewsachin.fategrandautomata.scripts.enums.GameServerEnum
import com.mathewsachin.fategrandautomata.ui.icon
import com.mathewsachin.fategrandautomata.ui.prefs.SingleSelectChipPreference
import com.mathewsachin.fategrandautomata.ui.prefs.SwitchPreference
import com.mathewsachin.fategrandautomata.util.stringRes

fun LazyListScope.battleGroup(
    prefs: PrefsCore
) {
    item {
        prefs.skillConfirmation.SwitchPreference(
            title = stringResource(R.string.p_skill_confirmation),
            icon = icon(Icons.Default.RadioButtonChecked)
        )
    }

    item {
        prefs.gameServerRaw.SingleSelectChipPreference(
            title = stringResource(R.string.p_game_server),
            icon = icon(Icons.Default.Public),
            entries =
            mapOf(PrefsCore.GameServerAutoDetect to stringResource(R.string.p_game_server_auto_detect))
                .plus(
                    GameServerEnum.values().associate {
                        it.name to stringResource(it.stringRes)
                    }
                )
        )
    }

    item {
        prefs.storySkip.SwitchPreference(
            title = stringResource(R.string.p_story_skip),
            icon = icon(Icons.Default.FastForward)
        )
    }

    item {
        prefs.withdrawEnabled.SwitchPreference(
            title = stringResource(R.string.p_enable_withdraw),
            icon = icon(R.drawable.ic_exit_run)
        )
    }

    item {
        prefs.stopOnCEGet.SwitchPreference(
            title = stringResource(R.string.p_stop_on_ce_get),
            summary = stringResource(R.string.p_stop_on_ce_get_summary),
            icon = icon(R.drawable.ic_card)
        )
    }

    item {
        prefs.stopOnFirstClearRewards.SwitchPreference(
            title = stringResource(R.string.p_stop_on_first_clear_rewards),
            icon = icon(R.drawable.ic_gift)
        )
    }

    item {
        prefs.screenshotDrops.SwitchPreference(
            title = stringResource(R.string.p_screenshot_drops),
            summary = stringResource(R.string.p_screenshot_drops_summary),
            icon = icon(R.drawable.ic_screenshot)
        )
    }

    item {
        prefs.screenshotDropsUnmodified.SwitchPreference(
            title = stringResource(R.string.p_screenshot_drops_unmodified),
            summary = stringResource(R.string.p_screenshot_drops_unmodified_summary),
            icon = icon(R.drawable.ic_screenshot)
        )
    }

    item {
        prefs.boostItemSelectionMode.SingleSelectChipPreference(
            title = stringResource(R.string.p_boost_item),
            icon = icon(Icons.Default.OfflineBolt),
            entries = (-1..3).associateWith { it.boostItemString() }
        )
    }

    item {
        prefs.skipServantFaceCardCheck.SwitchPreference(
            title = stringResource(R.string.p_skip_servant_face_checks),
            summary = stringResource(R.string.p_skip_servant_face_checks_summary),
            icon = icon(Icons.Default.NoAccounts)
        )
    }
}

@Composable
private fun Int.boostItemString() = when (this) {
    -1 -> stringResource(R.string.p_boost_item_disabled)
    0 -> stringResource(R.string.p_boost_item_skip)
    else -> toString()
}
