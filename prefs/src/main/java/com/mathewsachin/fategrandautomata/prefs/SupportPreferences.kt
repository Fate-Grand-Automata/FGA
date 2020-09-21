package com.mathewsachin.fategrandautomata.prefs

import com.mathewsachin.fategrandautomata.SupportStore
import com.mathewsachin.fategrandautomata.prefs.core.SupportPrefsCore
import com.mathewsachin.fategrandautomata.prefs.core.map
import com.mathewsachin.fategrandautomata.scripts.prefs.ISupportPreferences

internal class SupportPreferences(
    val prefs: SupportPrefsCore,
    val supportStore: SupportStore
) : ISupportPreferences {
    override val friendNames by prefs.friendNames
        .map { it.map { m -> supportStore.getFriend(m) } }

    override val preferredServants by prefs.preferredServants
        .map {
            it.flatMap { x ->
                supportStore.getServants(x)
                    // Give priority to later ascensions
                    .sortedWith(compareByDescending(String.CASE_INSENSITIVE_ORDER) { m -> m.name })
            }
        }

    override val mlb by prefs.mlb

    override val preferredCEs by prefs.preferredCEs
        .map { it.map { m -> supportStore.getCE(m) } }

    override val friendsOnly by prefs.friendsOnly

    override val selectionMode by prefs.selectionMode

    override val fallbackTo by prefs.fallbackTo

    override val supportClass by prefs.supportClass

    override val maxAscended by prefs.maxAscended

    override val skill1Max by prefs.skill1Max
    override val skill2Max by prefs.skill2Max
    override val skill3Max by prefs.skill3Max
}