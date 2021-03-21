package com.mathewsachin.fategrandautomata.ui.more

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.prefs.core.PrefsCore
import com.mathewsachin.fategrandautomata.ui.icon
import com.mathewsachin.fategrandautomata.ui.prefs.Preference
import com.mathewsachin.fategrandautomata.ui.prefs.SwitchPreference
import com.mathewsachin.fategrandautomata.ui.prefs.remember

fun LazyListScope.advancedGroup(
    prefs: PrefsCore,
    goToFineTune: () -> Unit
) {
    item {
        Preference(
            title = stringResource(R.string.p_fine_tune),
            icon = icon(R.drawable.ic_tune),
            onClick = goToFineTune
        )
    }

    item {
        prefs.debugMode.SwitchPreference(
            title = stringResource(R.string.p_debug_mode),
            summary = stringResource(R.string.p_debug_mode_summary),
            icon = icon(R.drawable.ic_bug)
        )
    }

    item {
        prefs.ignoreNotchCalculation.SwitchPreference(
            title = stringResource(R.string.p_ignore_notch),
            summary = stringResource(R.string.p_ignore_notch_summary),
            icon = icon(R.drawable.ic_notch)
        )
    }

    item {
        val rootForScreenshots by prefs.useRootForScreenshots.remember()

        prefs.recordScreen.SwitchPreference(
            title = stringResource(R.string.p_record_screen),
            summary = stringResource(R.string.p_record_screen_summary),
            icon = icon(R.drawable.ic_video),
            enabled = !rootForScreenshots
        )
    }

    item {
        prefs.useRootForScreenshots.SwitchPreference(
            title = stringResource(R.string.p_root_screenshot),
            summary = stringResource(R.string.p_root_screenshot_summary),
            icon = icon(R.drawable.ic_key)
        )
    }

    item {
        prefs.autoStartService.SwitchPreference(
            title = stringResource(R.string.p_auto_start_service),
            icon = icon(R.drawable.ic_launch)
        )
    }
}