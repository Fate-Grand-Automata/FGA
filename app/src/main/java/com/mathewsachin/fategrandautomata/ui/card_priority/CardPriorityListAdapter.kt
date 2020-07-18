package com.mathewsachin.fategrandautomata.ui.card_priority

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.scripts.CardScore
import com.mathewsachin.fategrandautomata.util.ItemTouchHelperCallback

class CardPriorityListAdapter(private val Items: List<MutableList<CardScore>>)
    : RecyclerView.Adapter<CardPriorityListViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardPriorityListViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_priority_list_item, parent, false)

        return CardPriorityListViewHolder(view)
    }

    override fun getItemCount() = Items.size

    override fun onBindViewHolder(holder: CardPriorityListViewHolder, position: Int) {
        holder.battleStageTextView.text = "WAVE ${position + 1}"

        val adapter = CardPriorityAdapter(Items[position])

        val recyclerView = holder.itemsRecyclerView
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(holder.itemView.context, LinearLayoutManager.HORIZONTAL, false)

        val callback = ItemTouchHelperCallback(adapter)
        val itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper.attachToRecyclerView(recyclerView)

        adapter.itemTouchHelper = itemTouchHelper
    }
}