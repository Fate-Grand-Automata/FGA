package io.github.fate_grand_automata.ui.spam


import android.widget.LinearLayout
import androidx.annotation.ColorInt
import io.github.fate_grand_automata.R
import io.github.fate_grand_automata.ui.drag_sort.DragSortAdapterBase
import android.widget.TextView

class DragSortSkillPriorityAdapter<T>(
    items: MutableList<T>,
    private val viewConfigGrabber: (T) -> ItemViewConfig
) : DragSortAdapterBase<T>(items) {

    class ItemViewConfig(
        @ColorInt val teamForegroundColor: Int,
        @ColorInt val skillForegroundColor: Int,
        @ColorInt val backgroundColor: Int,
        val teamSlot: String,
        val skillSlot: String
    )

    var onSkillDragged: ((newList: List<T>) -> Unit)? = null

    override fun onItemMove(From: Int, To: Int) {
        super.onItemMove(From, To)
        onSkillDragged?.invoke(items.toList())
    }

    override fun getLayoutResId() = R.layout.skill_priority_item

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val viewConfig = viewConfigGrabber(items[position])

        val layout = holder.itemView.findViewById<LinearLayout>(R.id.skill_priority_item)
        val teamSlotText = holder.itemView.findViewById<TextView>(R.id.team_slot)
        val skillSlotText = holder.itemView.findViewById<TextView>(R.id.skill_slot)

        teamSlotText.text = viewConfig.teamSlot
        skillSlotText.text = viewConfig.skillSlot

        teamSlotText.setTextColor(viewConfig.teamForegroundColor)
        skillSlotText.setTextColor(viewConfig.skillForegroundColor)
        layout.setBackgroundColor(viewConfig.backgroundColor)
    }
}