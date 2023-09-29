package io.github.fate_grand_automata.ui.drag_sort

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.fate_grand_automata.R
import io.github.fate_grand_automata.ui.FgaDialog

@Composable
fun <T> DragSort(
    titleText: String? = null,
    messageText: String? = null,
    items: List<T>,
    itemContent: @Composable LazyItemScope.(item: T) -> Unit,
    initialContent: @Composable BoxScope.() -> Unit,
    onSubmit: (List<T>) -> Unit
) {

    val dialog = FgaDialog()
    var newItems by remember(items) {
        mutableStateOf(items)
    }

    dialog.build(
        onDismiss = {
            newItems = items
        }
    ) {
        Column {
            titleText?.let {
                title(text = it)
            }
            messageText?.let {
                Text(
                    text = it,
                    textAlign = TextAlign.Justify,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxHeight(0.6f),
            ) {
                DragDropColumn(
                    items = newItems,
                    onSwap = { from, to ->
                        newItems = newItems.toMutableList().apply {
                            this[from] = newItems[to]
                            this[to] = newItems[from]
                        }
                    },
                    itemContent = itemContent
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = {
                        newItems = items
                    },
                    enabled = newItems != items,
                    content = {
                        Text(text = stringResource(id = R.string.reset).uppercase())
                    }
                )
                buttons(
                    onSubmit = {
                        onSubmit(newItems)
                    },
                    onCancel = {
                        newItems = items
                    }
                )
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .combinedClickable(
                onClick = {
                    dialog.show()
                },
                onLongClick = {
                    dialog.show()
                }
            ),
        content = initialContent
    )
}