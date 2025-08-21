package io.github.fate_grand_automata.scripts.models

import io.github.fate_grand_automata.scripts.entrypoints.AutoBattle
import io.github.fate_grand_automata.scripts.enums.CardAffinityEnum
import io.github.fate_grand_automata.scripts.enums.CardTypeEnum

class CardPriority private constructor(scores: List<CardScore>) : List<CardScore> by scores {
    override fun toString() = joinToString()

    companion object {
        private const val DUMMY_NORMAL_AFFINITY_CHAR = 'X'
        private const val CARD_PRIORITY_ERROR_MSG = "Battle_CardPriority Error at '"

        fun from(scores: List<CardScore>) = CardPriority(scores)

        private fun raiseParseError(msg: String): Nothing {
            throw AutoBattle.BattleExitException(
                AutoBattle.ExitReason.CardPriorityParseError(msg),
            )
        }

        fun of(priority: String): CardPriority {
            val scores = priority
                .splitToSequence(',')
                .map { it.trim().uppercase() }
                .map {
                    when (it.length) {
                        1 -> "$DUMMY_NORMAL_AFFINITY_CHAR$it"
                        2 -> it
                        else -> raiseParseError("$CARD_PRIORITY_ERROR_MSG$it': Invalid card length.")
                    }
                }
                .map {
                    val cardType = when (it[1]) {
                        'B' -> CardTypeEnum.Buster
                        'A' -> CardTypeEnum.Arts
                        'Q' -> CardTypeEnum.Quick
                        else -> raiseParseError(
                            "$CARD_PRIORITY_ERROR_MSG${it[1]}': Only 'B', 'A' and 'Q' are valid card types.",
                        )
                    }

                    val cardAffinity = when (it[0]) {
                        'W' -> CardAffinityEnum.Weak
                        'R' -> CardAffinityEnum.Resist
                        DUMMY_NORMAL_AFFINITY_CHAR -> CardAffinityEnum.Normal
                        else -> raiseParseError(
                            "$CARD_PRIORITY_ERROR_MSG${it[0]}': Only 'W', and 'R' are valid card affinities.",
                        )
                    }

                    CardScore(
                        cardType,
                        cardAffinity,
                    )
                }
                .toList()

            if (scores.size != 9) {
                raiseParseError("$CARD_PRIORITY_ERROR_MSG': Expected 9 cards, but ${scores.size} found.")
            }

            return CardPriority(scores)
        }
    }
}
