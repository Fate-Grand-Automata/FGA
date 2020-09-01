package com.mathewsachin.fategrandautomata.scripts.models

import com.mathewsachin.fategrandautomata.scripts.enums.CardAffinityEnum
import com.mathewsachin.fategrandautomata.scripts.enums.CardTypeEnum
import com.mathewsachin.libautomata.ScriptExitException
import java.util.*

class CardPriority private constructor(scores: List<CardScore>) : List<CardScore> by scores {
    override fun toString() = joinToString()

    companion object {
        private const val dummyNormalAffinityChar = 'X'
        private const val cardPriorityErrorString = "Battle_CardPriority Error at '"

        fun from(scores: List<CardScore>) = CardPriority(scores)

        fun of(priority: String): CardPriority {
            val scores = priority
                .splitToSequence(',')
                .map { it.trim().toUpperCase(Locale.US) }
                .map {
                    when (it.length) {
                        1 -> "$dummyNormalAffinityChar$it"
                        2 -> it
                        else -> throw ScriptExitException("$cardPriorityErrorString${it}': Invalid card length.")
                    }
                }
                .map {
                    val cardType = when (it[1]) {
                        'B' -> CardTypeEnum.Buster
                        'A' -> CardTypeEnum.Arts
                        'Q' -> CardTypeEnum.Quick
                        else -> throw ScriptExitException("$cardPriorityErrorString${it[1]}': Only 'B', 'A' and 'Q' are valid card types.")
                    }

                    val cardAffinity = when (it[0]) {
                        'W' -> CardAffinityEnum.Weak
                        'R' -> CardAffinityEnum.Resist
                        dummyNormalAffinityChar -> CardAffinityEnum.Normal
                        else -> throw ScriptExitException("$cardPriorityErrorString${it[0]}': Only 'W', and 'R' are valid card affinities.")
                    }

                    CardScore(
                        cardType,
                        cardAffinity
                    )
                }
                .toList()

            if (scores.size != 9) {
                throw ScriptExitException("$cardPriorityErrorString': Expected 9 cards, but ${scores.size} found.")
            }

            return CardPriority(scores)
        }
    }
}