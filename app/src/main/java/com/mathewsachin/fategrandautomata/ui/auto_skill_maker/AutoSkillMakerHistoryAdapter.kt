package com.mathewsachin.fategrandautomata.ui.auto_skill_maker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.mathewsachin.fategrandautomata.R

class AutoSkillMakerHistoryAdapter :
    RecyclerView.Adapter<AutoSkillMakerHistoryAdapter.ViewHolder>() {
    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val textView: TextView = ItemView.findViewById(R.id.autoskill_maker_history_textview)
    }

    private var items: List<String> = emptyList()

    fun update(items: List<String>) {
        this.items = items

        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.autoskill_maker_history_item, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val cmd = items[position]

        holder.itemView.let {
            if (it is CardView) {
                val colorRes = when (cmd[0]) {
                    // Turn/Battle change
                    ',' -> R.color.colorStageChange

                    // Master Skill
                    'j', 'k', 'l', 'x' -> R.color.colorMasterSkill

                    // Enemy Target
                    't' -> R.color.colorEnemyTarget

                    // Servants
                    '4', 'a', 'b', 'c' -> R.color.colorServant1
                    '5', 'd', 'e', 'f' -> R.color.colorServant2
                    '6', 'g', 'h', 'i' -> R.color.colorServant3

                    else -> R.color.colorAccent
                }

                val color = it.context.getColor(colorRes)

                it.setCardBackgroundColor(color)
            }
        }

        holder.textView.text = cmd
    }
}