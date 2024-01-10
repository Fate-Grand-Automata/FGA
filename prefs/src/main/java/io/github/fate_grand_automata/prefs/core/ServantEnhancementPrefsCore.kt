package io.github.fate_grand_automata.prefs.core

/**
 * @param shouldLimit Limit the number of servants to be enhanced
 * @param limitCount The number of servants to be enhanced
 * @param shouldRedirectAscension If the servant is max level, it will check if it can redirect to ascension
 * @param shouldPerformAscension If the servant is max level, it will perform ascension after redirecting to ascension
 * @param shouldRedirectGrail If the servant is max level, it will check if it can redirect to grail
 * @param muteNotifications Mute notifications
 */
class ServantEnhancementPrefsCore(maker: PrefMaker) {

    val shouldLimit = maker.bool("level_limit_servant_enhancement")

    val limitCount = maker.stringAsInt("level_limit_servant_enhancement_count", default = 1)

    val shouldRedirectAscension = maker.bool("level_redirect_servant_ascension")

    val shouldPerformAscension = maker.bool("level_perform_servant_ascension")

    val shouldRedirectGrail = maker.bool("level_redirect_servant_grail")

    val muteNotifications = maker.bool("level_mute_servant_enhancement_notifications")
}