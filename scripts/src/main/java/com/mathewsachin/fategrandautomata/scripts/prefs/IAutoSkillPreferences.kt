package com.mathewsachin.fategrandautomata.scripts.prefs

interface IAutoSkillPreferences {
    val id: String
    var name: String
    var skillCommand: String
    var cardPriority: String
    val party: Int
    val support: ISupportPreferences

    fun export(): Map<String, *>

    fun import(map: Map<String, *>)
}