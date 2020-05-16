package com.mathewsachin.fategrandautomata.ui.card_priority

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.util.IItemTouchHelperViewHolder

class CardPriorityViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView), IItemTouchHelperViewHolder {
    val textView: TextView = ItemView.findViewById(R.id.card_priority_textview)

    override fun onItemSelected() { }

    override fun onItemClear() { }
}