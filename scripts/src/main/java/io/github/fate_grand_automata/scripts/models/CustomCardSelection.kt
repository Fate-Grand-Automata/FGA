package io.github.fate_grand_automata.scripts.models

import io.github.fate_grand_automata.scripts.entrypoints.AutoBattle
import io.github.fate_grand_automata.scripts.enums.CardTypeEnum

class CustomCardSelection(
    private val cards: List<CustomCard>
) : List<CustomCard> by cards {
    override fun toString() = joinToString(separator = "")


    companion object {
        val empty = CustomCardSelection(emptyList())

        private const val customCardSelectionError = "Custom Card Selection Error at '"

        private fun raiseParseError(msg: String): Nothing {
            throw AutoBattle.BattleExitException(
                AutoBattle.ExitReason.CustomCardSelectionParseError(msg)
            )
        }

        fun of(selection: String): CustomCardSelection {
            val cards = selection
                .filter { !it.isWhitespace() }
                .uppercase()
                .chunked(2)
                .map {
                    when (it.length) {
                        2 -> it
                        else -> raiseParseError("$customCardSelectionError${it}': Invalid card.")
                    }
                }
                .map {
                    val type = when (it[0]) {
                        'B' -> CardTypeEnum.Buster
                        'A' -> CardTypeEnum.Arts
                        'Q' -> CardTypeEnum.Quick
                        else -> raiseParseError("$customCardSelectionError${it}': Invalid card type.")
                    }
                    val fieldSlot = when (it[1]) {
                        '1' -> FieldSlot.A
                        '2' -> FieldSlot.B
                        '3' -> FieldSlot.C
                        else -> raiseParseError("$customCardSelectionError${it}': Invalid servant field slot.")
                    }

                    CustomCard(fieldSlot, type)

                }

            if (cards.size > 3) {
                raiseParseError("$customCardSelectionError': Expected at most 3 cards selected per turn, but ${cards.size} found.")
            }
            return CustomCardSelection(cards)
        }
    }
}
