package io.github.fate_grand_automata.ui.drag_sort

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import io.github.fate_grand_automata.R
import io.github.fate_grand_automata.util.IItemTouchHelperAdapter
import io.github.fate_grand_automata.util.IItemTouchHelperViewHolder

abstract class DragSortAdapterBase<T>(
    protected val items: MutableList<T> = mutableListOf()
) : RecyclerView.Adapter<DragSortAdapterBase.ViewHolder>(), IItemTouchHelperAdapter {
    class ViewHolder(val containerView: View) :
        RecyclerView.ViewHolder(containerView), IItemTouchHelperViewHolder {
        override fun onItemSelected() {}
        override fun onItemClear() {}
    }

    lateinit var itemTouchHelper: ItemTouchHelper

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout = getLayoutResId()
        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)

        return ViewHolder(view).also { holder ->
            view.setOnTouchListener { _, event ->
                if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                    itemTouchHelper.startDrag(holder)
                }
                true
            }
        }
    }

    private var prevSize = items.size

    fun refreshIfDataChanged() {
        if (prevSize != items.size) {
            prevSize = items.size
            notifyDataSetChanged()
        }
    }

    override fun getItemCount() = items.size

    override fun onItemMove(From: Int, To: Int) {
        items.slide(From, To)
        notifyItemMoved(From, To)
    }

    abstract fun getLayoutResId(): Int
    abstract override fun onBindViewHolder(holder: ViewHolder, position: Int)
}

class DragSortAdapter<T>(
    items: MutableList<T>,
    private val viewConfigGrabber: (T) -> ItemViewConfig
) : DragSortAdapterBase<T>(items) {
    class ItemViewConfig(
        @ColorInt val foregroundColor: Int,
        @ColorInt val backgroundColor: Int,
        val text: String
    )

    override fun getLayoutResId() = R.layout.drag_sort_item

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val viewConfig = viewConfigGrabber(items[position])

        val textView = holder.itemView.findViewById<TextView>(R.id.drag_sort_text)
        textView.text = viewConfig.text
        textView.setTextColor(viewConfig.foregroundColor)
        holder.itemView.setBackgroundColor(viewConfig.backgroundColor)
    }
}

private fun <T> MutableList<T>.slide(from: Int, to: Int) {
    val item = this[from]

    if (from < to) {
        for (i in from until to) {
            this[i] = this[i + 1]
        }
    } else {
        for (i in from downTo to + 1) {
            this[i] = this[i - 1]
        }
    }
    this[to] = item
}