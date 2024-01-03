package io.github.fate_grand_automata.prefs.core

class ServantEnhancementPrefsCore(maker: PrefMaker) {

    val shouldLimit = maker.bool("limit_servant_enhancement")

    val limitCount = maker.stringAsInt("limit_servant_enhancement_count", default = 1)

    val shouldRedirectAscension = maker.bool("redirect_servant_ascension")

    val shouldPerformAscension = maker.bool("perform_servant_ascension")

    val shouldRedirectGrail = maker.bool("redirect_servant_grail")

    val muteNotifications = maker.bool("mute_servant_enhancement_notifications")
}