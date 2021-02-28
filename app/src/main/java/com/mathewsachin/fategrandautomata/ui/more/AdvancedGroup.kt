package com.mathewsachin.fategrandautomata.ui.more

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.prefs.core.PrefsCore
import com.mathewsachin.fategrandautomata.ui.prefs.Preference
import com.mathewsachin.fategrandautomata.ui.prefs.PreferenceGroup
import com.mathewsachin.fategrandautomata.ui.prefs.SwitchPreference
import com.mathewsachin.fategrandautomata.ui.prefs.collect

@Composable
fun AdvancedGroup(
    prefs: PrefsCore,
    goToFineTune: () -> Unit
) {
    PreferenceGroup(title = stringResource(R.string.p_advanced)) {
        Preference(
            title = stringResource(R.string.p_fine_tune),
            icon = painterResource(R.drawable.ic_tune),
            onClick = goToFineTune
        )

        prefs.debugMode.SwitchPreference(
            title = stringResource(R.string.p_debug_mode),
            summary = stringResource(R.string.p_debug_mode_summary),
            icon = painterResource(R.drawable.ic_bug)
        )

        prefs.ignoreNotchCalculation.SwitchPreference(
            title = stringResource(R.string.p_ignore_notch),
            summary = stringResource(R.string.p_ignore_notch_summary),
            icon = painterResource(R.drawable.ic_notch)
        )

        val rootForScreenshots by prefs.useRootForScreenshots.collect()

        prefs.recordScreen.SwitchPreference(
            title = stringResource(R.string.p_record_screen),
            summary = stringResource(R.string.p_record_screen_summary),
            icon = painterResource(R.drawable.ic_video),
            enabled = !rootForScreenshots
        )

        prefs.useRootForScreenshots.SwitchPreference(
            title = stringResource(R.string.p_root_screenshot),
            summary = stringResource(R.string.p_root_screenshot_summary),
            icon = painterResource(R.drawable.ic_key)
        )

        prefs.autoStartService.SwitchPreference(
            title = stringResource(R.string.p_auto_start_service),
            icon = painterResource(R.drawable.ic_launch)
        )
    }
}