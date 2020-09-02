package com.mathewsachin.fategrandautomata.ui.card_priority

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.scripts.models.CardScore
import com.mathewsachin.fategrandautomata.util.ItemTouchHelperCallback

class CardPriorityListAdapter(private val Items: List<MutableList<CardScore>>) :
    RecyclerView.Adapter<CardPriorityListAdapter.ViewHolder>() {
    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val battleStageTextView: TextView = ItemView.findViewById(R.id.card_priority_battle_stage)

        val itemsRecyclerView: RecyclerView = ItemView.findViewById(R.id.card_priority_items)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_priority_list_item, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount() = Items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.battleStageTextView.text =
            holder.itemView.context.getString(R.string.card_priority_wave_number, position + 1)

        val adapter = CardPriorityAdapter(Items[position])

        val recyclerView = holder.itemsRecyclerView
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = adapter
        recyclerView.layoutManager =
            LinearLayoutManager(holder.itemView.context, LinearLayoutManager.HORIZONTAL, false)

        val callback = ItemTouchHelperCallback(adapter)
        val itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper.attachToRecyclerView(recyclerView)

        adapter.itemTouchHelper = itemTouchHelper
    }
}