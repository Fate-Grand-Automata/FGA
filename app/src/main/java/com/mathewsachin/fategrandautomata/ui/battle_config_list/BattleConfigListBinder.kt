package com.mathewsachin.fategrandautomata.ui.battle_config_list

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
        val textView: TextView = itemView.findViewById(R.id.battle_config_item_name)
        val checkImg: ImageView = itemView.findViewById(R.id.battle_config_item_check)

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
            inflate(parent, R.layout.battle_config_item)
        )

    override fun canBindData(item: Any?) =
        item is IAutoSkillPreferences
}