package com.mathewsachin.fategrandautomata.scripts

import com.mathewsachin.fategrandautomata.scripts.enums.BraveChainEnum
import com.mathewsachin.fategrandautomata.scripts.models.CommandCard
import com.mathewsachin.fategrandautomata.scripts.models.FieldSlot
import com.mathewsachin.fategrandautomata.scripts.models.NPUsage
import com.mathewsachin.fategrandautomata.scripts.models.TeamSlot
import com.mathewsachin.fategrandautomata.scripts.modules.ApplyBraveChains
import com.mathewsachin.fategrandautomata.scripts.modules.ServantTracker
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert
import org.junit.Test

class BraveChainsTest {
    private fun init(
        deployed: Map<FieldSlot, TeamSlot> = mapOf(
            FieldSlot.A to TeamSlot.A,
            FieldSlot.B to TeamSlot.B,
            FieldSlot.C to TeamSlot.C
        )
    ): ApplyBraveChains {
        val servantTracker: ServantTracker = mockk()
        every { servantTracker.deployed } returns deployed

        return ApplyBraveChains(
            servantTracker = servantTracker
        )
    }

    private fun shouldReturnSame(mode: BraveChainEnum) {
        val braveChains = init()

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
        val braveChains = init()

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
        val braveChains = init()

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
        val braveChains = init()

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
        val braveChains = init()

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
        val braveChains = init()

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
        val braveChains = init()

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
        val braveChains = init()

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
        val braveChains = init()

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