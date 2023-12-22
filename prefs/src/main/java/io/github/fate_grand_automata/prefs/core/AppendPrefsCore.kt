package io.github.fate_grand_automata.prefs.core

class AppendPrefsCore(maker: PrefMaker) {

    val isAppend1Locked = maker.bool("isAppend1Locked")
    val shouldUnlockAppend1 = maker.bool("shouldUnlockAppend_1")
    val upgradeAppend1 = maker.stringAsInt(key = "upgrade_append_1", default = 0)

    val isAppend2Locked = maker.bool("isAppend2Locked")
    val shouldUnlockAppend2 = maker.bool("shouldUnlockAppend_2")
    val upgradeAppend2 = maker.stringAsInt(key = "upgrade_append_2", default = 0)

    val isAppend3Locked = maker.bool("isAppend3Locked")
    val shouldUnlockAppend3 = maker.bool("shouldUnlockAppend_3")
    val upgradeAppend3 = maker.stringAsInt(key = "upgrade_append_3", default = 0)

}