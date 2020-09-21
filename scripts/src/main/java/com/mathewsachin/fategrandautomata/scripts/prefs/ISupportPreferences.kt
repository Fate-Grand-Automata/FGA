package com.mathewsachin.fategrandautomata.scripts.prefs

import com.mathewsachin.fategrandautomata.SupportStore
import com.mathewsachin.fategrandautomata.scripts.enums.SupportClass
import com.mathewsachin.fategrandautomata.scripts.enums.SupportSelectionModeEnum

interface ISupportPreferences {
    val friendNames: List<SupportStore.SupportImage.File>
    val preferredServants: List<SupportStore.SupportImage.File>
    val mlb: Boolean
    val preferredCEs: List<SupportStore.SupportImage.File>
    val friendsOnly: Boolean
    val selectionMode: SupportSelectionModeEnum
    val fallbackTo: SupportSelectionModeEnum
    val supportClass: SupportClass

    val maxAscended: Boolean

    val skill1Max: Boolean
    val skill2Max: Boolean
    val skill3Max: Boolean
}