package com.mathewsachin.fategrandautomata.ui.card_priority

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.scripts.enums.BraveChainEnum
import com.mathewsachin.fategrandautomata.util.ItemTouchHelperCallback
import com.mathewsachin.fategrandautomata.util.stringRes

class CardPriorityListAdapter(
    private val Items: List<CardPriorityListItem>,
    private val experimental: LiveData<Boolean>,
    private val lifecycleOwner: LifecycleOwner
) :
    RecyclerView.Adapter<CardPriorityListAdapter.ViewHolder>() {
    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val battleStageTextView: TextView = ItemView.findViewById(R.id.card_priority_battle_stage)

        val itemsRecyclerView: RecyclerView = ItemView.findViewById(R.id.card_priority_items)

        val rearrangeCardsSwitchView: SwitchCompat = ItemView.findViewById(R.id.rearrange_cards)

        val braveChainsLabel: TextView = ItemView.findViewById(R.id.brave_chains_label)

        val braveChainsSpinner: Spinner = ItemView.findViewById(R.id.brave_chains)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_priority_list_item, parent, false)

        return ViewHolder(view).also { holder ->
            experimental.observe(lifecycleOwner) {
                val visible = if (it) View.VISIBLE else View.GONE
                holder.rearrangeCardsSwitchView.visibility = visible
                holder.braveChainsLabel.visibility = visible
                holder.braveChainsSpinner.visibility = visible
            }

            val context = holder.itemView.context
            val items = enumValues<BraveChainEnum>()
                .map {
                    context.getString(it.stringRes)
                }
                .toTypedArray()

            ArrayAdapter(
                context,
                android.R.layout.simple_spinner_item,
                items
            ).also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

                holder.braveChainsSpinner.adapter = adapter
            }
        }
    }

    override fun getItemCount() = Items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.battleStageTextView.text =
            holder.itemView.context.getString(R.string.card_priority_wave_number, position + 1)

        holder.rearrangeCardsSwitchView.setOnCheckedChangeListener { _, isChecked ->
            Items[position].rearrangeCards = isChecked
        }
        holder.rearrangeCardsSwitchView.isChecked = Items[position].rearrangeCards

        holder.braveChainsSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                val value = BraveChainEnum.values()[pos]

                Items[position].braveChains = value

                holder.rearrangeCardsSwitchView.isEnabled = value != BraveChainEnum.Avoid
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                holder.braveChainsSpinner.setSelection(0)
            }
        }
        holder.braveChainsSpinner.setSelection(Items[position].braveChains.ordinal)

        val adapter = CardPriorityAdapter(Items[position].scores)

        val recyclerView = holder.itemsRecyclerView
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = adapter
        recyclerView.layoutManager =
            LinearLayoutManager(holder.itemView.context, LinearLayoutManager.HORIZONTAL, false)

        val callback = ItemTouchHelperCallback(adapter)
        val itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper.attachToRecyclerView(recyclerView)

        adapter.itemTouchHelper = itemTouchHelper
    }
}