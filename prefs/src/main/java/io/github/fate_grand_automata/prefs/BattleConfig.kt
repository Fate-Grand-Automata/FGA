package io.github.fate_grand_automata.prefs

import io.github.fate_grand_automata.prefs.core.BattleConfigCore
import io.github.fate_grand_automata.prefs.core.PrefsCore
import io.github.fate_grand_automata.prefs.core.map
import io.github.fate_grand_automata.scripts.prefs.IBattleConfig

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

    override val servantPriority by prefs.servantPriority
    override val useServantPriority by prefs.useServantPriority

    override val chainPriority by prefs.chainPriority
    override val useChainPriority by prefs.useChainPriority

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