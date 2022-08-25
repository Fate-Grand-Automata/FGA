package com.mathewsachin.fategrandautomata.prefs

import com.mathewsachin.fategrandautomata.prefs.core.RefillPrefsCore
import com.mathewsachin.fategrandautomata.prefs.core.map
import com.mathewsachin.fategrandautomata.scripts.enums.RefillResourceEnum
import com.mathewsachin.fategrandautomata.scripts.prefs.IRefillPreferences

internal class RefillPreferences(
    val prefs: RefillPrefsCore
) : IRefillPreferences {
    override var repetitions by prefs.repetitions

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