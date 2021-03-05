package com.mathewsachin.fategrandautomata.ui.prefs

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.prefs.core.Pref
import com.vanpra.composematerialdialogs.MaterialDialog

@Composable
fun editTextDialog(
    title: String,
    value: String,
    valueChange: (String) -> Unit,
    validate: (String) -> Boolean = { true },
    errorMessage: String = "",
    keyboardOptions: KeyboardOptions = KeyboardOptions()
): MaterialDialog {
    val dialog = MaterialDialog()

    dialog.build {
        title(text = title)

        input(
            label = title,
            keyboardOptions = keyboardOptions,
            prefill = value,
            onInput = { valueChange(it) },
            isTextValid = validate,
            errorMessage = errorMessage
        )

        buttons {
            positiveButton(res = android.R.string.ok)
            negativeButton(res = android.R.string.cancel)
        }
    }

    return dialog
}

@Composable
fun PreferenceTextEditor(
    label: String,
    prefill: String,
    onSubmit: (String) -> Unit,
    onCancel: () -> Unit,
    validate: (String) -> Boolean = { true },
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    var textFieldValue by remember(prefill) {
        mutableStateOf(
            TextFieldValue(
                prefill,
                selection = TextRange(prefill.length)
            )
        )
    }
    val valid = remember(textFieldValue) { validate(textFieldValue.text) }

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        val focusRequester = remember { FocusRequester() }

        TextField(
            value = textFieldValue,
            onValueChange = { textFieldValue = it },
            label = { Text(label, color = MaterialTheme.colors.onBackground.copy(0.8f)) },
            modifier = Modifier
                .focusRequester(focusRequester)
                .weight(1f),
            isError = !valid,
            keyboardOptions = keyboardOptions,
            textStyle = TextStyle(MaterialTheme.colors.onBackground, fontSize = 16.sp),
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = Color.Transparent
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    if (valid) {
                        onSubmit(textFieldValue.text)
                    }
                }
            )
        )

        SideEffect {
            focusRequester.requestFocus()
        }

        IconButton(
            onClick = onCancel,
        ) {
            Icon(
                painterResource(R.drawable.ic_close),
                contentDescription = stringResource(android.R.string.cancel),
                tint = MaterialTheme.colors.error
            )
        }

        IconButton(
            onClick = { onSubmit(textFieldValue.text) },
            enabled = valid
        ) {
            StatusWrapper(enabled = valid) {
                Icon(
                    painterResource(R.drawable.ic_check),
                    contentDescription = stringResource(android.R.string.ok)
                )
            }
        }
    }
}

@Composable
fun Pref<String>.EditTextPreference(
    title: String,
    singleLineTitle: Boolean = false,
    singleLine: Boolean = false,
    icon: Painter? = null,
    enabled: Boolean = true,
    hint: String = "",
    summary: (String) -> String = { it },
    validate: (String) -> Boolean = { true}
) {
    val state by collect()
    var editing by remember { mutableStateOf(false) }

    val keyboardOptions = KeyboardOptions(
        imeAction = if (singleLine) ImeAction.Done else ImeAction.Default
    )

    if (editing) {
        PreferenceTextEditor(
            label = title,
            prefill = state,
            validate = validate,
            onSubmit = {
                set(it)
                editing = false
            },
            onCancel = { editing = false },
            keyboardOptions = keyboardOptions
        )
    }
    else {
        Preference(
            title = title,
            summary = summary(state),
            singleLineTitle = singleLineTitle,
            icon = icon,
            enabled = enabled,
            hint = hint,
            onClick = { editing = true }
        )
    }
}

fun MaterialDialog.setPositiveEnabled(index: Int, value: Boolean) {
    // Have to make temp list in order for state to register change
    synchronized(positiveEnabled) {
        val tempList = positiveEnabled.toMutableList()
        tempList[index] = value
        positiveEnabled = tempList
    }
}

/**
 * @brief Adds an input field with the given parameters to the dialog
 * @param label string to be shown in the input field before selection eg. Username
 * @param hint hint to be shown in the input field when it is selected but empty eg. Joe
 * @param prefill string to be input into the text field by default
 * @param keyboardOptions software keyboard options which can be used to customize parts
 * of the keyboard
 * @param errorMessage a message to be shown to the user when the input is not valid
 * @param isTextValid a function which is called to check if the user input is valid
 * @param onInput a function which is called with the user input
 */
@SuppressLint("ComposableNaming")
@Composable
fun MaterialDialog.input(
    label: String,
    hint: String = "",
    prefill: String = "",
    keyboardOptions: KeyboardOptions = KeyboardOptions(),
    errorMessage: String = "",
    isTextValid: (String) -> Boolean = { true },
    onInput: (String) -> Unit = {}
) {
    var text by remember { mutableStateOf(prefill) }
    val valid = remember(text) { isTextValid(text) }

    val positiveEnabledIndex = remember {
        val index = positiveEnabledCounter.getAndIncrement()
        positiveEnabled.add(index, valid)
        index
    }

    // TODO: TextField/SoftwareKeyboard focus is bugged in compose beta1, Error happens if a focused TextField goes out of composition
    val focusManager = LocalFocusManager.current

    val callbackIndex = remember {
        val index = callbackCounter.getAndIncrement()
        callbacks.add(index) {
            focusManager.clearFocus(true)
            onInput(text)
        }
        index
    }

    DisposableEffect(valid) {
        setPositiveEnabled(positiveEnabledIndex, valid)
        onDispose { }
    }

    DisposableEffect(true) {
        onDispose {
            callbacks[callbackIndex] = {}
            setPositiveEnabled(positiveEnabledIndex, true)
        }
    }

    Column(modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 8.dp)) {
        TextField(
            value = text,
            onValueChange = { text = it },
            label = { Text(label, color = MaterialTheme.colors.onBackground.copy(0.8f)) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(hint, color = MaterialTheme.colors.onBackground.copy(0.5f)) },
            isError = !valid,
            keyboardOptions = keyboardOptions,
            textStyle = TextStyle(MaterialTheme.colors.onBackground, fontSize = 16.sp)
        )

        if (!valid) {
            Text(
                errorMessage,
                fontSize = 14.sp,
                color = MaterialTheme.colors.error,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}