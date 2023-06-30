package io.github.fate_grand_automata.scripts.prefs

import io.github.fate_grand_automata.scripts.enums.RefillResourceEnum

interface IRefillPreferences {
    var repetitions: Int
    val resources: List<RefillResourceEnum>
    fun updateResources(resources: Set<RefillResourceEnum>)

    var shouldLimitRuns: Boolean
    var limitRuns: Int

    var shouldLimitMats: Boolean
    var limitMats: Int

    var shouldLimitCEs: Boolean
    var limitCEs: Int
}