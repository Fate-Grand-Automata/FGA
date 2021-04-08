package com.mathewsachin.fategrandautomata.ui.battle_config_item

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mathewsachin.fategrandautomata.util.toggle

@SuppressLint("ComposableNaming")
class FgaDialog private constructor() {
    companion object {
        @Composable
        operator fun invoke() = remember { FgaDialog() }
    }

    private val visible = mutableStateOf(false)

    fun show() {
        visible.value = true
    }

    fun hide() {
        visible.value = false
    }

    @Composable
    fun title(text: String) {
        Text(
            text,
            style = MaterialTheme.typography.h6,
            modifier = Modifier
                .padding(16.dp)
        )
    }

    @Composable
    fun buttons(
        onSubmit: () -> Unit,
        showOk: Boolean = true,
        showCancel: Boolean = true,
        okEnabled: Boolean = true
    ) {
        if (showOk || showCancel) {
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(5.dp)
            ) {
                if (showCancel) {
                    TextButton(
                        onClick = { hide() }
                    ) {
                        Text(stringResource(android.R.string.cancel))
                    }
                }

                if (showOk) {
                    TextButton(
                        onClick = {
                            onSubmit()
                            hide()
                        },
                        enabled = okEnabled
                    ) {
                        Text(stringResource(android.R.string.ok))
                    }
                }
            }
        }
    }

    @Composable
    fun build(
        content: @Composable FgaDialog.() -> Unit
    ) {
        if (visible.value) {
            ThemedDialog(
                onDismiss = { hide() }
            ) {
                Surface {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        this@FgaDialog.content()
                    }
                }
            }
        }
    }

    @Composable
    fun constrained(
        content: @Composable FgaDialog.(Modifier) -> Unit
    ) {
        BoxWithConstraints {
            val modifier = Modifier
                .heightIn(max = maxHeight * 0.6f)
                .padding(bottom = 8.dp)

            this@FgaDialog.content(modifier)
        }
    }
}

@Composable
@SuppressLint("ComposableNaming")
fun <T> FgaDialog.multiChoiceList(
    selected: Set<T>,
    items: List<T>,
    onSelectedChange: (Set<T>) -> Unit,
    template: @Composable RowScope.(T) -> Unit = {
        Text(it.toString())
    }
) {
    constrained { modifier ->
        LazyColumn(
            contentPadding = PaddingValues(16.dp, 0.dp),
            modifier = modifier
                .fillMaxWidth()
        ) {
            items(items) {
                val isSelected = it in selected

                val background =
                    if (isSelected)
                        MaterialTheme.colors.secondary
                    else MaterialTheme.colors.surface

                val foreground =
                    if (isSelected)
                        MaterialTheme.colors.onSecondary
                    else MaterialTheme.colors.onSurface

                Card(
                    shape = CircleShape,
                    backgroundColor = background,
                    contentColor = foreground,
                    modifier = Modifier
                        .padding(bottom = 5.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable {
                                onSelectedChange(selected.toggle(it))
                            }
                            .padding(16.dp, 5.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .weight(1f)
                        ) {
                            template(it)
                        }

                        if (isSelected) {
                            Icon(
                                rememberVectorPainter(Icons.Default.Check),
                                contentDescription = "check"
                            )
                        }
                    }
                }
            }
        }
    }
}