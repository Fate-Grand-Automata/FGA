package io.github.fate_grand_automata.prefs.core

class FriendGachaPrefsCore(maker: PrefMaker) {

    val shouldLimitFP = maker.bool("should_fp_limit")
    val limitFP = maker.int("fp_limit", 1)

    val shouldCreateCEBombAfterSummon = maker.bool("should_create_ce_bomb_after_summon")
}