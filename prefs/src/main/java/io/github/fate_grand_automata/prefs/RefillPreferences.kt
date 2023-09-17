package io.github.fate_grand_automata.prefs

import io.github.fate_grand_automata.prefs.core.RefillPrefsCore
import io.github.fate_grand_automata.prefs.core.map
import io.github.fate_grand_automata.scripts.enums.RefillResourceEnum
import io.github.fate_grand_automata.scripts.prefs.IRefillPreferences

internal class RefillPreferences(
    val prefs: RefillPrefsCore
) : IRefillPreferences {

    override val resources by prefs.resources.map { set ->
        set.sortedBy { it.ordinal }
    }

    override fun updateResources(resources: Set<RefillResourceEnum>) =
        prefs.resources.set(resources)

    override var shouldLimitRuns by prefs.shouldLimitRuns
    override var limitRuns by prefs.limitRuns

    override var shouldLimitMats by prefs.shouldLimitMats
    override var limitMats by prefs.limitMats

    override var shouldLimitCEs by prefs.shouldLimitCEs
    override var limitCEs by prefs.limitCEs
}