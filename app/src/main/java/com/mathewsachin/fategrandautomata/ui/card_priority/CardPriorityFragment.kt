package com.mathewsachin.fategrandautomata.ui.card_priority

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.prefs.defaultCardPriority
import com.mathewsachin.fategrandautomata.scripts.models.CardPriority
import com.mathewsachin.fategrandautomata.scripts.models.CardPriorityPerWave
import com.mathewsachin.fategrandautomata.scripts.models.CardScore
import com.mathewsachin.fategrandautomata.scripts.prefs.IAutoSkillPreferences
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.card_priority.*
import javax.inject.Inject

@AndroidEntryPoint
class CardPriorityFragment : Fragment(R.layout.card_priority) {
    private lateinit var cardScores: MutableList<MutableList<CardScore>>
    private lateinit var autoSkillPref: IAutoSkillPreferences

    val args: CardPriorityFragmentArgs by navArgs()

    val vm: CardPriorityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        vm.key = args.key
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
                    false
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
                    .setMessage("W: Weak (Effective)\nR: Resistive\n\nB: Buster\nA: Arts\nQ: Quick")
                    .setTitle("Info")
                    .setPositiveButton(android.R.string.yes, null)
                    .show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
