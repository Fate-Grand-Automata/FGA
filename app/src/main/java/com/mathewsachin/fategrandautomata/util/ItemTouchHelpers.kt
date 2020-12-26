package com.mathewsachin.fategrandautomata.util

interface IItemTouchHelperAdapter {
    fun onItemMove(From: Int, To: Int)
}

interface IItemTouchHelperViewHolder {
    fun onItemSelected()

    fun onItemClear()
}

