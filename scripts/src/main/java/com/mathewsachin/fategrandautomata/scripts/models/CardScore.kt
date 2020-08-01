package com.mathewsachin.fategrandautomata.scripts.models

import com.mathewsachin.fategrandautomata.scripts.enums.CardAffinityEnum
import com.mathewsachin.fategrandautomata.scripts.enums.CardTypeEnum

data class CardScore(val CardType: CardTypeEnum, val CardAffinity: CardAffinityEnum) {
    private fun String.filterCapitals(): String {
        return this
            .asSequence()
            .filter { it.isUpperCase() }
            .joinToString(separator = "")
    }

    override fun toString(): String {
        var result = ""

        if (CardAffinity != CardAffinityEnum.Normal) {
            result += "$CardAffinity "
        }

        result += CardType

        return result.filterCapitals()
    }
}