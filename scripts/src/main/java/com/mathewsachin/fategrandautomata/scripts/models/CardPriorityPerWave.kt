package com.mathewsachin.fategrandautomata.scripts.models

import com.mathewsachin.fategrandautomata.scripts.enums.CardAffinityEnum
import com.mathewsachin.fategrandautomata.scripts.enums.CardTypeEnum

class CardPriorityPerWave private constructor(
    private val scoresPerWave: List<CardPriority>
) : List<CardPriority> by scoresPerWave {
    fun atWave(wave: Int) =
        scoresPerWave[wave.coerceIn(scoresPerWave.indices)]
            // Give minimum priority to unknown
            .plus(
                CardScore(
                    CardTypeEnum.Unknown,
                    CardAffinityEnum.Normal
                )
            )

    override fun toString() =
        scoresPerWave.joinToString(cardPriorityStageSeparator)

    companion object {
        private const val cardPriorityStageSeparator = "\n"

        fun from(scoresPerWave: List<CardPriority>) =
            CardPriorityPerWave(scoresPerWave)

        fun of(priority: String): CardPriorityPerWave =
            CardPriorityPerWave(
                priority
                    .split(cardPriorityStageSeparator)
                    .map { CardPriority.of(it) }
            )
    }
}