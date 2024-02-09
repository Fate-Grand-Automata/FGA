package io.github.fate_grand_automata.scripts.prefs

interface IAppendPreferences {

    var appendOneLocked: Boolean
    var shouldUnlockAppendOne: Boolean
    var upgradeAppendOne: Int

    var appendTwoLocked: Boolean
    var shouldUnlockAppendTwo: Boolean
    var upgradeAppendTwo: Int

    var appendThreeLocked: Boolean
    var shouldUnlockAppendThree: Boolean
    var upgradeAppendThree: Int
}