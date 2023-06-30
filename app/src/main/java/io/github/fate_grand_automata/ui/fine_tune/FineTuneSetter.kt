package io.github.fate_grand_automata.ui.fine_tune

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import io.github.fate_grand_automata.R
import io.github.fate_grand_automata.ui.DimmedIcon
import io.github.fate_grand_automata.ui.FGAListItemColors
import io.github.fate_grand_automata.ui.FgaDialog
import io.github.fate_grand_automata.ui.Stepper
import io.github.fate_grand_automata.ui.icon
import io.github.fate_grand_automata.ui.prefs.remember

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
                headlineContent = { Text(stringResource(name)) },
                supportingContent = { Text(defaultString) },
                modifier = Modifier.weight(1f),
                colors = FGAListItemColors()
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