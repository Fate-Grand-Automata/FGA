package com.mathewsachin.fategrandautomata.ui.card_priority

import android.graphics.Color
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.scripts.CardScore
import com.mathewsachin.fategrandautomata.util.IItemTouchHelperAdapter
import com.mathewsachin.fategrandautomata.util.IOnStartDragListener

class CardPriorityAdapter(private val Items: MutableList<CardScore>, val DragStartListener: IOnStartDragListener)
    : RecyclerView.Adapter<CardPriorityViewHolder>(), IItemTouchHelperAdapter {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardPriorityViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_priority_item, parent, false)

        return CardPriorityViewHolder(view)
    }

    override fun getItemCount() = Items.size

    override fun onBindViewHolder(holder: CardPriorityViewHolder, position: Int) {
        holder.textView.text = Items[position].toString().filterCapitals()

        val context = holder.textView.context
        val colorRes = Items[position].getColorRes()
        val colorInt = context.getColor(colorRes)
        holder.itemView.setBackgroundColor(colorInt)
        holder.textView.setTextColor(Color.WHITE)
        holder.itemView.setOnTouchListener { _, event ->
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