package com.mathewsachin.fategrandautomata.util

import androidx.recyclerview.widget.RecyclerView

interface IItemTouchHelperAdapter {
    fun onItemMove(From: Int, To: Int)
}

interface IItemTouchHelperViewHolder {
    fun onItemSelected()

    fun onItemClear()
}

interface IOnStartDragListener {
    fun onStartDrag(ViewHolder: RecyclerView.ViewHolder)
}