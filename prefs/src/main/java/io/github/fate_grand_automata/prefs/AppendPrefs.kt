package io.github.fate_grand_automata.prefs

import io.github.fate_grand_automata.prefs.core.AppendPrefsCore
import io.github.fate_grand_automata.scripts.prefs.IAppendPreferences

internal class AppendPrefs(
    val prefs: AppendPrefsCore
): IAppendPreferences {

    override var appendOneLocked: Boolean by prefs.appendOneLocked
    override var shouldUnlockAppendOne: Boolean by prefs.shouldUnlockAppendOne
    override var upgradeAppendOne: Int by prefs.upgradeAppendOne

    override var appendTwoLocked: Boolean by prefs.appendTwoLocked
    override var shouldUnlockAppendTwo: Boolean by prefs.shouldUnlockAppendTwo
    override var upgradeAppendTwo: Int by prefs.upgradeAppendTwo

    override var appendThreeLocked: Boolean by prefs.appendThreeLocked
    override var shouldUnlockAppendThree: Boolean by prefs.shouldUnlockAppendThree
    override var upgradeAppendThree: Int by prefs.upgradeAppendThree
}