package io.github.fate_grand_automata.scripts.models

import io.github.fate_grand_automata.scripts.enums.CardAffinityEnum
import io.github.fate_grand_automata.scripts.enums.CardTypeEnum

data class CardScore(val type: CardTypeEnum, val affinity: CardAffinityEnum) {
    private fun String.filterCapitals(): String {
        return this
            .asSequence()
            .filter { it.isUpperCase() }
            .joinToString(separator = "")
    }

    override fun toString(): String {
        var result = ""

        if (affinity != CardAffinityEnum.Normal) {
            result += "$affinity "
        }

        result += type

        return result.filterCapitals()
    }
}