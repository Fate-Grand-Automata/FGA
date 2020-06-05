package com.mathewsachin.fategrandautomata.ui.auto_skill_maker

import android.view.LayoutInflater
import android.view.ViewGroup
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
        holder.textView.text = Items[position]
    }
}