package com.mathewsachin.fategrandautomata.ui.card_priority

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.edit
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.scripts.CardScore
import com.mathewsachin.fategrandautomata.scripts.modules.getCardScores
import com.mathewsachin.fategrandautomata.scripts.prefs.defaultCardPriority
import com.mathewsachin.fategrandautomata.scripts.prefs.defaultPrefs
import com.mathewsachin.fategrandautomata.scripts.prefs.getStringPref
import com.mathewsachin.fategrandautomata.util.IOnStartDragListener
import com.mathewsachin.fategrandautomata.util.ItemTouchHelperCallback
import kotlinx.android.synthetic.main.card_priority.*

fun String.filterCapitals(): String {
    return this
        .asSequence()
        .filter { it.isUpperCase() }
        .joinToString(separator = "")
}

class CardPriorityActivity : AppCompatActivity(), IOnStartDragListener {
    private lateinit var itemTouchHelper: ItemTouchHelper
    private lateinit var cardScores: MutableList<CardScore>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.card_priority)

        var cardPriority = getStringPref(R.string.pref_card_priority)

        // Handle simple mode and empty string
        if (cardPriority.length == 3 || cardPriority.isBlank())
        {
            cardPriority = defaultCardPriority
        }

        cardScores = getCardScores(cardPriority).toMutableList()

        val adapter = CardPriorityAdapter(cardScores, this)

        val recyclerView = card_priority_lv
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        val callback = ItemTouchHelperCallback(adapter)
        itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    override fun onPause() {
        super.onPause()

        val value = cardScores.joinToString { it.toString().filterCapitals() }

        val key = getString(R.string.pref_card_priority)
        defaultPrefs.edit(commit = true) { putString(key, value) }
    }

    override fun onStartDrag(ViewHolder: RecyclerView.ViewHolder) {
        itemTouchHelper.startDrag(ViewHolder)
    }
}
