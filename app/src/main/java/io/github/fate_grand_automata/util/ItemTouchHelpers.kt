package io.github.fate_grand_automata.util

import android.view.View

interface IItemTouchHelperAdapter {
    fun onItemMove(from: Int, to: Int, origin: View? = null, target: View? = null)
}

interface IItemTouchHelperViewHolder {
    fun onItemSelected()

    fun onItemClear()
}

