package com.mathewsachin.fategrandautomata.scripts.prefs

interface IAutoSkillPreferences {
    val id: String
    val name: String
    var skillCommand: String
    var cardPriority: String
    val party: Int
    val support: ISupportPreferences
}