package com.mathewsachin.fategrandautomata.scripts

import com.mathewsachin.fategrandautomata.scripts.enums.BraveChainEnum
import com.mathewsachin.fategrandautomata.scripts.models.AutoSkillAction
import com.mathewsachin.fategrandautomata.scripts.models.CommandCard
import com.mathewsachin.fategrandautomata.scripts.models.FieldSlot
import com.mathewsachin.fategrandautomata.scripts.models.TeamSlot
import com.mathewsachin.fategrandautomata.scripts.modules.ApplyBraveChains
import com.mathewsachin.fategrandautomata.scripts.modules.ServantTracker
import io.mockk.every
import io.mockk.mockk
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

        assert(picked == cards)
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

        assert(picked == expected)
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
            atk = AutoSkillAction.Atk.np(CommandCard.NP.A)
        ).map { it.card }

        val expected = listOf(
            CommandCard.Face.B, CommandCard.Face.A, CommandCard.Face.C, CommandCard.Face.D, CommandCard.Face.E
        )

        assert(picked == expected)
    }
}