package com.mathewsachin.fategrandautomata.scripts

import com.mathewsachin.fategrandautomata.R
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

    fun getColorRes(): Int {
        return when (CardType) {
            CardTypeEnum.Buster -> when(CardAffinity){
                CardAffinityEnum.Weak -> R.color.colorBusterWeak
                CardAffinityEnum.Normal -> R.color.colorBuster
                CardAffinityEnum.Resist -> R.color.colorBusterResist
            }
            CardTypeEnum.Arts -> when(CardAffinity){
                CardAffinityEnum.Weak -> R.color.colorArtsWeak
                CardAffinityEnum.Normal -> R.color.colorArts
                CardAffinityEnum.Resist -> R.color.colorArtsResist
            }
            CardTypeEnum.Quick -> when(CardAffinity){
                CardAffinityEnum.Weak -> R.color.colorQuickWeak
                CardAffinityEnum.Normal -> R.color.colorQuick
                CardAffinityEnum.Resist -> R.color.colorQuickResist
            }
        }
    }
}