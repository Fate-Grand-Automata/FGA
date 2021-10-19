package com.mathewsachin.fategrandautomata.scripts

import com.mathewsachin.fategrandautomata.scripts.enums.ShuffleCardsEnum
import com.mathewsachin.fategrandautomata.scripts.models.FieldSlot
import com.mathewsachin.fategrandautomata.scripts.models.NPUsage
import com.mathewsachin.fategrandautomata.scripts.models.TeamSlot
import com.mathewsachin.fategrandautomata.scripts.modules.ServantTracker
import com.mathewsachin.fategrandautomata.scripts.modules.ShuffleChecker
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert
import org.junit.Test

class ShuffleCheckerTest {
    private fun init(
        deployed: Map<FieldSlot, TeamSlot> = mapOf(
            FieldSlot.A to TeamSlot.A,
            FieldSlot.B to TeamSlot.B,
            FieldSlot.C to TeamSlot.C
        )
    ): ShuffleChecker {
        val servantTracker: ServantTracker = mockk()
        every { servantTracker.deployed } returns deployed

        return ShuffleChecker(
            servantTracker = servantTracker
        )
    }

    @Test
    fun noShuffle() {
        val checker = init()

        val should = checker.shouldShuffle(ShuffleCardsEnum.None, FaceCardPriorityTest.lineup1, NPUsage.none)
        Assert.assertFalse(should)
    }
}