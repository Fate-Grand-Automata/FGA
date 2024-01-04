package io.github.fate_grand_automata.prefs.core

class ServantEnhancementPrefsCore(maker: PrefMaker) {

    val shouldLimit = maker.bool("level_limit_servant_enhancement")

    val limitCount = maker.stringAsInt("level_limit_servant_enhancement_count", default = 1)

    val shouldRedirectAscension = maker.bool("level_redirect_servant_ascension")

    val shouldPerformAscension = maker.bool("level_perform_servant_ascension")

    val shouldRedirectGrail = maker.bool("level_redirect_servant_grail")

    val muteNotifications = maker.bool("level_mute_servant_enhancement_notifications")
}