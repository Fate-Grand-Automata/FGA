package io.github.fate_grand_automata.scripts.models

import io.github.fate_grand_automata.scripts.enums.CardAffinityEnum
import io.github.fate_grand_automata.scripts.enums.CardTypeEnum

class CardPriorityPerWave private constructor(
    private val scoresPerWave: List<CardPriority>
) : List<CardPriority> by scoresPerWave {
    fun atWave(wave: Int) = scoresPerWave[wave.coerceIn(scoresPerWave.indices)]
        .plus(
            // Give minimum priority to unknown cards
            CardScore(
                CardTypeEnum.Unknown,
                CardAffinityEnum.Normal
            )
        )

    override fun toString() =
        scoresPerWave.joinToString(cardPriorityStageSeparator)

    companion object {
        private const val defaultCardPriority = "WBC, WAC, WQC, WB, WA, WQ, BC, AC, QC, B, A, Q, RB, RA, RQ"
        private const val cardPriorityStageSeparator = "\n"

        val default
            get() =
                of(defaultCardPriority)

        fun from(scoresPerWave: List<CardPriority>) =
            CardPriorityPerWave(scoresPerWave)

        fun of(priority: String): CardPriorityPerWave =
            if (priority.isBlank()) {
                default
            } else {
                CardPriorityPerWave(
                    priority
                        .split(cardPriorityStageSeparator)
                        .map { CardPriority.of(it) }
                )
            }
    }
}