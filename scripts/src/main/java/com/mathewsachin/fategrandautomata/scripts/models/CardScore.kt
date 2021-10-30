package com.mathewsachin.fategrandautomata.scripts.models

import com.mathewsachin.fategrandautomata.scripts.enums.CardAffinityEnum
import com.mathewsachin.fategrandautomata.scripts.enums.CardTypeEnum

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