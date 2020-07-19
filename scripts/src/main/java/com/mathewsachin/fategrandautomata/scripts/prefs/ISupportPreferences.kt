package com.mathewsachin.fategrandautomata.scripts.prefs

import com.mathewsachin.fategrandautomata.scripts.enums.SupportSelectionModeEnum

interface ISupportPreferences {
    val friendNames: String
    val preferredServants: String
    val mlb: Boolean
    val preferredCEs: String
    val friendsOnly: Boolean
    val selectionMode: SupportSelectionModeEnum
    val fallbackTo: SupportSelectionModeEnum
}