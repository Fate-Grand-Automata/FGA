package com.mathewsachin.fategrandautomata.scripts.entrypoints

import com.mathewsachin.fategrandautomata.scripts.ImageLocator
import com.mathewsachin.fategrandautomata.scripts.modules.initScaling
import com.mathewsachin.libautomata.*
import kotlin.time.seconds

/**
 * Continually triggers 10x Summon, intended for FP summons, but could also be used for SQ summons.
 */
class AutoFriendGacha : EntryPoint() {
    private val first10SummonClick = Location(1400, 1120)
    private val okClick = Location(1600, 1120)
    private val continueSummonClick = Location(1600, 1420)
    private val skipRapidClick = Location(2520, 1400)

    private val continueSummonRegion = Region(1244, 1264, 580, 170)

    override fun script(): Nothing {
        initScaling()

        first10SummonClick.click()
        0.3.seconds.wait()
        okClick.click()

        while (true) {
            when {
                continueSummonRegion.exists(ImageLocator.FpSummonContinue) -> {
                    continueSummonClick.click()
                    0.3.seconds.wait()
                    okClick.click()
                    3.seconds.wait()
                }
                else -> skipRapidClick.click(15)
            }
        }
    }
}