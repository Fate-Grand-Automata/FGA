package io.github.fate_grand_automata.prefs.core

import io.github.fate_grand_automata.scripts.enums.RefillResourceEnum

class RefillPrefsCore(maker: PrefMaker) {

    val resources = maker.stringSet("refill_resource_x").map(
        defaultValue = emptySet(),
        convert = {
            it
                .mapNotNull { m ->
                    try {
                        enumValueOf<RefillResourceEnum>(m)
                    } catch (e: Exception) {
                        null
                    }
                }
                .toSet()
        },
        reverse = { it.map { m -> m.name }.toSet() },
    )

    val shouldLimitRuns = maker.bool("should_limit_runs")
    val limitRuns = maker.stringAsInt("limit_runs", 1)

    val shouldLimitMats = maker.bool("should_limit_mats")
    val limitMats = maker.stringAsInt("limit_mats", 1)

    val shouldLimitCEs = maker.bool("should_limit_ces")
    val limitCEs = maker.stringAsInt("limit_ces", 1)
}
