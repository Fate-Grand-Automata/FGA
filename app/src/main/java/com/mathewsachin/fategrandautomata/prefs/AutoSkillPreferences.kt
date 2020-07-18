package com.mathewsachin.fategrandautomata.prefs

import android.content.Context
import com.mathewsachin.fategrandautomata.R

const val defaultCardPriority = "WB, WA, WQ, B, A, Q, RB, RA, RQ"

class AutoSkillPreferences(
    val id: String,
    val context: Context
) {
    private val prefs =
        SharedPreferenceDelegation(
            context.getSharedPreferences(
                id,
                Context.MODE_PRIVATE
            ),
            context
        )

    val name by prefs.string(R.string.pref_autoskill_name, "--")

    var skillCommand by prefs.string(R.string.pref_autoskill_cmd)

    var cardPriority by prefs.string(
        R.string.pref_card_priority,
        defaultCardPriority
    )

    val party by prefs.int(R.string.pref_autoskill_party, -1)

    val support = SupportPreferences(prefs)
}