package io.github.fate_grand_automata.prefs

import io.github.fate_grand_automata.prefs.core.AppendPrefsCore
import io.github.fate_grand_automata.scripts.prefs.IAppendPreferences

internal class AppendPrefs(
    val prefs: AppendPrefsCore
): IAppendPreferences {

    override var isAppend1Locked: Boolean by prefs.isAppend1Locked
    override var shouldUnlockAppend1: Boolean by prefs.shouldUnlockAppend1
    override var upgradeAppend1: Int by prefs.upgradeAppend1

    override var isAppend2Locked: Boolean by prefs.isAppend2Locked
    override var shouldUnlockAppend2: Boolean by prefs.shouldUnlockAppend2
    override var upgradeAppend2: Int by prefs.upgradeAppend2

    override var isAppend3Locked: Boolean by prefs.isAppend3Locked
    override var shouldUnlockAppend3: Boolean by prefs.shouldUnlockAppend3
    override var upgradeAppend3: Int by prefs.upgradeAppend3
}