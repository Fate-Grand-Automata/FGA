package com.mathewsachin.fategrandautomata.ui.card_priority

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.databinding.CardPriorityBinding
import com.mathewsachin.fategrandautomata.scripts.enums.BraveChainEnum
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CardPriorityFragment : Fragment() {
    val vm: CardPriorityViewModel by viewModels()

    lateinit var binding: CardPriorityBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        CardPriorityBinding.inflate(inflater, container, false)
            .also { binding = it }
            .root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setHasOptionsMenu(true)

        val adapter = CardPriorityListAdapter(vm.cardPriorityItems)

        val recyclerView = binding.cardPriorityList
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        binding.cardPriorityAddBtn.setOnClickListener {
            vm.cardPriorityItems.add(
                CardPriorityListItem(
                    vm.cardPriorityItems[0].scores.toMutableList(),
                    false,
                    BraveChainEnum.None
                )
            )

            adapter.notifyItemInserted(vm.cardPriorityItems.lastIndex)
        }

        binding.cardPriorityRmBtn.setOnClickListener {
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
