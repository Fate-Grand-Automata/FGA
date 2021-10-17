package com.mathewsachin.fategrandautomata.scripts

import com.mathewsachin.fategrandautomata.scripts.enums.CardAffinityEnum
import com.mathewsachin.fategrandautomata.scripts.enums.CardTypeEnum
import com.mathewsachin.fategrandautomata.scripts.models.CardPriorityPerWave
import com.mathewsachin.fategrandautomata.scripts.models.CommandCard
import com.mathewsachin.fategrandautomata.scripts.models.ParsedCard
import com.mathewsachin.fategrandautomata.scripts.models.TeamSlot
import com.mathewsachin.fategrandautomata.scripts.modules.FaceCardPriority
import org.junit.Test

class FaceCardPriorityTest {
    companion object {
        /**
         * [Kama, Scathach, Nero]
         * [Scathach WB] [Kama Q] [Nero RA] [Nero RA] [Scathach WQ]
         */
        val lineup1 = listOf(
            ParsedCard(
                card = CommandCard.Face.A,
                servant = TeamSlot.B,
                type = CardTypeEnum.Buster,
                affinity = CardAffinityEnum.Weak
            ),
            ParsedCard(
                card = CommandCard.Face.B,
                servant = TeamSlot.A,
                type = CardTypeEnum.Quick
            ),
            ParsedCard(
                card = CommandCard.Face.C,
                servant = TeamSlot.C,
                type = CardTypeEnum.Arts,
                affinity = CardAffinityEnum.Resist
            ),
            ParsedCard(
                card = CommandCard.Face.D,
                servant = TeamSlot.C,
                type = CardTypeEnum.Arts,
                affinity = CardAffinityEnum.Resist
            ),
            ParsedCard(
                card = CommandCard.Face.E,
                servant = TeamSlot.B,
                type = CardTypeEnum.Quick,
                affinity = CardAffinityEnum.Weak
            )
        )
    }

    @Test
    fun defaultPriority() {
        val priority = FaceCardPriority(CardPriorityPerWave.default, null)

        val sorted = priority.sort(lineup1, 0).map { it.card }
        val expected = listOf(
            CommandCard.Face.A, CommandCard.Face.E, CommandCard.Face.B, CommandCard.Face.C, CommandCard.Face.D
        )

        assert(sorted == expected)
    }
}