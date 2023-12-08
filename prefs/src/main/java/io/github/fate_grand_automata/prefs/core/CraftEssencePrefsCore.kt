package io.github.fate_grand_automata.prefs.core

import io.github.fate_grand_automata.scripts.enums.CEDisplayChangeAreaEnum

class CraftEssencePrefsCore(maker: PrefMaker) {

    val emptyEnhance = maker.bool("emptyEnhance")

    val skipAutoLockTargetCE = maker.bool("skip_auto_lock_target_ce")

    val useDragging = maker.bool("use_dragging", default = true)

    val skipAutomaticDisplayChange = maker.bool("skip_automatic_display_change")

    val canShowAutomaticDisplayChange=  maker.bool("can_show_automatic_display_change", true)

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

    val skipSortDetection = maker.bool("skip_sort_detection")

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