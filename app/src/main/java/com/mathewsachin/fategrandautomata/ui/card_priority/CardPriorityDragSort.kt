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
            val adapter = CardPriorityAdapter(scores)

            val recyclerView = RecyclerView(context).apply {
                setHasFixedSize(true)
                this.adapter = adapter
                layoutManager =
                    LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            }

            val callback = ItemTouchHelperCallback(adapter)
            val itemTouchHelper = ItemTouchHelper(callback)
            itemTouchHelper.attachToRecyclerView(recyclerView)

            adapter.itemTouchHelper = itemTouchHelper

            recyclerView
        }
    )
}