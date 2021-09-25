package com.mathewsachin.fategrandautomata.prefs

import com.mathewsachin.fategrandautomata.prefs.core.BattleConfigCore
import com.mathewsachin.fategrandautomata.prefs.core.PrefsCore
import com.mathewsachin.fategrandautomata.prefs.core.map
import com.mathewsachin.fategrandautomata.scripts.prefs.IBattleConfig

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

    override val server by prefs.server.map(
        defaultValue = null,
        convert = { it.asGameServer() },
        reverse = {
            when (it) {
                null -> BattleConfigCore.Server.NotSet
                else -> BattleConfigCore.Server.Set(it)
            }
        }
    )

    override var spam by prefs.spam

    override fun export(): Map<String, *> = prefs.export()

    override fun import(map: Map<String, *>) =
        prefs.import(map)

    override fun equals(other: Any?): Boolean {
        if (other is IBattleConfig) {
            return other.id == id
        }

        return super.equals(other)
    }

    override fun hashCode() = id.hashCode()
}