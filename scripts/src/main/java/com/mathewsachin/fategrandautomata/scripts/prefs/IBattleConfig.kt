package com.mathewsachin.fategrandautomata.scripts.prefs

import com.mathewsachin.fategrandautomata.scripts.enums.BraveChainEnum
import com.mathewsachin.fategrandautomata.scripts.enums.MaterialEnum
import com.mathewsachin.fategrandautomata.scripts.enums.ShuffleCardsEnum
import com.mathewsachin.fategrandautomata.scripts.enums.SpamEnum

interface IBattleConfig {
    val id: String
    var name: String
    var skillCommand: String
    var cardPriority: String
    val rearrangeCards: List<Boolean>
    val braveChains: List<BraveChainEnum>
    val party: Int
    val materials: List<MaterialEnum>
    val support: ISupportPreferences
    val shuffleCards: ShuffleCardsEnum
    val shuffleCardsWave: Int

    val npSpam: SpamEnum
    val skillSpam: SpamEnum
    val autoChooseTarget: Boolean

    fun export(): Map<String, *>

    fun import(map: Map<String, *>)
}