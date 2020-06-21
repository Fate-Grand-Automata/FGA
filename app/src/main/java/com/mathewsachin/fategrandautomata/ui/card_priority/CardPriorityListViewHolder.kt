package com.mathewsachin.fategrandautomata.ui.card_priority

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mathewsachin.fategrandautomata.R

class CardPriorityListViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
    val battleStageTextView: TextView = ItemView.findViewById(R.id.card_priority_battle_stage)

    val itemsRecyclerView: RecyclerView = ItemView.findViewById(R.id.card_priority_items)
}