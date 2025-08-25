package io.github.fate_grand_automata.scripts

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import io.github.fate_grand_automata.scripts.enums.BraveChainEnum
import io.github.fate_grand_automata.scripts.models.CommandCard
import io.github.fate_grand_automata.scripts.models.NPUsage
import io.github.fate_grand_automata.scripts.modules.ApplyBraveChains
import kotlin.test.Test

class BraveChainsTest {
    private fun shouldReturnSame(mode: BraveChainEnum) {
        val braveChains = ApplyBraveChains()

        val cards = FaceCardPriorityTest.lineup1
        val picked = braveChains.pick(
            cards = cards,
            braveChains = mode
        )

        assertThat(picked).isEqualTo(cards)
    }

    @Test
    fun ignoreBraveChains() {
        shouldReturnSame(BraveChainEnum.None)
    }

    private fun justRearrange(mode: BraveChainEnum) {
        val braveChains = ApplyBraveChains()

        val cards = FaceCardPriorityTest.lineup1
        val picked = braveChains.pick(
            cards = cards,
            braveChains = mode,
            rearrange = true
        ).map { it.card }

        assertThat(picked).containsExactly(CommandCard.Face.A, CommandCard.Face.C, CommandCard.Face.B, CommandCard.Face.D, CommandCard.Face.E)
    }

    @Test
    fun ignoreBraveChainsRearrange() {
        justRearrange(BraveChainEnum.None)
    }

    @Test
    fun braveChainsButNoNP() {
        shouldReturnSame(BraveChainEnum.WithNP)
    }

    @Test
    fun braveChainsRearrangeButNoNP() {
        justRearrange(BraveChainEnum.WithNP)
    }

    @Test
    fun braveChainsWith1MatchingCard() {
        val braveChains = ApplyBraveChains()

        val cards = FaceCardPriorityTest.lineup1
        val picked = braveChains.pick(
            cards = cards,
            braveChains = BraveChainEnum.WithNP,
            npUsage = NPUsage(setOf(CommandCard.NP.A), 0)
        ).map { it.card }

        assertThat(picked).containsExactly(CommandCard.Face.B, CommandCard.Face.A, CommandCard.Face.C, CommandCard.Face.D, CommandCard.Face.E)
    }

    @Test
    fun braveChainsRearrangeWith1MatchingCard() {
        val braveChains = ApplyBraveChains()

        val cards = FaceCardPriorityTest.lineup1
        val picked = braveChains.pick(
            cards = cards,
            braveChains = BraveChainEnum.WithNP,
            rearrange = true,
            npUsage = NPUsage(setOf(CommandCard.NP.A), 0)
        ).map { it.card }

        assertThat(picked).containsExactly(CommandCard.Face.A, CommandCard.Face.B, CommandCard.Face.C, CommandCard.Face.D, CommandCard.Face.E)
    }

    @Test
    fun braveChainsWith2MatchingCards() {
        val braveChains = ApplyBraveChains()

        val cards = FaceCardPriorityTest.lineup1
        val picked = braveChains.pick(
            cards = cards,
            braveChains = BraveChainEnum.WithNP,
            npUsage = NPUsage(setOf(CommandCard.NP.B), 0)
        ).map { it.card }

        assertThat(picked).containsExactly(CommandCard.Face.A, CommandCard.Face.E, CommandCard.Face.B, CommandCard.Face.C, CommandCard.Face.D)
    }

    @Test
    fun braveChainsRearrangeWith2MatchingCards() {
        val braveChains = ApplyBraveChains()

        val cards = FaceCardPriorityTest.lineup1
        val picked = braveChains.pick(
            cards = cards,
            braveChains = BraveChainEnum.WithNP,
            rearrange = true,
            npUsage = NPUsage(setOf(CommandCard.NP.B), 0)
        ).map { it.card }

        assertThat(picked).containsExactly(CommandCard.Face.E, CommandCard.Face.A, CommandCard.Face.B, CommandCard.Face.C, CommandCard.Face.D)
    }

    @Test
    fun avoidBraveChains3DiffCards() {
        val braveChains = ApplyBraveChains()

        val cards = FaceCardPriorityTest.lineup1
        val picked = braveChains.pick(
            cards = cards,
            braveChains = BraveChainEnum.Avoid
        ).map { it.card }

        assertThat(picked).containsExactly(CommandCard.Face.A, CommandCard.Face.B, CommandCard.Face.C, CommandCard.Face.E, CommandCard.Face.D)
    }

    @Test
    fun avoidBraveChainsRearrange3DiffCards() {
        justRearrange(BraveChainEnum.Avoid)
    }

    @Test
    fun avoidBraveChainsWith2DiffCards() {
        val braveChains = ApplyBraveChains()

        val cards = FaceCardPriorityTest.lineup2
        val picked = braveChains.pick(
            cards = cards,
            braveChains = BraveChainEnum.Avoid
        ).map { it.card }

        assertThat(picked).containsExactly(CommandCard.Face.A, CommandCard.Face.B, CommandCard.Face.E, CommandCard.Face.C, CommandCard.Face.D)
    }

