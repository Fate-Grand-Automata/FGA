package io.github.fate_grand_automata.scripts.models

import io.github.fate_grand_automata.scripts.enums.CardAffinityEnum
import io.github.fate_grand_automata.scripts.enums.CardTypeEnum

class CardPriorityPerWave private constructor(
    private val scoresPerWave: List<CardPriority>,
) : List<CardPriority> by scoresPerWave {
    fun atWave(wave: Int) =
        scoresPerWave[wave.coerceIn(scoresPerWave.indices)]
            // Give minimum priority to unknown
            .plus(
                CardScore(
                    CardTypeEnum.Unknown,
                    CardAffinityEnum.Normal,
                ),
            )

    override fun toString() =
        scoresPerWave.joinToString(CARD_PRIORITY_STAGE_SEPARATOR)

    companion object {
        private const val DEFAULT_CARD_PRIORITY = "WB, WA, WQ, B, A, Q, RB, RA, RQ"
        private const val CARD_PRIORITY_STAGE_SEPARATOR = "\n"

        val default get() =
            of(DEFAULT_CARD_PRIORITY)

        fun from(scoresPerWave: List<CardPriority>) =
            CardPriorityPerWave(scoresPerWave)

        fun of(priority: String): CardPriorityPerWave =
            if (priority.isBlank()) {
                default
            } else {
                CardPriorityPerWave(
                    priority
                        .split(CARD_PRIORITY_STAGE_SEPARATOR)
                        .map { CardPriority.of(it) },
                )
            }
    }
}
