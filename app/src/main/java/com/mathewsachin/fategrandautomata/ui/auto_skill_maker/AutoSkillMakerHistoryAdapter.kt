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
            @ColorInt
            fun color(@ColorRes Res: Int): Int {
                return it.context.getColor(Res)
            }

            if (it is CardView) {
                val color = when {
                    // Turn/Battle change
                    cmd.contains(',') -> color(R.color.colorStageChange)

                    // Master Skill
                    cmd[0] in listOf('j', 'k', 'l', 'x') -> color(R.color.colorMasterSkill)

                    // Enemy Target
                    cmd[0] == 't' -> color(R.color.colorEnemyTarget)

                    // Servants
                    cmd[0] in listOf('4', 'a', 'b', 'c') -> color(R.color.colorServant1)
                    cmd[0] in listOf('5', 'd', 'e', 'f') -> color(R.color.colorServant2)
                    cmd[0] in listOf('6', 'g', 'h', 'i') -> color(R.color.colorServant3)

                    else -> color(R.color.colorAccent)
                }

                it.setCardBackgroundColor(color)
            }
        }

        holder.textView.text = cmd
    }
}