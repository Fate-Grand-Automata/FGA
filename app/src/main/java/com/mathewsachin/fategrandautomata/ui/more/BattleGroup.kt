package com.mathewsachin.fategrandautomata.ui.more

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.prefs.core.PrefsCore
import com.mathewsachin.fategrandautomata.scripts.enums.GameServerEnum
import com.mathewsachin.fategrandautomata.ui.prefs.ListPreference
import com.mathewsachin.fategrandautomata.ui.prefs.PreferenceGroup
import com.mathewsachin.fategrandautomata.ui.prefs.SwitchPreference

@Composable
fun BattleGroup(
    prefs: PrefsCore
) {
    PreferenceGroup(title = stringResource(R.string.p_script_mode_battle)) {
        prefs.skillConfirmation.SwitchPreference(
            title = stringResource(R.string.p_skill_confirmation),
            icon = painterResource(R.drawable.ic_radio)
        )

        prefs.gameServerRaw.ListPreference(
            title = stringResource(R.string.p_game_server),
            icon = painterResource(R.drawable.ic_earth),
            entries =
            mapOf(PrefsCore.GameServerAutoDetect to stringResource(R.string.p_game_server_auto_detect))
                .plus(
                    GameServerEnum.values().associate {
                        it.name to stringResource(it.displayStringRes)
                    }
                )
        )

        prefs.storySkip.SwitchPreference(
            title = stringResource(R.string.p_story_skip),
            icon = painterResource(R.drawable.ic_fast_forward)
        )

        prefs.withdrawEnabled.SwitchPreference(
            title = stringResource(R.string.p_enable_withdraw),
            icon = painterResource(R.drawable.ic_exit_run)
        )

        prefs.stopOnCEDrop.SwitchPreference(
            title = stringResource(R.string.p_stop_on_ce_drop),
            icon = painterResource(R.drawable.ic_card)
        )

        prefs.stopOnCEGet.SwitchPreference(
            title = stringResource(R.string.p_stop_on_ce_get),
            summary = stringResource(R.string.p_stop_on_ce_get_summary),
            icon = painterResource(R.drawable.ic_card)
        )

        prefs.screenshotDrops.SwitchPreference(
            title = stringResource(R.string.p_screenshot_drops),
            summary = stringResource(R.string.p_screenshot_drops_summary),
            icon = painterResource(R.drawable.ic_screenshot)
        )

        prefs.boostItemSelectionMode.ListPreference(
            title = stringResource(R.string.p_boost_item),
            icon = painterResource(R.drawable.ic_boost),
            entries = (-1..3).associateWith { it.boostItemString() }
        )
    }
}