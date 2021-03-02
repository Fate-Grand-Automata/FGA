package com.mathewsachin.fategrandautomata.ui.prefs

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
fun editNumberDialog(
    title: String,
    value: Int,
    valueChange: (Int) -> Unit,
    validate: (Int) -> Boolean = { true },
    errorMessage: String = ""
) =
    editTextDialog(
        title = title,
        value = value.toString(),
        valueChange = { valueChange(it.toInt()) },
        validate = { it.toIntOrNull().let { num -> num != null && validate(num) } },
        errorMessage = errorMessage,
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Done,
            keyboardType = KeyboardType.Number
        )
    )

@Composable
fun Pref<Int>.EditNumberPreference(
    title: String,
    singleLineTitle: Boolean = false,
    icon: Painter? = null,
    enabled: Boolean = true,
    hint: String = "",
    min: Int = 0,
    max: Int = Int.MAX_VALUE,
    summary: (Int) -> String = { it.toString() },
    dialogTitle: String? = null
) {
    val state by collect()

    val dialog = editNumberDialog(
        title = dialogTitle ?: title,
        value = state,
        valueChange = { set(it.coerceIn(min, max)) }
    )

    Preference(
        title = title,
        summary = summary(state),
        singleLineTitle = singleLineTitle,
        icon = icon,
        enabled = enabled,
        hint = hint,
        onClick = { dialog.show() }
    )
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
    dialogTitle: String? = null
) {
    val state by collect()

    val dialog = editTextDialog(
        title = dialogTitle ?: title,
        value = state,
        valueChange = { set(it) },
        keyboardOptions = KeyboardOptions(
            imeAction = if (singleLine) ImeAction.Done else ImeAction.Default
        )
    )

    Preference(
        title = title,
        summary = summary(state),
        singleLineTitle = singleLineTitle,
        icon = icon,
        enabled = enabled,
        hint = hint,
        onClick = { dialog.show() }
    )
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

    val callbackIndex = remember {
        val index = callbackCounter.getAndIncrement()
        callbacks.add(index) { onInput(text) }
        index
    }

    DisposableEffect(valid) {
        setPositiveEnabled(positiveEnabledIndex, valid)
        onDispose { }
    }

    DisposableEffect(Unit) {
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