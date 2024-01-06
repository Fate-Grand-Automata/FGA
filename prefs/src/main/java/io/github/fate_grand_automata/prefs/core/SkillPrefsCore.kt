package io.github.fate_grand_automata.prefs.core

class SkillPrefsCore(maker: PrefMaker) {

    val shouldUpgradeSkillOne = maker.bool("skill_should_upgrade_one")
    val minimumSkillOne = maker.stringAsInt(key="skill_one_minimum", default = 1)
    val skillOneUpgradeValue = maker.stringAsInt(key = "skill_one_upgrade_value")

    val shouldUpgradeSkillTwo = maker.bool("skill_should_upgrade_two")
    val minimumSkillTwo = maker.stringAsInt(key="skill_two_minimum", default = 1)
    val skillTwoUpgradeValue = maker.stringAsInt(key = "skill_two_upgrade_value")
    val isSkillTwoAvailable = maker.bool("skill_is_two_available")


    val shouldUpgradeSkillThree = maker.bool("skill_should_upgrade_three")
    val minimumSkillThree = maker.stringAsInt(key="skill_three_minimum", default = 1)
    val skillThreeUpgradeValue = maker.stringAsInt(key = "skill_three_upgrade_value")
    val isSkillThreeAvailable = maker.bool("skill_is_three_available")
}