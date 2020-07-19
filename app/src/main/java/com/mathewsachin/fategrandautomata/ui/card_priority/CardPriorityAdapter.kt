package com.mathewsachin.fategrandautomata.ui.card_priority

import android.graphics.Color
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.scripts.CardScore
import com.mathewsachin.fategrandautomata.scripts.enums.CardAffinityEnum
import com.mathewsachin.fategrandautomata.scripts.enums.CardTypeEnum
import com.mathewsachin.fategrandautomata.util.IItemTouchHelperAdapter

fun CardScore.getColorRes(): Int {
    return when (CardType) {
        CardTypeEnum.Buster -> when (CardAffinity) {
            CardAffinityEnum.Weak -> R.color.colorBusterWeak
            CardAffinityEnum.Normal -> R.color.colorBuster
            CardAffinityEnum.Resist -> R.color.colorBusterResist
        }
        CardTypeEnum.Arts -> when (CardAffinity) {
            CardAffinityEnum.Weak -> R.color.colorArtsWeak
            CardAffinityEnum.Normal -> R.color.colorArts
            CardAffinityEnum.Resist -> R.color.colorArtsResist
        }
        CardTypeEnum.Quick -> when (CardAffinity) {
            CardAffinityEnum.Weak -> R.color.colorQuickWeak
            CardAffinityEnum.Normal -> R.color.colorQuick
            CardAffinityEnum.Resist -> R.color.colorQuickResist
        }
    }
}

class CardPriorityAdapter(private val Items: MutableList<CardScore>) :
    RecyclerView.Adapter<CardPriorityViewHolder>(), IItemTouchHelperAdapter {
    lateinit var itemTouchHelper: ItemTouchHelper

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
                itemTouchHelper.startDrag(holder)
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