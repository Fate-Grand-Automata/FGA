package com.mathewsachin.fategrandautomata.ui.more

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.ui.icon
import com.mathewsachin.fategrandautomata.ui.prefs.Preference

@Composable
fun StorageGroup(
    directoryName: String,
    onPickDirectory: () -> Unit
) {
    Preference(
        title = stringResource(R.string.p_folder),
        summary = directoryName,
        icon = icon(R.drawable.ic_folder_edit),
        onClick = onPickDirectory
    )
}