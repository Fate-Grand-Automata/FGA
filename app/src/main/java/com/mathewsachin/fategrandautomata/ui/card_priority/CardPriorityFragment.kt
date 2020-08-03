package com.mathewsachin.fategrandautomata.ui.card_priority

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.prefs.defaultCardPriority
import com.mathewsachin.fategrandautomata.scripts.models.CardPriority
import com.mathewsachin.fategrandautomata.scripts.models.CardPriorityPerWave
import com.mathewsachin.fategrandautomata.scripts.models.CardScore
import com.mathewsachin.fategrandautomata.scripts.prefs.IAutoSkillPreferences
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences
import com.mathewsachin.fategrandautomata.util.appComponent
import kotlinx.android.synthetic.main.card_priority.*
import javax.inject.Inject

class CardPriorityFragment : Fragment() {
    private lateinit var cardScores: MutableList<MutableList<CardScore>>
    private lateinit var autoSkillPref: IAutoSkillPreferences

    val args: CardPriorityFragmentArgs by navArgs()

    @Inject
    lateinit var preferences: IPreferences

    override fun onAttach(context: Context) {
        super.onAttach(context)

        context.appComponent.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.card_priority, container, false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        autoSkillPref = preferences.forAutoSkillConfig(args.key)

        var cardPriority = autoSkillPref.cardPriority

        // Handle simple mode and empty string
        if (cardPriority.length == 3 || cardPriority.isBlank()) {
            cardPriority =
                defaultCardPriority
        }

        cardScores = CardPriorityPerWave.of(cardPriority)
            .map { it.toMutableList() }
            .toMutableList()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = CardPriorityListAdapter(cardScores)

        val recyclerView = card_priority_list
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        card_priority_add_btn.setOnClickListener {
            cardScores.add(mutableListOf<CardScore>().apply {
                addAll(cardScores[0])
            })

            adapter.notifyItemInserted(cardScores.lastIndex)
        }

        card_priority_rm_btn.setOnClickListener {
            if (cardScores.size > 1) {
                cardScores.removeAt(cardScores.lastIndex)

                adapter.notifyItemRemoved(cardScores.lastIndex + 1)
            }
        }
    }

    override fun onPause() {
        super.onPause()

        val value = CardPriorityPerWave.from(
            cardScores.map {
                CardPriority.from(it)
            }
        ).toString()

        autoSkillPref.cardPriority = value
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
