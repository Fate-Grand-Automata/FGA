package io.github.fate_grand_automata.ui.drag_sort

import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import io.github.fate_grand_automata.ui.scrollbar
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

// https://stackoverflow.com/a/73592914/9381524

/**
 * Creates a drag-and-drop column
 *
 * @param items Collection of Elements <T>
 * @param onSwap (from, to) returns two Integer to indicate the index of the currently dragging item and the index of the item it will swap with
 * @param itemContent returns the LazyItemScope of the LazyColumn
 */

@Composable
fun <T> DragDropColumn(
    items: List<T>,
    onSwap: (Int, Int) -> Unit,
    itemContent: @Composable LazyItemScope.(item: T) -> Unit
) {
    var overscrollJob by remember { mutableStateOf<Job?>(null) }

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val dragDropState = rememberDragDropState(listState) { fromIndex, toIndex ->
        onSwap(fromIndex, toIndex)
    }

    Box(
        modifier = Modifier
            .clipToBounds()
    ){
        LazyColumn(
            modifier = Modifier
                .pointerInput(dragDropState) {
                    detectDragGesturesAfterLongPress(
                        onDrag = { change, offset ->
                            change.consume()
                            dragDropState.onDrag(offset = offset)

                            if (overscrollJob?.isActive == true)
                                return@detectDragGesturesAfterLongPress

                            dragDropState
                                .checkForOverScroll()
                                .takeIf { it != 0f }
                                ?.let {
                                    overscrollJob =
                                        scope.launch {
                                            dragDropState.state.animateScrollBy(
                                                it * 1.3f, tween(easing = FastOutLinearInEasing)
                                            )
                                        }
                                }
                                ?: run { overscrollJob?.cancel() }
                        },
                        onDragStart = { offset -> dragDropState.onDragStart(offset) },
                        onDragEnd = {
                            dragDropState.onDragInterrupted()
                            overscrollJob?.cancel()
                        },
                        onDragCancel = {
                            dragDropState.onDragInterrupted()
                            overscrollJob?.cancel()
                        }
                    )
                }
                .scrollbar(
                    state = listState,
                    horizontal = false,
                    knobColor = MaterialTheme.colorScheme.secondary,
                    hiddenAlpha = 0.3f
                ),
            state = listState,
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(items = items) { index, item ->
                DraggableItem(
                    dragDropState = dragDropState,
                    index = index
                ) { isDragging ->
                    val elevation by animateDpAsState(if (isDragging) 4.dp else 0.dp, label = "CardDraggingElevationAnimation")
                    Card(
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = elevation
                        ),
                        shape = RectangleShape,
                        border = if (isDragging) BorderStroke(5.dp, MaterialTheme.colorScheme.onBackground) else null
                    ) {
                        itemContent(item)
                    }
                }
            }
        }
    }
}