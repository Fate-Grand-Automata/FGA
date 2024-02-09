package io.github.fate_grand_automata.prefs.core

/**
 * @param shouldUpgradeSkillOne: Whether to upgrade skill one
 * @param shouldUpgradeSkillTwo: Whether to upgrade skill two
 * @param shouldUpgradeSkillThree: Whether to upgrade skill three
 *
 * @param minimumSkillOne: Minimum skill level for skill one.
 * @param minimumSkillTwo: Minimum skill level for skill two
 * @param minimumSkillThree: Minimum skill level for skill three
 *        Serves as the baseline for skill leveling.
 *
 * @param skillOneUpgradeValue: Skill level to upgrade skill one to
 * @param skillTwoUpgradeValue: Skill level to upgrade skill two to
 * @param skillThreeUpgradeValue: Skill level to upgrade skill three to
 *          This value is added to the minimum skill level.
 *          For example, if minimumSkillOne is 1 and skillOneUpgradeValue is 2,
 *          then skill one will be upgraded to level 3.
 *          The Script would perform two loops of skill one upgrade.
 *
 * @param isSkillTwoAvailable: Whether skill two is available
 * @param isSkillThreeAvailable: Whether skill three is available
 *           Skill one is always available. This takes into account that the
 *          servant may not have skill two or three. Due to being in lower
 *          ascension or not being leveled up.
 */
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