package io.github.fate_grand_automata.scripts.models

import io.github.fate_grand_automata.scripts.enums.CardTypeEnum

data class CustomCard(val fieldSlot: FieldSlot, val type: CardTypeEnum) {
    override fun toString(): String {
        val typeChar = when (type) {
            CardTypeEnum.Buster -> 'B'
            CardTypeEnum.Arts -> 'A'
            CardTypeEnum.Quick -> 'Q'
            else -> '?'
        }
        return "$typeChar${fieldSlot.position}"
    }
}
