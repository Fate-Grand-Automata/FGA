package com.mathewsachin.fategrandautomata.prefs.core

import android.content.Context
import com.mathewsachin.fategrandautomata.StorageDirs
import com.mathewsachin.fategrandautomata.prefs.R
import com.mathewsachin.fategrandautomata.prefs.defaultCardPriority

class AutoSkillPrefsCore(
    val id: String,
    val context: Context,
    val storageDirs: StorageDirs
) {
    val sharedPrefs = context.getSharedPreferences(
        id,
        Context.MODE_PRIVATE
    )

    private val maker = PrefMaker(sharedPrefs, context)

    val name = maker.string(R.string.pref_autoskill_name, "--")

    val skillCommand = maker.string(R.string.pref_autoskill_cmd)

    val cardPriority = maker.string(
        R.string.pref_card_priority,
        defaultCardPriority
    )

    val experimental = maker.bool(R.string.pref_auto_skill_experimental)

    var rearrangeCards by maker.string(R.string.pref_auto_skill_rearrange_cards)
        .map({
            it.split(",").map { m -> m == "T" }
        }, {
            it.joinToString(",") { m -> if (m) "T" else "F" }
        })

    val party = maker.stringAsInt(R.string.pref_autoskill_party, -1)

    val support = SupportPrefsCore(maker, storageDirs)

    val skill1Max = maker.bool(R.string.pref_support_skill_max_1)
    val skill2Max = maker.bool(R.string.pref_support_skill_max_2)
    val skill3Max = maker.bool(R.string.pref_support_skill_max_3)
}