package com.mathewsachin.fategrandautomata.scripts.prefs

import com.mathewsachin.fategrandautomata.scripts.enums.BraveChainEnum

interface IAutoSkillPreferences {
    val id: String
    var name: String
    var skillCommand: String
    var cardPriority: String
    val rearrangeCards: List<Boolean>
    val braveChains: List<BraveChainEnum>
    val party: Int
    val support: ISupportPreferences

    fun export(): Map<String, *>

    fun import(map: Map<String, *>)
}