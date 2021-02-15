package com.mathewsachin.fategrandautomata.prefs

import androidx.core.content.edit
import com.mathewsachin.fategrandautomata.prefs.core.PrefsCore
import com.mathewsachin.fategrandautomata.scripts.prefs.IBattleConfig

const val defaultCardPriority = "WB, WA, WQ, B, A, Q, RB, RA, RQ"

internal class BattleConfig(
    override val id: String,
    prefsCore: PrefsCore
) : IBattleConfig {
    val prefs = prefsCore.forBattleConfig(id)

    override var name by prefs.name
    override var skillCommand by prefs.skillCommand

    override var cardPriority by prefs.cardPriority
    override val rearrangeCards by prefs.rearrangeCards
    override val braveChains by prefs.braveChains

    override val party by prefs.party
    override val materials by prefs.materials

    override val shuffleCards by prefs.shuffleCards
    override val shuffleCardsWave by prefs.shuffleCardsWave

    override val support = SupportPreferences(prefs.support)

    override val autoChooseTarget by prefs.autoChooseTarget

    override var spam by prefs.spam

    override fun export(): Map<String, *> = prefs.sharedPrefs.all

    override fun import(map: Map<String, *>) =
        prefs.sharedPrefs.edit {
            import(map)
        }

    override fun equals(other: Any?): Boolean {
        if (other is IBattleConfig) {
            return other.id == id
        }

        return super.equals(other)
    }

    override fun hashCode() = id.hashCode()
}