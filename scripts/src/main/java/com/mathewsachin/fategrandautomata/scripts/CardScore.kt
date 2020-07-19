package com.mathewsachin.fategrandautomata.scripts

import com.mathewsachin.fategrandautomata.scripts.enums.CardAffinityEnum
import com.mathewsachin.fategrandautomata.scripts.enums.CardTypeEnum

data class CardScore(val CardType: CardTypeEnum, val CardAffinity: CardAffinityEnum) {
    override fun toString(): String {
        var result = ""

        if (CardAffinity != CardAffinityEnum.Normal) {
            result += "$CardAffinity "
        }

        result += CardType

        return result
    }
}