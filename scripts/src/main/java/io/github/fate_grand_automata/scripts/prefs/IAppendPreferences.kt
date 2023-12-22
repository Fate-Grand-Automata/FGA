package io.github.fate_grand_automata.scripts.prefs

interface IAppendPreferences {

    var isAppend1Locked: Boolean
    var shouldUnlockAppend1: Boolean
    var upgradeAppend1: Int

    var isAppend2Locked: Boolean
    var shouldUnlockAppend2: Boolean
    var upgradeAppend2: Int

    var isAppend3Locked: Boolean
    var shouldUnlockAppend3: Boolean
    var upgradeAppend3: Int
}