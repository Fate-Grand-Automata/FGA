package com.mathewsachin.fategrandautomata.scripts.entrypoints

import com.mathewsachin.fategrandautomata.core.*
import com.mathewsachin.fategrandautomata.scripts.modules.initScaling

class AutoFriendGacha : EntryPoint() {
    private val first10SummonClick = Location(1400, 1120)
    private val okClick = Location(1600, 1120)
    private val continue10SummonClick = Location(1600, 1420)
    private val skipRapidClick = Location(1600, 1300)

    override fun script() {
        initScaling()

        first10SummonClick.click()
        okClick.click()

        while (true)
        {
            continue10SummonClick.click()
            okClick.click()
            AutomataApi.wait(3)

            skipRapidClick.continueClick(15)
            AutomataApi.wait(0.5)
        }
    }
}