package com.mathewsachin.fategrandautomata.ui.auto_skill_maker

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.mathewsachin.fategrandautomata.R

class AutoSkillMakerHistoryAdapter(private val Items: List<String>)
    : RecyclerView.Adapter<AutoSkillMakerHistoryViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AutoSkillMakerHistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.autoskill_maker_history_item, parent, false)

        return AutoSkillMakerHistoryViewHolder(view)
    }

    override fun getItemCount() = Items.size

    override fun onBindViewHolder(holder: AutoSkillMakerHistoryViewHolder, position: Int) {
        val cmd = Items[position]

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