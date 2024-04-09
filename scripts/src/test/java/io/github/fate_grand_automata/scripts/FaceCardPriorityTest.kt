package io.github.fate_grand_automata.scripts

import assertk.assertThat
import assertk.assertions.containsExactly
import io.github.fate_grand_automata.scripts.enums.CardAffinityEnum
import io.github.fate_grand_automata.scripts.enums.CardTypeEnum
import io.github.fate_grand_automata.scripts.models.CardPriorityPerWave
import io.github.fate_grand_automata.scripts.models.CommandCard
import io.github.fate_grand_automata.scripts.models.FieldSlot
import io.github.fate_grand_automata.scripts.models.ParsedCard
import io.github.fate_grand_automata.scripts.models.ServantPriorityPerWave
import io.github.fate_grand_automata.scripts.models.TeamSlot
import io.github.fate_grand_automata.scripts.modules.FaceCardPriority
import kotlin.test.Test

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

        val superOrion1B = ParsedCard(
            card = CommandCard.Face.A,
            servant = TeamSlot.A,
            fieldSlot = FieldSlot.A,
            type = CardTypeEnum.Buster,
            affinity = CardAffinityEnum.Weak,
            criticalPercentage = 10
        )

        val superOrion2B = ParsedCard(
            card = CommandCard.Face.B,
            servant = TeamSlot.A,
            fieldSlot = FieldSlot.A,
            type = CardTypeEnum.Buster,
            affinity = CardAffinityEnum.Weak,
            criticalPercentage = 2
        )

        val superOrion3B = ParsedCard(
            card = CommandCard.Face.C,
            servant = TeamSlot.A,
            fieldSlot = FieldSlot.A,
            type = CardTypeEnum.Buster,
            affinity = CardAffinityEnum.Weak,
            criticalPercentage = 8
        )

        val superOrion1A = ParsedCard(
            card = CommandCard.Face.D,
            servant = TeamSlot.A,
            fieldSlot = FieldSlot.A,
            type = CardTypeEnum.Arts,
            affinity = CardAffinityEnum.Weak
        )

        val superOrion1Q = ParsedCard(
            card = CommandCard.Face.E,
            servant = TeamSlot.A,
            fieldSlot = FieldSlot.A,
            type = CardTypeEnum.Quick,
            affinity = CardAffinityEnum.Weak
        )

        val lineup3 = listOf(superOrion1B, superOrion2B, superOrion3B, superOrion1A, superOrion1Q)
    }

    @Test
    fun defaultPriority() {
        val priority = FaceCardPriority(CardPriorityPerWave.default, null)

        val sorted = priority.sort(lineup1, 0).map { it.card }

        assertThat(sorted).containsExactly(
            CommandCard.Face.A,
            CommandCard.Face.E,
            CommandCard.Face.B,
            CommandCard.Face.C,
            CommandCard.Face.D
        )
    }

    @Test
    fun defaultServantPriority() {
        val priority = FaceCardPriority(CardPriorityPerWave.default, ServantPriorityPerWave.default)

        val sorted = priority.sort(lineup1, 0).map { it.card }

        assertThat(sorted).containsExactly(
            CommandCard.Face.B,
            CommandCard.Face.A,
            CommandCard.Face.E,
            CommandCard.Face.C,
            CommandCard.Face.D
        )
    }

    @Test
    fun defaultCardPriorityWithCritStars() {
        val priority = FaceCardPriority(CardPriorityPerWave.default, null)

        val sorted = priority.sort(lineup3, 0).map { it.card }

        assertThat(sorted).containsExactly(
            CommandCard.Face.A,
            CommandCard.Face.C,
            CommandCard.Face.B,
            CommandCard.Face.D,
            CommandCard.Face.E
        )
    }

    @Test
    fun customCardPriorityWithDefaultCritStars() {
        val priority = FaceCardPriority(
            CardPriorityPerWave.of("WA, WB, WQ, A, B, Q, RA, RB, RQ"),
            ServantPriorityPerWave.default
        )

        val sorted = priority.sort(lineup3, 0).map { it.card }

        assertThat(sorted).containsExactly(
            CommandCard.Face.D,
            CommandCard.Face.A,
            CommandCard.Face.C,
            CommandCard.Face.B,
            CommandCard.Face.E
        )
    }

    @Test
    fun customCardPriorityWithCritStars() {
        val superOrion1B = ParsedCard(
            card = CommandCard.Face.A,
            servant = TeamSlot.A,
            fieldSlot = FieldSlot.A,
            type = CardTypeEnum.Buster,
            affinity = CardAffinityEnum.WeakCritical,
            criticalPercentage = 10
        )

        val superOrion2B = ParsedCard(
            card = CommandCard.Face.B,
            servant = TeamSlot.A,
            fieldSlot = FieldSlot.A,
            type = CardTypeEnum.Buster,
            affinity = CardAffinityEnum.Weak,
            criticalPercentage = 2
        )

        val superOrion3B = ParsedCard(
            card = CommandCard.Face.C,
            servant = TeamSlot.A,
            fieldSlot = FieldSlot.A,
            type = CardTypeEnum.Buster,
            affinity = CardAffinityEnum.WeakCritical,
            criticalPercentage = 8
        )

        val superOrion1A = ParsedCard(
            card = CommandCard.Face.D,
            servant = TeamSlot.A,
            fieldSlot = FieldSlot.A,
            type = CardTypeEnum.Arts,
            affinity = CardAffinityEnum.Weak
        )

        val superOrion1Q = ParsedCard(
            card = CommandCard.Face.E,
            servant = TeamSlot.A,
            fieldSlot = FieldSlot.A,
            type = CardTypeEnum.Quick,
            affinity = CardAffinityEnum.Weak
        )
        val lineup4 = listOf(superOrion1B, superOrion2B, superOrion3B, superOrion1A, superOrion1Q)
        val priority = FaceCardPriority(
            CardPriorityPerWave.of("WBC, WAC, WQC, WA, WB, WQ,AC,BC, QC, A, B, Q, RA, RB, RQ"),
            ServantPriorityPerWave.default
        )

        val sorted = priority.sort(lineup4, 0).map { it.card }

        assertThat(sorted).containsExactly(
            CommandCard.Face.A,
            CommandCard.Face.C,
            CommandCard.Face.D,
            CommandCard.Face.B,
            CommandCard.Face.E
        )
    }
}