package io.github.fate_grand_automata.prefs.core

class ServantEnhancementPrefsCore(maker: PrefMaker) {

    val shouldLimit = maker.bool("limit_servant_enhancement")

    val limitCount = maker.stringAsInt("limit_servant_enhancement_count", default = 1)
}