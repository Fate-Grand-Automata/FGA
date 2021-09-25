package com.mathewsachin.fategrandautomata.scripts.prefs

import com.mathewsachin.fategrandautomata.scripts.enums.BraveChainEnum
import com.mathewsachin.fategrandautomata.scripts.enums.GameServerEnum
import com.mathewsachin.fategrandautomata.scripts.enums.MaterialEnum
import com.mathewsachin.fategrandautomata.scripts.enums.ShuffleCardsEnum
import com.mathewsachin.fategrandautomata.scripts.models.CardPriorityPerWave
import com.mathewsachin.fategrandautomata.scripts.models.ServantSpamConfig

interface IBattleConfig {
    val id: String
    var name: String
    var skillCommand: String
    var cardPriority: CardPriorityPerWave
    val rearrangeCards: List<Boolean>
    val braveChains: List<BraveChainEnum>
    val party: Int
    val materials: Set<MaterialEnum>
    val support: ISupportPreferences
    val shuffleCards: ShuffleCardsEnum
    val shuffleCardsWave: Int

    var spam: List<ServantSpamConfig>
    val autoChooseTarget: Boolean

    val server: GameServerEnum?

    fun export(): Map<String, *>

    fun import(map: Map<String, *>)
}