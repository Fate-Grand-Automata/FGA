package com.mathewsachin.fategrandautomata.ui.card_priority

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.observe
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.util.ItemTouchHelperCallback

class CardPriorityListAdapter(
    private val Items: List<CardPriorityListItem>,
    private val experimental: LiveData<Boolean>,
    private val lifecycleOwner: LifecycleOwner
) :
    RecyclerView.Adapter<CardPriorityListAdapter.ViewHolder>() {
    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val battleStageTextView: TextView = ItemView.findViewById(R.id.card_priority_battle_stage)

        val itemsRecyclerView: RecyclerView = ItemView.findViewById(R.id.card_priority_items)

        val rearrangeCardsSwitchView: SwitchCompat = ItemView.findViewById(R.id.rearrange_cards)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_priority_list_item, parent, false)

        return ViewHolder(view).also { holder ->
            experimental.observe(lifecycleOwner) {
                holder.rearrangeCardsSwitchView.visibility = if (it) View.VISIBLE else View.GONE
            }
        }
    }

    override fun getItemCount() = Items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.battleStageTextView.text = "WAVE ${position + 1}"

        holder.rearrangeCardsSwitchView.setOnCheckedChangeListener { _, isChecked ->
            Items[position].rearrangeCards = isChecked
        }
        holder.rearrangeCardsSwitchView.isChecked = Items[position].rearrangeCards

        val adapter = CardPriorityAdapter(Items[position].scores)

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