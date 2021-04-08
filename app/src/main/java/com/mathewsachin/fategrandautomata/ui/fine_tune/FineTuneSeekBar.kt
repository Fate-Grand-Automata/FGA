package com.mathewsachin.fategrandautomata.ui.fine_tune

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.ui.DimmedIcon
import com.mathewsachin.fategrandautomata.ui.FgaDialog
import com.mathewsachin.fategrandautomata.ui.icon
import com.mathewsachin.fategrandautomata.ui.prefs.SeekBarPreference

@Composable
fun FineTuneItem.FineTuneSeekBar() {
    // TODO: Localize
    val defaultString = "Default: ${valueRepresentation(pref.defaultValue)}"

    val hintDialog = FgaDialog()
    hintDialog.build {
        title(
            text = stringResource(name),
            icon = icon
        )

        message("$defaultString\n\n$hint")

        buttons(
            onSubmit = { reset() },
            // TODO: Localize 'Reset to default'
            okLabel = "Reset to default"
        )
    }

    Row {
        pref.SeekBarPreference(
            title = stringResource(name),
            summary = defaultString,
            valueRange = valueRange,
            valueRepresentation = valueRepresentation,
            state = state,
            modifier = Modifier.weight(1f)
        )

        DimmedIcon(
            icon(R.drawable.ic_info),
            contentDescription = "Info",
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .padding(end = 16.dp)
                .size(40.dp)
                .clickable(onClick = { hintDialog.show() })
                .padding(7.dp)
        )
    }
}