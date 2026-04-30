package io.github.fate_grand_automata.ui.more

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import io.github.fate_grand_automata.R
import io.github.fate_grand_automata.ui.icon
import io.github.fate_grand_automata.ui.prefs.Preference

@Composable
fun StorageGroup(
    directoryName: String,
    onPickDirectory: () -> Unit,
    extractSupportImages: () -> Unit,
    extractSummary: String,
) {
    Preference(
        title = stringResource(R.string.p_folder),
        summary = directoryName,
        icon = icon(R.drawable.ic_folder_edit),
        onClick = onPickDirectory,
    )

    Preference(
        title = stringResource(R.string.support_menu_extract_default_support_images),
        icon = icon(Icons.Default.Image),
        onClick = extractSupportImages,
        summary = extractSummary,
    )
}
