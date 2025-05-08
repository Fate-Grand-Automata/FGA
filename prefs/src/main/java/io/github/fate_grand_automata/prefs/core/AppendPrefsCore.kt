package io.github.fate_grand_automata.prefs.core

class AppendPrefsCore(maker: PrefMaker) {

    val appendOneLocked = maker.bool("append_one_Locked")
    val shouldUnlockAppendOne = maker.bool("append_one_should_unlock")
    val upgradeAppendOne = maker.stringAsInt(key = "append_one_upgrade", default = 0)

    val appendTwoLocked = maker.bool("append_two_Locked")
    val shouldUnlockAppendTwo = maker.bool("append_two_should_unlock")
    val upgradeAppendTwo = maker.stringAsInt(key = "append_two_upgrade", default = 0)

    val appendThreeLocked = maker.bool("append_three_Locked")
    val shouldUnlockAppendThree = maker.bool("append_three_should_unlock")
    val upgradeAppendThree = maker.stringAsInt(key = "append_three_upgrade", default = 0)

}