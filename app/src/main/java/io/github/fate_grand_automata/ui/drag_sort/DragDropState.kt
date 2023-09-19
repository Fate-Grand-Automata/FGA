package io.github.fate_grand_automata.ui.drag_sort

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


class DragDropState internal constructor(
    val state: LazyListState,
    private val scope: CoroutineScope,
    private val onSwap: (Int, Int) -> Unit
) {
    private var draggedDistance by mutableStateOf(0f)
    private var draggingItemInitialOffset by mutableStateOf(0)
    internal val draggingItemOffset: Float
        get() = draggingItemLayoutInfo?.let { item ->
            draggingItemInitialOffset + draggedDistance - item.offset
        } ?: 0f
    private val draggingItemLayoutInfo: LazyListItemInfo?
        get() = state.layoutInfo.visibleItemsInfo
            .firstOrNull { it.index == currentIndexOfDraggedItem }

    internal var previousIndexOfDraggedItem by mutableStateOf<Int?>(null)
        private set
    internal var previousItemOffset = Animatable(0f)
        private set

    // used to obtain initial offsets on drag start
    private var initiallyDraggedElement by mutableStateOf<LazyListItemInfo?>(null)

    var currentIndexOfDraggedItem by mutableStateOf<Int?>(null)

    private val initialOffsets: Pair<Int, Int>?
        get() = initiallyDraggedElement?.let { Pair(it.offset, it.offsetEnd) }

    private val currentElement: LazyListItemInfo?
        get() = currentIndexOfDraggedItem?.let {
            state.getVisibleItemInfoFor(absoluteIndex = it)
        }


    fun onDragStart(offset: Offset) {
        state.layoutInfo.visibleItemsInfo
            .firstOrNull { item -> offset.y.toInt() in item.offset..(item.offset + item.size) }
            ?.also {
                currentIndexOfDraggedItem = it.index
                initiallyDraggedElement = it
                draggingItemInitialOffset = it.offset
            }
    }

    fun onDragInterrupted() {
        if (currentIndexOfDraggedItem != null) {
            previousIndexOfDraggedItem = currentIndexOfDraggedItem
            // val startOffset = draggingItemOffset
            scope.launch {
                //previousItemOffset.snapTo(startOffset)
                previousItemOffset.animateTo(
                    0f,
                    tween(easing = FastOutLinearInEasing)
                )
                previousIndexOfDraggedItem = null
            }
        }
        draggingItemInitialOffset = 0
        draggedDistance = 0f
        currentIndexOfDraggedItem = null
        initiallyDraggedElement = null
    }

    fun onDrag(offset: Offset) {
        draggedDistance += offset.y

        initialOffsets?.let { (topOffset, bottomOffset) ->
            val startOffset = topOffset + draggedDistance
            val endOffset = bottomOffset + draggedDistance

            currentElement?.let { hovered ->
                state.layoutInfo.visibleItemsInfo
                    .filterNot { item -> item.offsetEnd < startOffset || item.offset > endOffset || hovered.index == item.index }
                    .firstOrNull { item ->
                        val delta = (startOffset - hovered.offset)
                        when {
                            delta > 0 -> (endOffset > item.offsetEnd)
                            else -> (startOffset < item.offset)
                        }
                    }
                    ?.also { item ->
                        currentIndexOfDraggedItem?.let { current ->
                            scope.launch {
                                onSwap.invoke(
                                    current,
                                    item.index
                                )
                            }
                        }
                        currentIndexOfDraggedItem = item.index
                    }
            }
        }
    }

    fun checkForOverScroll(): Float {
        return initiallyDraggedElement?.let {
            val startOffset = it.offset + draggedDistance
            val endOffset = it.offsetEnd + draggedDistance
            return@let when {
                draggedDistance > 0 -> (endOffset - state.layoutInfo.viewportEndOffset+50f).takeIf { diff -> diff > 0 }
                draggedDistance < 0 -> (startOffset - state.layoutInfo.viewportStartOffset-50f).takeIf { diff -> diff < 0 }
                else -> null
            }
        } ?: 0f
    }
}