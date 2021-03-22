package com.mathewsachin.fategrandautomata.ui.more

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.Checkbox
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.prefs.core.PrefsCore
import com.mathewsachin.fategrandautomata.ui.icon
import com.mathewsachin.fategrandautomata.ui.prefs.Preference
import com.mathewsachin.fategrandautomata.ui.prefs.PreferenceGroupHeader
import com.mathewsachin.fategrandautomata.ui.prefs.SeekBarPreference

fun LazyListScope.WaitForAPRegenGroup(
    prefs: PrefsCore,
    waitEnabled: Boolean,
    onWaitEnabledChange: (Boolean) -> Unit
) {
    item {
        PreferenceGroupHeader(
            title = stringResource(R.string.p_wait_ap_regen_text)
        )
    }

    item {
        Row {
            Checkbox(
                checked = waitEnabled,
                onCheckedChange = { onWaitEnabledChange(it) },
                modifier = Modifier
                    .padding(start = 16.dp)
                    .align(Alignment.CenterVertically)
                    .size(40.dp)
            )

            Preference(
                title = stringResource(R.string.p_wait_ap_regen_text),
                hint = stringResource(R.string.p_wait_ap_regen_text_summary),
                enabled = waitEnabled
            )
        }
    }

    if (waitEnabled) {
        item {
            prefs.waitAPRegenMinutes.SeekBarPreference(
                title = stringResource(R.string.p_wait_ap_regen_minutes_text),
                summary = stringResource(R.string.p_wait_ap_regen_minutes_text_summary),
                valueRange = 1..60,
                icon = icon(R.drawable.ic_counter),
                valueRepresentation = { "$it min" }
            )
        }
    }
}