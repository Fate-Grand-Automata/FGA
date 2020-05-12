package com.mathewsachin.fategrandautomata.ui.card_priority

import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.scripts.CardScore
import com.mathewsachin.fategrandautomata.util.IItemTouchHelperAdapter
import com.mathewsachin.fategrandautomata.util.IOnStartDragListener

class RecyclerListAdapter(private val Items: MutableList<CardScore>, val DragStartListener: IOnStartDragListener)
    : RecyclerView.Adapter<ItemViewHolder>(), IItemTouchHelperAdapter {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_priority_item, parent, false)

        return ItemViewHolder(view)
    }

    override fun getItemCount() = Items.size

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.textView.text = Items[position].toString()

        val context = holder.textView.context
        val colorRes = Items[position].getColorRes()
        val colorInt = context.getColor(colorRes)
        holder.textView.setTextColor(colorInt)

        holder.imageView.setOnTouchListener { _, event ->
            if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                DragStartListener.onStartDrag(holder)
            }
            true
        }
    }

    override fun onItemMove(From: Int, To: Int) {
        val temp = Items[From]
        Items[From] = Items[To]
        Items[To] = temp

        notifyItemMoved(From, To)
    }
}