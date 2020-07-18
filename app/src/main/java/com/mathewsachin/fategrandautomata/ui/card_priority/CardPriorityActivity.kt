package com.mathewsachin.fategrandautomata.ui.card_priority

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.recyclerview.widget.LinearLayoutManager
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.scripts.CardScore
import com.mathewsachin.fategrandautomata.scripts.modules.cardPriorityStageSeparator
import com.mathewsachin.fategrandautomata.scripts.modules.getCardScores
import com.mathewsachin.fategrandautomata.scripts.prefs.defaultCardPriority
import com.mathewsachin.fategrandautomata.scripts.prefs.getStringPref
import com.mathewsachin.fategrandautomata.ui.AutoSkillItemActivity
import kotlinx.android.synthetic.main.card_priority.*

fun String.filterCapitals(): String {
    return this
        .asSequence()
        .filter { it.isUpperCase() }
        .joinToString(separator = "")
}

class CardPriorityActivity : AppCompatActivity() {
    private lateinit var cardScores: MutableList<MutableList<CardScore>>

    private lateinit var autoSkillPref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.card_priority)

        val autoSkillKey = intent.getStringExtra(AutoSkillItemActivity::autoSkillItemKey.name)
            ?: throw IllegalArgumentException("Missing AutoSkill item key in intent")
        autoSkillPref = getSharedPreferences(autoSkillKey, Context.MODE_PRIVATE)

        var cardPriority = getStringPref(R.string.pref_card_priority, Prefs = autoSkillPref)

        // Handle simple mode and empty string
        if (cardPriority.length == 3 || cardPriority.isBlank())
        {
            cardPriority = defaultCardPriority
        }

        cardScores = cardPriority
            .splitToSequence(cardPriorityStageSeparator)
            .map { getCardScores(it).toMutableList() }
            .toMutableList()

        val adapter = CardPriorityListAdapter(cardScores)

        val recyclerView = card_priority_list
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

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

        val value = cardScores.joinToString(cardPriorityStageSeparator) {
            it.joinToString { m -> m.toString().filterCapitals() }
        }

        val key = getString(R.string.pref_card_priority)
        autoSkillPref.edit(commit = true) { putString(key, value) }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.card_priority_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_card_priority_info -> {
                AlertDialog.Builder(this)
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
