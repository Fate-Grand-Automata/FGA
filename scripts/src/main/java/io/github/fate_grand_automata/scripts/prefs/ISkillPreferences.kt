package io.github.fate_grand_automata.scripts.prefs

interface ISkillPreferences {

    var shouldUpgradeSkill1: Boolean
    var minSkill1: Int
    var upgradeSkill1: Int

    var shouldUpgradeSkill2: Boolean
    var minSkill2: Int
    var upgradeSkill2: Int
    var skill2Available: Boolean

    var shouldUpgradeSkill3: Boolean
    var minSkill3: Int
    var upgradeSkill3: Int
    var skill3Available: Boolean
    
}