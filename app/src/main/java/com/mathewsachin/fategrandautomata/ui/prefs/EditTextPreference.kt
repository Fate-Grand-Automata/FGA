package com.mathewsachin.fategrandautomata.ui.prefs

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
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