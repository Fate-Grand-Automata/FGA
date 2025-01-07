package io.github.fate_grand_automata.prefs

import io.github.fate_grand_automata.prefs.core.SkillPrefsCore
import io.github.fate_grand_automata.scripts.prefs.ISkillPreferences

internal class SkillPrefs(
    val prefs: SkillPrefsCore
): ISkillPreferences {
    override var skillOneCurrentLevel: Int = 1
    override var skillOneTargetLevel: Int by prefs.skillOneTargetLevel

    override var skillTwoCurrentLevel: Int = 1
    override var skillTwoTargetLevel: Int by prefs.skillTwoTargetLevel
    override var isSkillTwoAvailable: Boolean = false

    override var skillThreeCurrentLevel: Int = 1
    override var skillThreeTargetLevel: Int by prefs.skillThreeTargetLevel
    override var isSkillThreeAvailable: Boolean = false

    override var isEmptyEnhance: Boolean = false
}