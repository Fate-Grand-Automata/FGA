package com.mathewsachin.fategrandautomata.ui.prefs

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.sp
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.prefs.core.Pref
import com.mathewsachin.fategrandautomata.ui.VectorIcon

@Composable
fun PreferenceTextEditor(
    label: String,
    prefill: String,
    onSubmit: (String) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
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

    val focusRequester = remember { FocusRequester() }

    TextField(
        value = textFieldValue,
        onValueChange = { textFieldValue = it },
        label = { Text(label, color = MaterialTheme.colorScheme.onBackground.copy(0.8f)) },
        modifier = modifier
            .fillMaxWidth()
            .focusRequester(focusRequester),
        isError = !valid,
        keyboardOptions = keyboardOptions,
        textStyle = TextStyle(MaterialTheme.colorScheme.onBackground, fontSize = 16.sp),
        colors = TextFieldDefaults.textFieldColors(
            containerColor = Color.Transparent
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                if (valid) {
                    onSubmit(textFieldValue.text)
                }
            }
        ),
        trailingIcon = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onCancel,
                ) {
                    Icon(
                        painterResource(R.drawable.ic_close),
                        contentDescription = stringResource(android.R.string.cancel),
                        tint = MaterialTheme.colorScheme.error
                    )
                }

                IconButton(
                    onClick = { onSubmit(textFieldValue.text) },
                    enabled = valid
                ) {
                    StatusWrapper(enabled = valid) {
                        Icon(
                            rememberVectorPainter(Icons.Default.Check),
                            contentDescription = stringResource(android.R.string.ok)
                        )
                    }
                }
            }
        }
    )

    SideEffect {
        focusRequester.requestFocus()
    }
}

@Composable
fun Pref<String>.EditTextPreference(
    title: String,
    modifier: Modifier = Modifier,
    singleLineTitle: Boolean = false,
    singleLine: Boolean = false,
    icon: VectorIcon? = null,
    enabled: Boolean = true,
    summary: (String) -> String = { it },
    validate: (String) -> Boolean = { true }
) {
    var state by remember()
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
                state = it
                editing = false
            },
            onCancel = { editing = false },
            keyboardOptions = keyboardOptions,
            modifier = modifier
        )
    } else {
        Preference(
            title = title,
            summary = summary(state),
            singleLineTitle = singleLineTitle,
            icon = icon,
            enabled = enabled,
            onClick = { editing = true },
            modifier = modifier
        )
    }
}