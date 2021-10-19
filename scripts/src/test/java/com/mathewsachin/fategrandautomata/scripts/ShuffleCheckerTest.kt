package com.mathewsachin.fategrandautomata.scripts

import com.mathewsachin.fategrandautomata.scripts.enums.ShuffleCardsEnum
import com.mathewsachin.fategrandautomata.scripts.models.NPUsage
import com.mathewsachin.fategrandautomata.scripts.modules.ShuffleChecker
import org.junit.Assert
import org.junit.Test

class ShuffleCheckerTest {
    @Test
    fun noShuffle() {
        val checker = ShuffleChecker()

        val should = checker.shouldShuffle(ShuffleCardsEnum.None, FaceCardPriorityTest.lineup1, NPUsage.none)
        Assert.assertFalse(should)
    }
}