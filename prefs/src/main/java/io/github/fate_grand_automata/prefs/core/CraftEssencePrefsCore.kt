package io.github.fate_grand_automata.prefs.core

class CraftEssencePrefsCore(maker: PrefMaker) {

    val emptyEnhance = maker.bool("emptyEnhance")

    val skipAutomaticDisplayChange = maker.bool("skip_automatic_display_change")

    /**
     * If true, uses top right corner to change display.
     * If false, uses bottom left corner to change display.
     */
    val topRightDisplayLocation = maker.bool("automatic_display_right_click", default = true)

    val ceTargetRarity = maker.int("ce_target_rarity", 1)

    val skipCEFilterDetection = maker.bool("skip_ce_filter_detection")

    val ceFodderRarity = maker.stringSet("ce_fodder_rarity").map(
        defaultValue = setOf(1, 2),
        convert = {
            it
                .mapNotNull { value ->
                    try {
                        value.toInt()
                    } catch (e: Exception) {
                        1
                    }
                }
                .toSet()
        },
        reverse = { it.map { m -> m.toString() }.toSet() }
    )
}