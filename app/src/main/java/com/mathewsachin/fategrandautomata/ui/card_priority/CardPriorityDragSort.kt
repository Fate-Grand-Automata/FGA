package com.mathewsachin.fategrandautomata.ui.card_priority

import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mathewsachin.fategrandautomata.scripts.models.CardScore
import com.mathewsachin.fategrandautomata.util.ItemTouchHelperCallback

@Composable
fun CardPriorityDragSort(scores: MutableList<CardScore>) {
    AndroidView(
        factory = { context ->
            RecyclerView(context).apply {
                setHasFixedSize(true)
                layoutManager =
                    LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            }
        },
        update = {
            it.adapter = CardPriorityAdapter(scores).also { adapter ->
                val callback = ItemTouchHelperCallback(adapter)
                val itemTouchHelper = ItemTouchHelper(callback)
                itemTouchHelper.attachToRecyclerView(it)

                adapter.itemTouchHelper = itemTouchHelper
            }
        }
    )
}