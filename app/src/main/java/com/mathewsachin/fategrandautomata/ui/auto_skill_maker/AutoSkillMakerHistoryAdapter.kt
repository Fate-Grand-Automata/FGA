package com.mathewsachin.fategrandautomata.ui.auto_skill_maker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.scripts.models.AutoSkillAction

class AutoSkillMakerHistoryAdapter :
    RecyclerView.Adapter<AutoSkillMakerHistoryAdapter.ViewHolder>() {
    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val textView: TextView = ItemView.findViewById(R.id.autoskill_maker_history_textview)
    }

    private var items: List<AutoSkillMakerEntry> = emptyList()

    fun update(items: List<AutoSkillMakerEntry>) {
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
                val defaultColor = R.color.colorAccent

                val colorRes = when (cmd) {
                    is AutoSkillMakerEntry.NextWave,
                    is AutoSkillMakerEntry.NextTurn -> R.color.colorStageChange

                    is AutoSkillMakerEntry.Action -> when (cmd.action) {
                        // Master Skill
                        is AutoSkillAction.MasterSkill -> R.color.colorMasterSkill

                        // Enemy Target
                        is AutoSkillAction.TargetEnemy -> R.color.colorEnemyTarget

                        // Servants
                        is AutoSkillAction.ServantSkill -> when (cmd.action.skill.autoSkillCode) {
                            'a', 'b', 'c' -> R.color.colorServant1
                            'd', 'e', 'f' -> R.color.colorServant2
                            'g', 'h', 'i' -> R.color.colorServant3
                            else -> defaultColor
                        }

                        is AutoSkillAction.NP -> when (cmd.action.np.autoSkillCode) {
                            '4' -> R.color.colorServant1
                            '5' -> R.color.colorServant2
                            '6' -> R.color.colorServant3
                            else -> defaultColor
                        }

                        else -> defaultColor
                    }

                    else -> defaultColor
                }

                val color = it.context.getColor(colorRes)

                it.setCardBackgroundColor(color)
            }
        }

        holder.textView.text =
            if (cmd is AutoSkillMakerEntry.Start) ">"
            else cmd.toString()
    }
}