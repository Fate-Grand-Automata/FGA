package com.mathewsachin.fategrandautomata.ui.card_priority

import android.graphics.Color
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.util.IItemTouchHelperViewHolder

class ItemViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView), IItemTouchHelperViewHolder {
    val textView: TextView = ItemView.findViewById(R.id.card_priority_textview)
    val imageView: ImageView = ItemView.findViewById(R.id.card_priority_dragger)

    override fun onItemSelected() {
        itemView.setBackgroundColor(Color.LTGRAY)
    }

    override fun onItemClear() {
        itemView.setBackgroundColor(Color.TRANSPARENT)
    }
}