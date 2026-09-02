package io.github.fate_grand_automata.scripts

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import io.github.fate_grand_automata.scripts.enums.CardAffinityEnum
import io.github.fate_grand_automata.scripts.enums.CardTypeEnum
import io.github.fate_grand_automata.scripts.models.CardPriorityPerWave
import io.github.fate_grand_automata.scripts.models.CardTypePattern
import io.github.fate_grand_automata.scripts.models.CommandCard
import io.github.fate_grand_automata.scripts.models.FieldSlot
import io.github.fate_grand_automata.scripts.models.NPUsage
import io.github.fate_grand_automata.scripts.models.ParsedCard
import io.github.fate_grand_automata.scripts.models.TeamSlot
import io.github.fate_grand_automata.scripts.modules.CardTypePatternSelector
import kotlin.test.Test

class CardTypePatternSelectorTest {
    private val selector = CardTypePatternSelector()

    @Test
    fun matchesEachPositionUsingCardPriorityAsTiebreak() {
        // lineup1 = [Scathach WB, Kama Q, Nero RA, Nero RA, Scathach WQ] (FaceCardPriorityTest.kt)
        val result = selector.select(
            cards = FaceCardPriorityTest.lineup1,
            patterns = listOf(CardTypePattern.of("BQA")),
            cardPriority = CardPriorityPerWave.default,
            stage = 0
        )

        // B: only Scathach WB. Q: Scathach WQ beats Kama's plain Q under the default
        // priority ("WB,WA,WQ,B,A,Q,RB,RA,RQ"). A: both Nero cards tie (RA=RA), so the
        // one dealt first (Face.C) wins. The two cards never picked trail unordered.
        assertThat(result?.map { it.card }).isEqualTo(
            listOf(CommandCard.Face.A, CommandCard.Face.E, CommandCard.Face.C, CommandCard.Face.B, CommandCard.Face.D)
        )
    }

    @Test
    fun fallsBackToTheNextPatternWhenTheFirstCannotBeSatisfied() {
        // lineup1 only has 1 Buster card, so a pattern needing 3 is unsatisfiable.
        val result = selector.select(
            cards = FaceCardPriorityTest.lineup1,
            patterns = listOf(CardTypePattern.of("BBB"), CardTypePattern.of("BQA")),
            cardPriority = CardPriorityPerWave.default,
            stage = 0
        )

        assertThat(result?.take(3)?.map { it.card }).isEqualTo(
            listOf(CommandCard.Face.A, CommandCard.Face.E, CommandCard.Face.C)
        )
    }

    @Test
    fun returnsNullWhenNoPatternCanBeSatisfied() {
        val result = selector.select(
            cards = FaceCardPriorityTest.lineup1,
            patterns = listOf(CardTypePattern.of("BBB"), CardTypePattern.of("BBQ")),
            cardPriority = CardPriorityPerWave.default,
            stage = 0
        )

        assertThat(result).isNull()
    }

    @Test
    fun returnsNullImmediatelyWhenNoPatternsAreConfigured() {
        val result = selector.select(
            cards = FaceCardPriorityTest.lineup1,
            patterns = emptyList(),
            cardPriority = CardPriorityPerWave.default,
            stage = 0
        )

        assertThat(result).isNull()
    }

    @Test
    fun doesNotReuseTheSamePhysicalCardForARepeatedTypeInThePattern() {
        val firstBuster = ParsedCard(CommandCard.Face.A, TeamSlot.A, FieldSlot.A, CardTypeEnum.Buster)
        val arts = ParsedCard(CommandCard.Face.B, TeamSlot.A, FieldSlot.A, CardTypeEnum.Arts)
        val secondBuster = ParsedCard(CommandCard.Face.C, TeamSlot.A, FieldSlot.A, CardTypeEnum.Buster)

        val result = selector.select(
            cards = listOf(firstBuster, arts, secondBuster),
            patterns = listOf(CardTypePattern.of("BAB")),
            cardPriority = CardPriorityPerWave.default,
            stage = 0
        )

        assertThat(result?.map { it.card }).isEqualTo(
            listOf(CommandCard.Face.A, CommandCard.Face.B, CommandCard.Face.C)
        )
    }

