package com.mathewsachin.fategrandautomata.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mathewsachin.fategrandautomata.util.toggle
import java.util.*

// Simplified form of https://github.com/vanpra/compose-material-dialogs

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
    fun title(text: String, icon: VectorIcon? = null) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(24.dp, 16.dp)
        ) {
            if (icon != null) {
                DimmedIcon(
                    icon,
                    contentDescription = "heading icon",
                    modifier = Modifier
                        .padding(end = 16.dp)
                )
            }

            Text(
                text,
                style = MaterialTheme.typography.titleLarge
            )
        }
    }

    @Composable
    fun message(text: String) {
        Text(
            text,
            modifier = Modifier
                .padding(24.dp, 16.dp)
                .padding(bottom = 12.dp)
        )
    }

    @Composable
    fun buttons(
        onSubmit: () -> Unit,
        showOk: Boolean = true,
        showCancel: Boolean = true,
        okEnabled: Boolean = true,
        okLabel: String = stringResource(android.R.string.ok),
        cancelLabel: String = stringResource(android.R.string.cancel)
    ) {
        Row(
            horizontalArrangement = Arrangement.End,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp, 5.dp)
        ) {
            if (showCancel) {
                TextButton(
                    onClick = { hide() }
                ) {
                    Text(cancelLabel.uppercase())
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
                    Text(okLabel.uppercase())
                }
            }
        }
    }

    @Composable
    fun build(
        shape: Shape = MaterialTheme.shapes.medium,
        color: Color = MaterialTheme.colorScheme.surface,
        contentColor: Color = contentColorFor(color),
        content: @Composable FgaDialog.() -> Unit
    ) {
        if (visible.value) {
            ThemedDialog(
                onDismiss = { hide() }
            ) {
                Surface(
                    shape = shape,
                    color = color,
                    contentColor = contentColor,
                    modifier = Modifier
                        .padding(16.dp)
                        .widthIn(max = 450.dp)
                        .fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
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
                ChoiceListItem(
                    isSelected = it in selected,
                    onClick = { onSelectedChange(selected.toggle(it)) }
                ) {
                    template(it)
                }
            }
        }
    }
}

@Composable
fun ChoiceListItem(
    isSelected: Boolean,
    onClick: () -> Unit,
    content: @Composable RowScope.() -> Unit
) {
    Card(
        shape = CircleShape,
        colors = CardDefaults.cardColors(
            containerColor =
            if (isSelected) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = Modifier
            .padding(bottom = 7.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable(onClick = onClick)
                .padding(16.dp, 5.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1f)
            ) {
                content()
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

@Composable
@SuppressLint("ComposableNaming")
fun <T> FgaDialog.singleChoiceList(
    selected: T,
    items: List<T>,
    onSelectedChange: (T) -> Unit,
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
                ChoiceListItem(
                    isSelected = it == selected,
                    onClick = { onSelectedChange(it) }
                ) {
                    template(it)
                }
            }
        }
    }
}