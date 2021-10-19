package com.mathewsachin.fategrandautomata.scripts

import com.mathewsachin.fategrandautomata.scripts.enums.CardAffinityEnum
import com.mathewsachin.fategrandautomata.scripts.enums.CardTypeEnum
import com.mathewsachin.fategrandautomata.scripts.models.*
import com.mathewsachin.fategrandautomata.scripts.modules.FaceCardPriority
import org.junit.Assert
import org.junit.Test

class FaceCardPriorityTest {
    companion object {
        val scathach1WB = ParsedCard(
            card = CommandCard.Face.A,
            servant = TeamSlot.B,
            fieldSlot = FieldSlot.B,
            type = CardTypeEnum.Buster,
            affinity = CardAffinityEnum.Weak
        )
        val kama2Q = ParsedCard(
            card = CommandCard.Face.B,
            servant = TeamSlot.A,
            fieldSlot = FieldSlot.A,
            type = CardTypeEnum.Quick
        )
        val nero3RA = ParsedCard(
            card = CommandCard.Face.C,
            servant = TeamSlot.C,
            fieldSlot = FieldSlot.C,
            type = CardTypeEnum.Arts,
            affinity = CardAffinityEnum.Resist
        )
        val nero4RA = ParsedCard(
            card = CommandCard.Face.D,
            servant = TeamSlot.C,
            fieldSlot = FieldSlot.C,
            type = CardTypeEnum.Arts,
            affinity = CardAffinityEnum.Resist
        )
        val scathach5WQ = ParsedCard(
            card = CommandCard.Face.E,
            servant = TeamSlot.B,
            fieldSlot = FieldSlot.B,
            type = CardTypeEnum.Quick,
            affinity = CardAffinityEnum.Weak
        )

        /**
         * [Kama, Scathach, Nero]
         * [Scathach WB] [Kama Q] [Nero RA] [Nero RA] [Scathach WQ]
         */
        val lineup1 = listOf(scathach1WB, kama2Q, nero3RA, nero4RA, scathach5WQ)

        val lineup2 = listOf(scathach1WB, scathach5WQ, kama2Q, nero3RA, nero4RA)
    }

    @Test
    fun defaultPriority() {
        val priority = FaceCardPriority(CardPriorityPerWave.default, null)

        val sorted = priority.sort(lineup1, 0).map { it.card }
        val expected = listOf(
            CommandCard.Face.A, CommandCard.Face.E, CommandCard.Face.B, CommandCard.Face.C, CommandCard.Face.D
        )

        Assert.assertEquals(expected, sorted)
    }

    @Test
    fun defaultServantPriority() {
        val priority = FaceCardPriority(CardPriorityPerWave.default, ServantPriorityPerWave.default)

        val sorted = priority.sort(lineup1, 0).map { it.card }
        val expected = listOf(
            CommandCard.Face.B, CommandCard.Face.A, CommandCard.Face.E, CommandCard.Face.C, CommandCard.Face.D
        )

        Assert.assertEquals(expected, sorted)
    }
}