    @Test
    fun excludesAStunnedCardFromMatchingAnyRequiredType() {
        // CardTypePatternSelector never reads ParsedCard.isStunned. This works because
        // CardParser.kt:74-76 always reports a stunned card's type as CardTypeEnum.Unknown,
        // and CardTypePattern.of() can never require Unknown (only B/A/Q), so a stunned
        // card structurally cannot satisfy any requiredType. isStunned = true is set below
        // only to mirror how CardParser actually produces this ParsedCard.
        val stunned = ParsedCard(
            card = CommandCard.Face.A,
            servant = TeamSlot.A,
            fieldSlot = FieldSlot.A,
            type = CardTypeEnum.Unknown,
            isStunned = true
        )
        val onlyArts = ParsedCard(CommandCard.Face.B, TeamSlot.A, FieldSlot.A, CardTypeEnum.Arts)

        val result = selector.select(
            cards = listOf(stunned, onlyArts),
            patterns = listOf(CardTypePattern.of("BAB")),
            cardPriority = CardPriorityPerWave.default,
            stage = 0
        )

        assertThat(result).isNull()
    }

    @Test
    fun requiresFewerCardsAsMoreNpsAreUsedThisTurn() {
        val pattern = CardTypePattern.of("BQA")

        val noNp = selector.select(FaceCardPriorityTest.lineup1, listOf(pattern), CardPriorityPerWave.default, 0)
        val oneNp = selector.select(
            FaceCardPriorityTest.lineup1, listOf(pattern), CardPriorityPerWave.default, 0,
            NPUsage(setOf(CommandCard.NP.A), 0)
        )
        val twoNps = selector.select(
            FaceCardPriorityTest.lineup1, listOf(pattern), CardPriorityPerWave.default, 0,
            NPUsage(setOf(CommandCard.NP.A, CommandCard.NP.B), 0)
        )
        val threeNps = selector.select(
            FaceCardPriorityTest.lineup1, listOf(pattern), CardPriorityPerWave.default, 0,
            NPUsage(setOf(CommandCard.NP.A, CommandCard.NP.B, CommandCard.NP.C), 0)
        )

        // 0 NPs: all 3 positions (B, Q, A) need a matching card.
        assertThat(noNp?.take(3)?.map { it.card }).isEqualTo(
            listOf(CommandCard.Face.A, CommandCard.Face.E, CommandCard.Face.C)
        )
        // 1 NP: only the pattern's leading 2 positions (B, Q) need a matching card.
        assertThat(oneNp?.take(2)?.map { it.card }).isEqualTo(listOf(CommandCard.Face.A, CommandCard.Face.E))
        // 2 NPs: only the pattern's leading 1 position (B) needs a matching card.
        assertThat(twoNps?.take(1)?.map { it.card }).isEqualTo(listOf(CommandCard.Face.A))
        // 3 NPs: no face card is required at all, so every hand trivially satisfies it
        // and the hand comes back exactly as dealt.
        assertThat(threeNps).isEqualTo(FaceCardPriorityTest.lineup1)
    }

    @Test
    fun ranksACardWhoseScoreIsAbsentFromCardPriorityLastNotFirst() {
        val weakBuster = ParsedCard(
            card = CommandCard.Face.A,
            servant = TeamSlot.A,
            fieldSlot = FieldSlot.A,
            type = CardTypeEnum.Buster,
            affinity = CardAffinityEnum.Weak
        )
        val normalBuster = ParsedCard(CommandCard.Face.B, TeamSlot.A, FieldSlot.A, CardTypeEnum.Buster)

        // "B" repeated 9 times only ever names CardScore(Buster, Normal); CardScore(Buster,
        // Weak) never appears in this priority list even though it has the required length.
        val incompletePriority = CardPriorityPerWave.of("B,B,B,B,B,B,B,B,B")

        val result = selector.select(
            cards = listOf(weakBuster, normalBuster),
            patterns = listOf(CardTypePattern.of("BBB")),
            cardPriority = incompletePriority,
            stage = 0,
            npUsage = NPUsage(setOf(CommandCard.NP.A, CommandCard.NP.B), 0)
        )

        // Only 1 required type (pattern.take(3 - 2 nps) = 1 Buster). normalBuster's score is
        // listed (rank 0); weakBuster's score is absent and must lose the tiebreak, not win it.
        assertThat(result?.take(1)?.map { it.card }).isEqualTo(listOf(CommandCard.Face.B))
    }
}
