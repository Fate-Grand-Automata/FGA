package com.mathewsachin.fategrandautomata.ui.auto_skill_list

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.scripts.prefs.IAutoSkillPreferences
import mva3.adapter.ItemBinder
import mva3.adapter.ItemViewHolder

typealias AutoSkillListListener = (IAutoSkillPreferences) -> Unit

class AutoSkillListBinder(
    val itemClickListener: AutoSkillListListener,
    val longClickListener: AutoSkillListListener
) :
    ItemBinder<IAutoSkillPreferences, AutoSkillListBinder.ViewHolder>() {
    inner class ViewHolder(itemView: View) : ItemViewHolder<IAutoSkillPreferences>(itemView) {
        val textView = itemView.findViewById<TextView>(R.id.auto_skill_item_name)
        val checkImg = itemView.findViewById<ImageView>(R.id.auto_skill_item_check)

        init {
            itemView.setOnLongClickListener {
                if (!isInActionMode) {
                    item?.let(longClickListener)
                    toggleItemSelection()
                    true
                } else false
            }
            itemView.setOnClickListener {
                if (isInActionMode) {
                    toggleItemSelection()
                } else item?.let(itemClickListener)
            }
        }
    }

    override fun bindViewHolder(holder: ViewHolder, item: IAutoSkillPreferences) {
        holder.textView.text = item.name
        holder.itemView.isActivated = holder.isItemSelected
        holder.checkImg.visibility = if (holder.isItemSelected) View.VISIBLE else View.GONE
    }

    override fun createViewHolder(parent: ViewGroup) =
        ViewHolder(
            inflate(parent, R.layout.autoskill_item)
        )

    override fun canBindData(item: Any?) =
        item is IAutoSkillPreferences
}