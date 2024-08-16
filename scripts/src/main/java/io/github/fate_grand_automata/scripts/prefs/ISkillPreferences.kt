package io.github.fate_grand_automata.scripts.prefs

interface ISkillPreferences {

    var shouldUpgradeSkillOne: Boolean
    var minimumSkillOne: Int
    var skillOneUpgradeValue: Int

    var shouldUpgradeSkillTwo: Boolean
    var minimumSkillTwo: Int
    var skillTwoUpgradeValue: Int
    var isSkillTwoAvailable: Boolean

    var shouldUpgradeSkillThree: Boolean
    var minimumSkillThree: Int
    var skillThreeUpgradeValue: Int
    var isSkillThreeAvailable: Boolean

    var isEmptyEnhance: Boolean
}