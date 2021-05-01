package com.mathewsachin.fategrandautomata.ui.fine_tune

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.IconButton
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.ui.DimmedIcon
import com.mathewsachin.fategrandautomata.ui.FgaDialog
import com.mathewsachin.fategrandautomata.ui.Stepper
import com.mathewsachin.fategrandautomata.ui.icon
import com.mathewsachin.fategrandautomata.ui.prefs.remember

@Composable
fun FineTuneItem.FineTuneSetter() {
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

    Column {
        Row {
            ListItem(
                text = { Text(stringResource(name)) },
                secondaryText = { Text(defaultString) },
                modifier = Modifier.weight(1f)
            )

            IconButton(
                onClick = { hintDialog.show() }
            ) {
                DimmedIcon(
                    icon(R.drawable.ic_info),
                    contentDescription = "Info"
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            var value by pref.remember()

            Stepper(
                value = value,
                onValueChange = { value = it },
                valueRange = valueRange,
                valueRepresentation = valueRepresentation
            )
        }
    }
}