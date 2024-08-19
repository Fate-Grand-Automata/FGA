package io.github.fate_grand_automata.scripts.prefs

interface ISkillPreferences {

    var skillOneCurrentLevel: Int
    var skillOneTargetLevel: Int

    var skillTwoCurrentLevel: Int
    var skillTwoTargetLevel: Int
    var isSkillTwoAvailable: Boolean

    var skillThreeCurrentLevel: Int
    var skillThreeTargetLevel: Int
    var isSkillThreeAvailable: Boolean

    var isEmptyEnhance: Boolean
}