package com.mathewsachin.fategrandautomata.ui.skill_maker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.scripts.models.AutoSkillAction

class SkillMakerHistoryAdapter(val currentIndexListener: (Int) -> Unit) :
    RecyclerView.Adapter<SkillMakerHistoryAdapter.ViewHolder>() {
    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val textView: TextView = ItemView.findViewById(R.id.skill_maker_history_text_view)
        var index = -1
    }

    private var items: List<SkillMakerEntry> = emptyList()
    private var currentIndex = -1

    fun update(items: List<SkillMakerEntry>, current: Int) {
        this.items = items
        this.currentIndex = current

        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.skill_maker_history_item, parent, false)

        val holder = ViewHolder(view)

        view.setOnClickListener {
            currentIndexListener(holder.index)
        }

        return holder
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val cmd = items[position]

        holder.itemView.let {
            if (it is MaterialCardView) {
                val defaultColor = R.color.colorAccent

                val colorRes = when (cmd) {
                    is SkillMakerEntry.Next -> R.color.colorStageChange

                    is SkillMakerEntry.Action -> when (cmd.action) {
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

                        else -> defaultColor
                    }

                    else -> defaultColor
                }

                val color = it.context.getColor(colorRes)

                it.setCardBackgroundColor(color)

                if (position == currentIndex) {
                    it.radius = 20f
                    it.strokeWidth = 5
                } else {
                    it.radius = 0f
                    it.strokeWidth = 0
                }
            }
        }

        holder.textView.text =
            if (cmd is SkillMakerEntry.Start) ">"
            else cmd.toString()

        holder.index = position
    }
}