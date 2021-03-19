package com.mathewsachin.fategrandautomata.ui.fine_tune

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.ui.prefs.SeekBarPreference
import com.vanpra.composematerialdialogs.MaterialDialog

@Composable
fun FineTuneItem.FineTuneSeekBar() {
    val defaultString = "Default: ${valueRepresentation(pref.defaultValue)}"

    val hintDialog = MaterialDialog()
    hintDialog.build {
        iconTitle(
            textRes = name,
            icon = {
                Icon(
                    icon.asPainter(),
                    contentDescription = "icon"
                )
            }
        )

        message("$defaultString\n\n$hint")

        buttons {
            negativeButton(res = android.R.string.cancel)
            // TODO: Localize 'Reset to default'
            positiveButton("Reset to default") {
                reset()
            }
        }
    }

    Row {
        Box(
            modifier = Modifier.weight(1f)
        ) {
            pref.SeekBarPreference(
                title = stringResource(name),
                summary = defaultString,
                valueRange = valueRange,
                valueRepresentation = valueRepresentation,
                state = state
            )
        }

        Icon(
            painterResource(R.drawable.ic_info),
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