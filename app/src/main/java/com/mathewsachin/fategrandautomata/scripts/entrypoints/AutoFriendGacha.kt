package com.mathewsachin.fategrandautomata.scripts.entrypoints

import com.mathewsachin.libautomata.*
import com.mathewsachin.fategrandautomata.scripts.modules.initScaling
import kotlin.time.seconds

/**
 * Continually triggers 10x Summon, intended for FP summons, but could also be used for SQ summons.
 */
class AutoFriendGacha : EntryPoint() {
    private val first10SummonClick = Location(1400, 1120)
    private val okClick = Location(1600, 1120)
    private val continue10SummonClick = Location(1600, 1420)
    private val skipRapidClick = Location(1600, 1300)

    override fun script(): Nothing {
        initScaling()

        first10SummonClick.click()
        okClick.click()

        while (true) {
            continue10SummonClick.click()
            okClick.click()
            3.seconds.wait()

            skipRapidClick.click(15)
            0.5.seconds.wait()
        }
    }
}