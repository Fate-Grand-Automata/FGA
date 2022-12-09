package com.mathewsachin.fategrandautomata.ui.pref_support

import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.prefs.core.Pref
import com.mathewsachin.fategrandautomata.ui.DimmedIcon
import com.mathewsachin.fategrandautomata.ui.VectorIcon
import com.mathewsachin.fategrandautomata.ui.icon
import com.mathewsachin.fategrandautomata.ui.prefs.MultiSelectListPreference
import com.mathewsachin.fategrandautomata.ui.prefs.remember

@Composable
fun Pref<Set<String>>.SupportSelectPreference(
    title: String,
    entries: Map<String, String>,
    icon: VectorIcon? = null
) {
    val value by remember()

    MultiSelectListPreference(
        title = title,
        entries = entries,
        icon = icon,
        summary = {
            if (it.isEmpty())
                stringResource(R.string.p_not_set)
            else it.joinToString()
        }
    ) {
        if (value.isNotEmpty()) {
            IconButton(
                onClick = { resetToDefault() }
            ) {
                DimmedIcon(
                    icon(R.drawable.ic_close),
                    contentDescription = "Clear"
                )
            }
        }
    }
}