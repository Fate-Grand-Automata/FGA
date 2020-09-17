package com.mathewsachin.fategrandautomata.ui.card_priority

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.scripts.enums.BraveChainEnum
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.card_priority.*

@AndroidEntryPoint
class CardPriorityFragment : Fragment(R.layout.card_priority) {
    val vm: CardPriorityViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setHasOptionsMenu(true)

        val adapter = CardPriorityListAdapter(
            vm.cardPriorityItems,
            vm.experimental,
            viewLifecycleOwner
        )

        vm.experimental.observe(viewLifecycleOwner) {
            experimental_switch.isChecked = it
        }

        experimental_switch.setOnCheckedChangeListener { _, isChecked ->
            vm.setExperimental(isChecked)
        }

        val recyclerView = card_priority_list
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        card_priority_add_btn.setOnClickListener {
            vm.cardPriorityItems.add(
                CardPriorityListItem(
                    vm.cardPriorityItems[0].scores.toMutableList(),
                    false,
                    BraveChainEnum.None
                )
            )

            adapter.notifyItemInserted(vm.cardPriorityItems.lastIndex)
        }

        card_priority_rm_btn.setOnClickListener {
            if (vm.cardPriorityItems.size > 1) {
                vm.cardPriorityItems.removeAt(vm.cardPriorityItems.lastIndex)

                adapter.notifyItemRemoved(vm.cardPriorityItems.lastIndex + 1)
            }
        }
    }

    override fun onPause() {
        super.onPause()

        vm.save()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.card_priority_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_card_priority_info -> {
                AlertDialog.Builder(requireContext())
                    .setMessage(R.string.card_priority_info_content)
                    .setTitle(R.string.card_priority_info_title)
                    .setPositiveButton(android.R.string.yes, null)
                    .show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
