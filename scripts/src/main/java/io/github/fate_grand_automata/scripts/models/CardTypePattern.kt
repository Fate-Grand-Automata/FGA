package io.github.fate_grand_automata.scripts.models

import io.github.fate_grand_automata.scripts.entrypoints.AutoBattle
import io.github.fate_grand_automata.scripts.enums.CardTypeEnum

/**
 * An ordered request for the type (Buster/Arts/Quick) of each of the 3 cards a turn
 * ultimately taps, independent of [CardPriority]'s whole-hand ranking. See CONTEXT.md
 * ("型パターン / Type Pattern") for the concept this models.
 */
class CardTypePattern private constructor(
    private val types: List<CardTypeEnum>
) : List<CardTypeEnum> by types {
    override fun toString() = types.joinToString("") {
        when (it) {
            CardTypeEnum.Buster -> "B"
            CardTypeEnum.Arts -> "A"
            CardTypeEnum.Quick -> "Q"
            // of() never produces Unknown; this branch only exists to keep the `when` exhaustive.
            CardTypeEnum.Unknown -> "?"
        }
    }

    companion object {
        private const val requiredLength = 3
        private const val errorPrefix = "Battle_CardTypePattern Error at '"

        fun from(types: List<CardTypeEnum>) = CardTypePattern(types)

        private fun raiseParseError(msg: String): Nothing {
            throw AutoBattle.BattleExitException(
                AutoBattle.ExitReason.CardTypePatternParseError(msg)
            )
        }

        fun of(pattern: String): CardTypePattern {
            val normalized = pattern.trim().uppercase()

            if (normalized.length != requiredLength) {
                raiseParseError("$errorPrefix$pattern': Expected exactly $requiredLength characters, but ${normalized.length} found.")
            }

            val types = normalized.map {
                when (it) {
                    'B' -> CardTypeEnum.Buster
                    'A' -> CardTypeEnum.Arts
                    'Q' -> CardTypeEnum.Quick
                    else -> raiseParseError("$errorPrefix$pattern': Only 'B', 'A' and 'Q' are valid card types.")
                }
            }

            return CardTypePattern(types)
        }
    }
}
