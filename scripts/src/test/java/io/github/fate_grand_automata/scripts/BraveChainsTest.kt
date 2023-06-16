package io.github.fate_grand_automata.scripts

import io.github.fate_grand_automata.scripts.enums.BraveChainEnum
import io.github.fate_grand_automata.scripts.models.CommandCard
import io.github.fate_grand_automata.scripts.models.NPUsage
import io.github.fate_grand_automata.scripts.modules.ApplyBraveChains
import org.junit.Assert
import org.junit.Test

class BraveChainsTest {
    private fun shouldReturnSame(mode: BraveChainEnum) {
        val braveChains = ApplyBraveChains()

        val cards = FaceCardPriorityTest.lineup1
        val picked = braveChains.pick(
            cards = cards,
            braveChains = mode
        )

        Assert.assertEquals(cards, picked)
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

        val expected = listOf(
            CommandCard.Face.A, CommandCard.Face.C, CommandCard.Face.B, CommandCard.Face.D, CommandCard.Face.E
        )

        Assert.assertEquals(expected, picked)
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

        val expected = listOf(
            CommandCard.Face.B, CommandCard.Face.A, CommandCard.Face.C, CommandCard.Face.D, CommandCard.Face.E
        )

        Assert.assertEquals(expected, picked)
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

        val expected = listOf(
            CommandCard.Face.A, CommandCard.Face.B, CommandCard.Face.C, CommandCard.Face.D, CommandCard.Face.E
        )

        Assert.assertEquals(expected, picked)
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

        val expected = listOf(
            CommandCard.Face.A, CommandCard.Face.E, CommandCard.Face.B, CommandCard.Face.C, CommandCard.Face.D
        )

        Assert.assertEquals(expected, picked)
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

        val expected = listOf(
            CommandCard.Face.E, CommandCard.Face.A, CommandCard.Face.B, CommandCard.Face.C, CommandCard.Face.D
        )

        Assert.assertEquals(expected, picked)
    }

    @Test
    fun avoidBraveChains3DiffCards() {
        val braveChains = ApplyBraveChains()

        val cards = FaceCardPriorityTest.lineup1
        val picked = braveChains.pick(
            cards = cards,
            braveChains = BraveChainEnum.Avoid
        ).map { it.card }

        val expected = listOf(
            CommandCard.Face.A, CommandCard.Face.B, CommandCard.Face.C, CommandCard.Face.E, CommandCard.Face.D
        )

        Assert.assertEquals(expected, picked)
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

        val expected = listOf(
            CommandCard.Face.A, CommandCard.Face.B, CommandCard.Face.E, CommandCard.Face.C, CommandCard.Face.D
        )

        Assert.assertEquals(expected, picked)
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

        val expected = listOf(
            CommandCard.Face.A, CommandCard.Face.B, CommandCard.Face.E, CommandCard.Face.C, CommandCard.Face.D
        )

        Assert.assertEquals(expected, picked)
    }
}