    @Test
    fun avoidBraveChainsRearrangeWith2DiffCards() {
        val braveChains = ApplyBraveChains()

        val cards = FaceCardPriorityTest.lineup2
        val picked = braveChains.pick(
            cards = cards,
            braveChains = BraveChainEnum.Avoid,
            rearrange = true
        ).map { it.card }

        assertThat(picked).containsExactly(CommandCard.Face.A, CommandCard.Face.B, CommandCard.Face.E, CommandCard.Face.C, CommandCard.Face.D)
    }

    @Test
    fun mightyChain_lineup1() {
        val braveChains = ApplyBraveChains()

        val cards = FaceCardPriorityTest.lineup1
        val picked = braveChains.pick(
            cards = cards,
            braveChains = BraveChainEnum.WithNPMighty
        ).map { it.card }

        assertThat(picked).containsExactly(CommandCard.Face.A, CommandCard.Face.B, CommandCard.Face.C, CommandCard.Face.D, CommandCard.Face.E)
    }

    @Test
    fun mightyChain_lineup2() {
        val braveChains = ApplyBraveChains()

        val cards = FaceCardPriorityTest.lineup2
        val picked = braveChains.pick(
            cards = cards,
            braveChains = BraveChainEnum.WithNPMighty
        ).map { it.card }

        assertThat(picked).containsExactly(CommandCard.Face.A, CommandCard.Face.E, CommandCard.Face.C, CommandCard.Face.B, CommandCard.Face.D)
    }

    @Test
    fun mightyChain_lineup1_withNp_fieldSlotA() {
        val braveChains = ApplyBraveChains()

        val cards = FaceCardPriorityTest.lineup1
        val picked = braveChains.pick(
            cards = cards,
            braveChains = BraveChainEnum.WithNPMighty,
            npUsage = NPUsage(setOf(CommandCard.NP.A), 0)
        ).map { it.card }

        // Unable to Brave Chain. Will ignore and return result of withNp
        assertThat(picked).containsExactly(CommandCard.Face.B, CommandCard.Face.A, CommandCard.Face.C, CommandCard.Face.D, CommandCard.Face.E)
    }

    @Test
    fun mightyChain_lineup1_withNp_fieldSlotB() {
        val braveChains = ApplyBraveChains()

        val cards = FaceCardPriorityTest.lineup1
        val picked = braveChains.pick(
            cards = cards,
            braveChains = BraveChainEnum.WithNPMighty,
            npUsage = NPUsage(setOf(CommandCard.NP.B), 0)
        ).map { it.card }

        // Attempt to Brave chain with Scathach cards
        assertThat(picked).containsExactly(CommandCard.Face.A, CommandCard.Face.E, CommandCard.Face.B, CommandCard.Face.C, CommandCard.Face.D)
    }

    @Test
    fun mightyChain_lineup1_withNp_fieldSlotC() {
        val braveChains = ApplyBraveChains()

        val cards = FaceCardPriorityTest.lineup1
        val picked = braveChains.pick(
            cards = cards,
            braveChains = BraveChainEnum.WithNPMighty,
            npUsage = NPUsage(setOf(CommandCard.NP.C), 0)
        ).map { it.card }

        // Unable to Mighty Chain. Will ignore and return result of withNp
        assertThat(picked).containsExactly(CommandCard.Face.C, CommandCard.Face.D, CommandCard.Face.A, CommandCard.Face.B, CommandCard.Face.E)
    }

    @Test
    fun mightyChain_fullCards_lineup1() {
        val braveChains = ApplyBraveChains()

        val cards = MightyChainTest.lineup1
        val picked = braveChains.pick(
            cards = cards,
            braveChains = BraveChainEnum.WithNPMighty
        ).map { it.card }

        assertThat(picked).containsExactly(CommandCard.Face.A, CommandCard.Face.B, CommandCard.Face.C, CommandCard.Face.D, CommandCard.Face.E)
    }

    @Test
    fun mightyChain_fullCards_lineup2() {
        val braveChains = ApplyBraveChains()

        val cards = MightyChainTest.lineup2
        val picked = braveChains.pick(
            cards = cards,
            braveChains = BraveChainEnum.WithNPMighty
        ).map { it.card }

        assertThat(picked).containsExactly(CommandCard.Face.A, CommandCard.Face.E, CommandCard.Face.C, CommandCard.Face.D, CommandCard.Face.B)
    }

    @Test
    fun mightyChain_fullCards_lineup3() {
        val braveChains = ApplyBraveChains()

        val cards = MightyChainTest.lineup3
        val picked = braveChains.pick(
            cards = cards,
            braveChains = BraveChainEnum.WithNPMighty
        ).map { it.card }

        assertThat(picked).containsExactly(CommandCard.Face.E, CommandCard.Face.C, CommandCard.Face.A, CommandCard.Face.B, CommandCard.Face.D)
    }
}