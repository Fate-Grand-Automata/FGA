package io.github.fate_grand_automata.prefs

import io.github.fate_grand_automata.prefs.core.SkillUpgradePrefsCore
import io.github.fate_grand_automata.scripts.prefs.ISkillUpgradePreferences

internal class SkillUpgradePrefs(
    val prefs: SkillUpgradePrefsCore
): ISkillUpgradePreferences {
    override var shouldUpgradeSkill1: Boolean by prefs.shouldUpgradeSkill1
    override var minSkill1: Int by prefs.minSkill1
    override var upgradeSkill1: Int by prefs.upgradeSkill1

    override var shouldUpgradeSkill2: Boolean by prefs.shouldUpgradeSkill2
    override var minSkill2: Int by prefs.minSkill2
    override var upgradeSkill2: Int by prefs.upgradeSkill2
    override var skill2Available: Boolean by prefs.skill2Available

    override var shouldUpgradeSkill3: Boolean by prefs.shouldUpgradeSkill3
    override var minSkill3: Int by prefs.minSkill3
    override var upgradeSkill3: Int by prefs.upgradeSkill3
    override var skill3Available: Boolean by prefs.skill3Available
}