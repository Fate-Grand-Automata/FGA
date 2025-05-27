package io.github.fate_grand_automata.prefs.core

class SkillPrefsCore(maker: PrefMaker) {
    /**
     * Skill level to upgrade skill 1 to
     */
    val skillOneTargetLevel = maker.stringAsInt(key = "skill_one_target_level")

    /**
     * Skill level to upgrade skill 2 to
     */
    val skillTwoTargetLevel = maker.stringAsInt(key = "skill_two_target_level")

    /**
     * Skill level to upgrade skill 3 to
     */
    val skillThreeTargetLevel = maker.stringAsInt(key = "skill_three_target_level")
}