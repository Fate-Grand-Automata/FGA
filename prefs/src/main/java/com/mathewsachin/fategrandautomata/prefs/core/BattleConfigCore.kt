package com.mathewsachin.fategrandautomata.prefs.core

import android.content.Context
import android.content.SharedPreferences
import com.mathewsachin.fategrandautomata.prefs.defaultCardPriority
import com.mathewsachin.fategrandautomata.scripts.enums.BraveChainEnum
import com.mathewsachin.fategrandautomata.scripts.enums.ShuffleCardsEnum
import com.mathewsachin.fategrandautomata.scripts.enums.SpamEnum

class BattleConfigCore(
    val id: String,
    val context: Context
) {
    val sharedPrefs: SharedPreferences = context.getSharedPreferences(
        id,
        Context.MODE_PRIVATE
    )

    private val maker = PrefMaker(sharedPrefs)

    val name = maker.string("autoskill_name", "--")
    val skillCommand = maker.string("autoskill_cmd")
    val notes = maker.string("autoskill_notes")

    val cardPriority = maker.string("card_priority", defaultCardPriority)

    var rearrangeCards by maker.string("auto_skill_rearrange_cards")
        .map({
            it.split(",").map { m -> m == "T" }
        }, {
            it.joinToString(",") { m -> if (m) "T" else "F" }
        })

    var braveChains by maker.string("auto_skill_brave_chains")
        .map({
            it.split(",").map { m ->
                try {
                    enumValueOf(m)
                } catch (e: Exception) {
                    BraveChainEnum.None
                }
            }
        }, {
            it.joinToString(",") { m -> m.toString() }
        })

    val shuffleCards = maker.enum("shuffle_cards", ShuffleCardsEnum.None)
    val shuffleCardsWave = maker.int("shuffle_cards_wave", 3)

    val party = maker.stringAsInt("autoskill_party", -1)
    val materials = maker.stringSet("battle_config_mat")

    val support = SupportPrefsCore(maker)

    val npSpam = maker.enum("battle_np", SpamEnum.None)
    val skillSpam = maker.enum("skill_spam", SpamEnum.None)
    val autoChooseTarget = maker.bool("auto_choose_target")
}