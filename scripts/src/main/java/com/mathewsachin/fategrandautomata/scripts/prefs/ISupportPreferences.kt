package com.mathewsachin.fategrandautomata.scripts.prefs

import com.mathewsachin.fategrandautomata.scripts.enums.SupportClass
import com.mathewsachin.fategrandautomata.scripts.enums.SupportSelectionModeEnum

interface ISupportPreferences {
    val friendNames: String
    val preferredServants: String
    val mlb: Boolean
    val preferredCEs: String
    val friendsOnly: Boolean
    val selectionMode: SupportSelectionModeEnum
    val fallbackTo: SupportSelectionModeEnum
    val supportClass: SupportClass

    val maxAscended: Boolean

    val skill1Max: Boolean
    val skill2Max: Boolean
    val skill3Max: Boolean
}