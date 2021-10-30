package com.mathewsachin.fategrandautomata.ui.drag_sort

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.util.IItemTouchHelperAdapter
import com.mathewsachin.fategrandautomata.util.IItemTouchHelperViewHolder

class DragSortAdapter<T>(
    private val items: MutableList<T>,
    private val viewConfigGrabber: (T) -> ItemViewConfig
): RecyclerView.Adapter<DragSortAdapter.ViewHolder>(), IItemTouchHelperAdapter {
    class ItemViewConfig(
        @ColorInt val foregroundColor: Int,
        @ColorInt val backgroundColor: Int,
        val text: String
    )

    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView),
        IItemTouchHelperViewHolder {
        val textView: TextView = ItemView.findViewById(R.id.drag_sort_text)

        override fun onItemSelected() {}

        override fun onItemClear() {}
    }

    lateinit var itemTouchHelper: ItemTouchHelper

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.drag_sort_item, parent, false)

        return ViewHolder(view).also { holder ->
            view.setOnTouchListener { _, event ->
                if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                    itemTouchHelper.startDrag(holder)
                }
                true
            }
        }
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val viewConfig = viewConfigGrabber(items[position])

        holder.textView.text = viewConfig.text
        holder.itemView.setBackgroundColor(viewConfig.backgroundColor)
        holder.textView.setTextColor(viewConfig.foregroundColor)
    }

    override fun onItemMove(From: Int, To: Int) {
        val temp = items[From]
        items[From] = items[To]
        items[To] = temp

        notifyItemMoved(From, To)
    }
}