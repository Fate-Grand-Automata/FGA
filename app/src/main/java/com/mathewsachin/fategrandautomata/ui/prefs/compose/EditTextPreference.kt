package com.mathewsachin.fategrandautomata.ui.prefs.compose

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.savedinstancestate.savedInstanceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.mathewsachin.fategrandautomata.prefs.core.Pref
import kotlinx.coroutines.flow.onEach

@Composable
fun EditTextPreferenceDialog(
    title: String,
    showDialog: Boolean,
    value: String,
    valueChange: (String) -> Unit,
    closeDialog: () -> Unit,
    keyboardOptions: KeyboardOptions = KeyboardOptions(),
    singleLine: Boolean = true
) {
    if (showDialog) {
        var current by savedInstanceState { value }

        val commit = {
            valueChange(current)
            closeDialog()
        }

        AlertDialog(
            onDismissRequest = closeDialog,
            title = { Text(text = title) },
            text = {
                TextField(
                    value = current,
                    onValueChange = { current = it },
                    keyboardOptions = keyboardOptions,
                    singleLine = singleLine,
                    onImeActionPerformed = { action, _ ->
                        if (action == ImeAction.Done) {
                            commit()
                        }
                    }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { commit() },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colors.secondary),
                ) {
                    Text(text = stringResource(id = android.R.string.ok))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = closeDialog,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colors.secondary),
                ) {
                    Text(text = stringResource(id = android.R.string.cancel))
                }
            }
        )
    }
}

@Composable
fun Pref<Int>.EditNumberPreference(
    title: String,
    singleLineTitle: Boolean = false,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    hint: String = "",
    min: Int = 0,
    max: Int = Int.MAX_VALUE,
    summary: (Int) -> String = { it.toString() },
    dialogTitle: String? = null
) {
    val state by collect()
    var showDialog by savedInstanceState { false }

    Preference(
        title = title,
        summary = summary(state),
        singleLineTitle = singleLineTitle,
        icon = icon,
        enabled = enabled,
        hint = hint,
        onClick = { showDialog = true }
    )

    EditTextPreferenceDialog(
        title = dialogTitle ?: title,
        showDialog = showDialog,
        value = state.toString(),
        valueChange = { set(it.toIntOrNull()?.coerceIn(min, max) ?: defaultValue) },
        closeDialog = { showDialog = false },
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Done,
            keyboardType = KeyboardType.Number
        ),
        singleLine = true
    )
}

@Composable
fun Pref<String>.EditTextPreference(
    title: String,
    singleLineTitle: Boolean = false,
    singleLine: Boolean = false,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    hint: String = "",
    summary: (String) -> String = { it },
    dialogTitle: String? = null
) {
    val state by collect()
    var showDialog by savedInstanceState { false }

    Preference(
        title = title,
        summary = summary(state),
        singleLineTitle = singleLineTitle,
        icon = icon,
        enabled = enabled,
        hint = hint,
        onClick = { showDialog = true },
    )

    EditTextPreferenceDialog(
        title = dialogTitle ?: title,
        showDialog = showDialog,
        value = state,
        valueChange = { set(it) },
        closeDialog = { showDialog = false },
        singleLine = singleLine
    )
}