package io.github.fate_grand_automata.prefs.core

import io.github.fate_grand_automata.scripts.enums.CEDisplayChangeAreaEnum

class CraftEssencePrefsCore(maker: PrefMaker) {
    /**
     * Checks if the CE enhancement screen is empty.
     * If it is not empty, then the script will show the Target CE as there is already target CE selected.
     */
    val emptyEnhance = maker.bool("ce_empty_enhance")

    val skipAutoLockTargetCE = maker.bool("ce_skip_auto_lock_target")

    val useDragging = maker.bool("ce_use_dragging", default = true)

    val skipAutomaticDisplayChange = maker.bool("ce_skip_automatic_display_change")

    val canShowAutomaticDisplayChange=  maker.bool("ce_can_show_automatic_display_change", true)

    val ceDisplayChangeArea = maker.stringSet("ce_display_change_area").map(
        defaultValue = emptySet(),
        convert = {
            it
                .mapNotNull { m ->
                    try {
                        enumValueOf<CEDisplayChangeAreaEnum>(m)
                    } catch (e: Exception) {
                        null
                    }
                }
                .toSet()
        },
        reverse = { it.map { m -> m.name }.toSet() }
    )

    val ceTargetRarity = maker.int("ce_target_rarity", 1)

    val skipSortDetection = maker.bool("ce_skip_sort_detection")

    val skipCEFilterDetection = maker.bool("ce_skip_ce_filter_detection")

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