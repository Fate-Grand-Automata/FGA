package com.mathewsachin.fategrandautomata.ui.prefs.compose

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.savedinstancestate.savedInstanceState
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.mathewsachin.fategrandautomata.prefs.core.Pref
import kotlinx.coroutines.flow.onEach

@Composable
fun Pref<Int>.EditNumberPreference(
    title: String,
    singleLineTitle: Boolean = false,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    hint: String = "",
    min: Int = 0
) {
    var current by savedInstanceState { defaultValue.toString() }
    val state by asFlow()
        .onEach { current = it.toString() }
        .collectAsState(defaultValue)
    var showDialog by savedInstanceState { false }
    val closeDialog = { showDialog = false }

    Preference(
        title = title,
        summary = state.toString(),
        singleLineTitle = singleLineTitle,
        icon = icon,
        enabled = enabled,
        hint = hint,
        onClick = { showDialog = true },
    )

    if (showDialog) {
        val commit = {
            set(current.toIntOrNull()?.coerceAtLeast(min) ?: defaultValue)
            closeDialog()
        }

        AlertDialog(
            onDismissRequest = { closeDialog() },
            title = { Text(text = title) },
            text = {
                TextField(
                    value = current,
                    onValueChange = { current = it },
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done,
                        keyboardType = KeyboardType.Number
                    ),
                    singleLine = true,
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
                    onClick = { closeDialog() },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colors.secondary),
                ) {
                    Text(text = stringResource(id = android.R.string.cancel))
                }
            }
        )
    }
}

@Composable
fun Pref<String>.EditTextPreference(
    title: String,
    singleLineTitle: Boolean = false,
    singleLine: Boolean = false,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    hint: String = ""
) {
    var current by savedInstanceState { defaultValue }
    val state by asFlow()
        .onEach { current = it }
        .collectAsState(defaultValue)
    var showDialog by savedInstanceState { false }
    val closeDialog = { showDialog = false }

    Preference(
        title = title,
        summary = state,
        singleLineTitle = singleLineTitle,
        icon = icon,
        enabled = enabled,
        hint = hint,
        onClick = { showDialog = true },
    )

    if (showDialog) {
        val commit = {
            set(current)
            closeDialog()
        }

        AlertDialog(
            onDismissRequest = { closeDialog() },
            title = { Text(text = title) },
            text = {
                TextField(
                    value = current,
                    onValueChange = { current = it },
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
                    onClick = {
                        set(current)
                        closeDialog()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colors.secondary),
                ) {
                    Text(text = stringResource(id = android.R.string.ok))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { closeDialog() },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colors.secondary),
                ) {
                    Text(text = stringResource(id = android.R.string.cancel))
                }
            }
        )
    }
}