package com.mathewsachin.fategrandautomata.ui.more

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.Card
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.prefs.core.GameAreaMode
import com.mathewsachin.fategrandautomata.prefs.core.Pref
import com.mathewsachin.fategrandautomata.prefs.core.PrefsCore
import com.mathewsachin.fategrandautomata.root.SuperUser
import com.mathewsachin.fategrandautomata.ui.Stepper
import com.mathewsachin.fategrandautomata.ui.icon
import com.mathewsachin.fategrandautomata.ui.prefs.ListPreference
import com.mathewsachin.fategrandautomata.ui.prefs.Preference
import com.mathewsachin.fategrandautomata.ui.prefs.SwitchPreference
import com.mathewsachin.fategrandautomata.ui.prefs.remember
import com.mathewsachin.fategrandautomata.util.stringRes

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
        RootForScreenshots(prefs.useRootForScreenshots)
    }

    item {
        prefs.autoStartService.SwitchPreference(
            title = stringResource(R.string.p_auto_start_service),
            icon = icon(R.drawable.ic_launch)
        )
    }

    item {
        Column {
            prefs.gameAreaMode.ListPreference(
                title = stringResource(R.string.p_game_area_mode),
                icon = icon(Icons.Default.Fullscreen),
                entries = GameAreaMode.values()
                    .associateWith { stringResource(it.stringRes) }
            )

            val gameAreaMode by prefs.gameAreaMode.remember()

            AnimatedVisibility(gameAreaMode == GameAreaMode.Custom) {
                Card(
                    modifier = Modifier.padding(5.dp),
                    elevation = 5.dp
                ) {
                    Column(
                        modifier = Modifier.scale(0.9f)
                    ) {
                        prefs.gameOffsetLeft.customOffset(stringResource(R.string.p_game_area_custom_left))
                        prefs.gameOffsetRight.customOffset(stringResource(R.string.p_game_area_custom_right))
                        prefs.gameOffsetTop.customOffset(stringResource(R.string.p_game_area_custom_top))
                        prefs.gameOffsetBottom.customOffset(stringResource(R.string.p_game_area_custom_bottom))
                    }
                }
            }
        }
    }

    item {
        prefs.stageCounterNew.SwitchPreference(
            title = stringResource(R.string.p_thresholded_stage_counter),
            icon = icon(R.drawable.ic_counter)
        )
    }
}

@Composable
private fun Pref<Int>.customOffset(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        var value by remember()

        Text(text)
        Stepper(
            value = value,
            onValueChange = { value = it },
            valueRange = 0..999
        )
    }
}

private fun hasRootAccess() = try {
    SuperUser().close()
    true
} catch (e: Exception) {
    false
}

@Composable
private fun RootForScreenshots(
    pref: Pref<Boolean>
) {
    var state by pref.remember()
    var enabled by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf(false) }

    val action: (Boolean) -> Unit = {
        error = false
        enabled = false
        try {
            when {
                !it -> state = false
                hasRootAccess() -> state = true
                else -> error = true
            }
        } finally {
            enabled = true
        }
    }

    Column {
        Preference(
            title = stringResource(R.string.p_root_screenshot),
            summary = stringResource(if (error) R.string.root_failed else R.string.p_root_screenshot_summary),
            icon = icon(R.drawable.ic_key),
            enabled = enabled,
            onClick = { action(!state) },
        ) {
            Switch(
                checked = state,
                onCheckedChange = action,
                enabled = enabled
            )
        }
    }
}