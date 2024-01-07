package io.github.fate_grand_automata.prefs

import io.github.fate_grand_automata.prefs.core.SkillPrefsCore
import io.github.fate_grand_automata.scripts.prefs.ISkillPreferences

internal class SkillPrefs(
    val prefs: SkillPrefsCore
): ISkillPreferences {
    override var shouldUpgradeSkillOne: Boolean by prefs.shouldUpgradeSkillOne
    override var minimumSkillOne: Int by prefs.minimumSkillOne
    override var skillOneUpgradeValue: Int by prefs.skillOneUpgradeValue

    override var shouldUpgradeSkillTwo: Boolean by prefs.shouldUpgradeSkillTwo
    override var minimumSkillTwo: Int by prefs.minimumSkillTwo
    override var skillTwoUpgradeValue: Int by prefs.skillTwoUpgradeValue
    override var isSkillTwoAvailable: Boolean by prefs.isSkillTwoAvailable

    override var shouldUpgradeSkillThree: Boolean by prefs.shouldUpgradeSkillThree
    override var minimumSkillThree: Int by prefs.minimumSkillThree
    override var skillThreeUpgradeValue: Int by prefs.skillThreeUpgradeValue
    override var isSkillThreeAvailable: Boolean by prefs.isSkillThreeAvailable
}