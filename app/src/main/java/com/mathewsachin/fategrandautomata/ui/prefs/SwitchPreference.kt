package com.mathewsachin.fategrandautomata.ui.prefs

import androidx.compose.material.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.mathewsachin.fategrandautomata.prefs.core.Pref
import com.mathewsachin.fategrandautomata.ui.VectorIcon

@Composable
fun Pref<Boolean>.SwitchPreference(
    title: String,
    summary: String = "",
    singleLineTitle: Boolean = true,
    icon: VectorIcon? = null,
    enabled: Boolean = true
) {
    var state by remember()

    Preference(
        title = title,
        summary = summary,
        singleLineTitle = singleLineTitle,
        icon = icon,
        enabled = enabled,
        onClick = { state = !state }
    ) {
        Switch(
            checked = state,
            onCheckedChange = { state = it },
            enabled = enabled
        )
    }
}