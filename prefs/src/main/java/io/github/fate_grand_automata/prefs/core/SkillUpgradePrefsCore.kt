package io.github.fate_grand_automata.prefs.core

class SkillUpgradePrefsCore(maker: PrefMaker) {

    val shouldUpgradeSkill1 = maker.bool("shouldUpgradeSkill_1")
    val minSkill1 = maker.stringAsInt(key="minSkill1", default = 1)
    val upgradeSkill1 = maker.stringAsInt(key = "upgrade_skill_1")

    val shouldUpgradeSkill2 = maker.bool("shouldUpgradeSkill_2")
    val minSkill2 = maker.stringAsInt(key="minSkill_2", default = 1)
    val upgradeSkill2 = maker.stringAsInt(key = "upgrade_skill_2")
    val skill2Available = maker.bool("isSkill2Available")


    val shouldUpgradeSkill3 = maker.bool("shouldUpgradeSkill_3")
    val minSkill3 = maker.stringAsInt(key="minSkill_3", default = 1)
    val upgradeSkill3 = maker.stringAsInt(key = "upgrade_skill_3")
    val skill3Available = maker.bool("isSkill3Available")
}