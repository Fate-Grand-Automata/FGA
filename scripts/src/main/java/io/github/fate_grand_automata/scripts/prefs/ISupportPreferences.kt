package io.github.fate_grand_automata.scripts.prefs

import io.github.fate_grand_automata.scripts.enums.CEMatchCountEnum
import io.github.fate_grand_automata.scripts.enums.SupportClass
import io.github.fate_grand_automata.scripts.enums.SupportSelectionModeEnum

interface ISupportPreferences {
    val friendNames: List<String>
    val preferredServants: List<String>
    val mlb: Boolean
    val preferredCEs: List<String>
    val friendsOnly: Boolean
    val selectionMode: SupportSelectionModeEnum
    val fallbackTo: SupportSelectionModeEnum
    val supportClass: SupportClass
    val alsoCheckAll: Boolean

    val maxAscended: Boolean

    val skill1Max: Boolean
    val skill2Max: Boolean
    val skill3Max: Boolean

    val grandServant: Boolean
    val ceMatchCount: CEMatchCountEnum
}