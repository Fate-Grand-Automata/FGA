package io.github.fate_grand_automata.ui.drag_sort

import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.github.fate_grand_automata.util.ItemTouchHelperCallback

@Composable
fun <T> DragSort(
    items: MutableList<T>,
    viewConfigGrabber: (T) -> DragSortAdapter.ItemViewConfig,
) {
    AndroidView(
        factory = { context ->
            RecyclerView(context).apply {
                setHasFixedSize(true)
                layoutManager =
                    LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            }
        },
        update = {
            it.adapter = DragSortAdapter(
                items,
                viewConfigGrabber,
            ).also { adapter ->
                val callback = ItemTouchHelperCallback(adapter)
                val itemTouchHelper = ItemTouchHelper(callback)
                itemTouchHelper.attachToRecyclerView(it)

                adapter.itemTouchHelper = itemTouchHelper
            }
        },
    )
}
