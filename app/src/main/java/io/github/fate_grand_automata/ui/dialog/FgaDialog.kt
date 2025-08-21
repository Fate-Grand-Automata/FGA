package io.github.fate_grand_automata.ui.dialog

import android.annotation.SuppressLint
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.fate_grand_automata.ui.DimmedIcon
import io.github.fate_grand_automata.ui.VectorIcon
import io.github.fate_grand_automata.util.toggle

// Simplified form of https://github.com/vanpra/compose-material-dialogs

@SuppressLint("ComposableNaming")
class FgaDialog private constructor(initialVisibility: Boolean = false) {
    companion object {

        fun saver(): Saver<FgaDialog, *> = Saver(
            save = {
                it.visible
            },
            restore = {
                FgaDialog(it)
            },
        )

        @Composable
        operator fun invoke(initialValue: Boolean = false) = rememberSaveable(saver = saver()) {
            FgaDialog(initialValue)
        }
    }

    private var visible by mutableStateOf(initialVisibility)

    fun show() {
        visible = true
    }

    fun hide() {
        visible = false
    }

    @Composable
    fun title(text: String, icon: VectorIcon? = null) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(24.dp, 16.dp),
        ) {
            if (icon != null) {
                DimmedIcon(
                    icon,
                    contentDescription = "heading icon",
                    modifier = Modifier
                        .padding(end = 16.dp),
                )
            }

            Text(
                text,
                style = MaterialTheme.typography.titleLarge,
            )
        }
    }

    @Composable
    fun message(text: String) {
        Text(
            text,
            modifier = Modifier
                .padding(24.dp, 16.dp)
                .padding(bottom = 12.dp),
        )
    }

    @Composable
    fun buttons(
        onSubmit: () -> Unit,
        onCancel: () -> Unit = {},
        showOk: Boolean = true,
        showCancel: Boolean = true,
        okEnabled: Boolean = true,
        okLabel: String = stringResource(android.R.string.ok),
        cancelLabel: String = stringResource(android.R.string.cancel),
    ) {
        Row(
            horizontalArrangement = Arrangement.End,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp, 5.dp),
        ) {
            if (showCancel) {
                TextButton(
                    onClick = {
                        hide()
                        onCancel()
                    },
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
                    enabled = okEnabled,
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
        onDismiss: () -> Unit = {},
        content: @Composable FgaDialog.() -> Unit,
    ) {
        if (visible) {
            ThemedDialog(
                onDismiss = {
                    hide()
                    onDismiss()
                },
            ) {
                Surface(
                    shape = shape,
                    color = color,
                    contentColor = contentColor,
                    modifier = Modifier
                        .padding(16.dp)
                        .widthIn(max = 450.dp)
                        .fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                    ) {
                        this@FgaDialog.content()
                    }
                }
            }
        }
    }

    @Composable
    fun constrained(
        content: @Composable FgaDialog.(Modifier) -> Unit,
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
    prioritySelected: Boolean = false,
    onSelectedChange: (Set<T>) -> Unit,
    template: @Composable RowScope.(T) -> Unit = {
        Text(it.toString())
    },
) {
    constrained { modifier ->
        LazyColumn(
            contentPadding = PaddingValues(16.dp, 0.dp),
            modifier = modifier
                .fillMaxWidth(),
        ) {
            val rearrangeItems = when (prioritySelected) {
                true -> items.sortedByDescending {
                    it in selected
                }

                false -> items
            }
            items(rearrangeItems) {
                ChoiceListItem(
                    isSelected = it in selected,
                    onClick = { onSelectedChange(selected.toggle(it)) },
                    modifier = Modifier.animateItem(
                        spring(
                            stiffness = Spring.StiffnessMedium,
                        ),
                    ),
                ) {
                    template(it)
                }
            }
        }
    }
}

@Composable
fun ChoiceListItem(
    modifier: Modifier = Modifier,
    isSelected: Boolean,
    onClick: () -> Unit,
    content: @Composable RowScope.() -> Unit,
) {
    Card(
        shape = CircleShape,
        colors = CardDefaults.cardColors(
            containerColor =
            if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
        ),
        modifier = Modifier
            .padding(bottom = 7.dp)
            .then(modifier),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable(onClick = onClick)
                .padding(16.dp, 5.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1f),
            ) {
                content()
            }

            if (isSelected) {
                Icon(
                    rememberVectorPainter(Icons.Default.Check),
                    contentDescription = "check",
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
    },
) {
    constrained { modifier ->
        LazyColumn(
            contentPadding = PaddingValues(16.dp, 0.dp),
            modifier = modifier
                .fillMaxWidth(),
        ) {
            items(items) {
                ChoiceListItem(
                    isSelected = it == selected,
                    onClick = { onSelectedChange(it) },
                ) {
                    template(it)
                }
            }
        }
    }
}
