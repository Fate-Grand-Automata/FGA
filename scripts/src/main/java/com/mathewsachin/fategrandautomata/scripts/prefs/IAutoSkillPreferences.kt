package com.mathewsachin.fategrandautomata.scripts.prefs

interface IAutoSkillPreferences {
    val id: String
    var name: String
    var skillCommand: String
    var cardPriority: String
    val rearrangeCards: List<Boolean>
    val party: Int
    val support: ISupportPreferences

    val skill1Max: Boolean
    val skill2Max: Boolean
    val skill3Max: Boolean

    fun export(): Map<String, *>

    fun import(map: Map<String, *>)
}