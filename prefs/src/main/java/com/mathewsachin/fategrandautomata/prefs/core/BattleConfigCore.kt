package com.mathewsachin.fategrandautomata.prefs.core

import android.content.Context
import android.content.SharedPreferences
import com.mathewsachin.fategrandautomata.prefs.R
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

    private val maker = PrefMaker(sharedPrefs, context)

    val name = maker.string(R.string.pref_battle_config_name, "--")

    val skillCommand = maker.string(R.string.pref_battle_config_cmd)

    val cardPriority = maker.string(
        R.string.pref_card_priority,
        defaultCardPriority
    )

    val experimental = maker.bool(R.string.pref_battle_config_experimental)

    var rearrangeCards by maker.string(R.string.pref_battle_config_rearrange_cards)
        .map({
            it.split(",").map { m -> m == "T" }
        }, {
            it.joinToString(",") { m -> if (m) "T" else "F" }
        })

    var braveChains by maker.string(R.string.pref_battle_config_brave_chains)
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

    val shuffleCards = maker.enum(R.string.pref_shuffle_cards, ShuffleCardsEnum.None)
    val shuffleCardsWave = maker.int(R.string.pref_shuffle_cards_wave, 3)

    val party = maker.stringAsInt(R.string.pref_battle_config_party, -1)

    val materials = maker.stringSet(R.string.pref_battle_config_mat)

    val support = SupportPrefsCore(maker)

    val npSpam = maker.enum(
        R.string.pref_spam_np,
        SpamEnum.None
    )

    val skillSpam = maker.enum(
        R.string.pref_spam_skill,
        SpamEnum.None
    )

    val autoChooseTarget = maker.bool(R.string.pref_auto_choose_target)
}