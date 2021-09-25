package com.mathewsachin.fategrandautomata.scripts.supportSelection

import com.mathewsachin.fategrandautomata.scripts.IFgoAutomataApi
import com.mathewsachin.fategrandautomata.scripts.Images
import kotlin.time.Duration

class FirstSupportSelection(
    fgAutomataApi: IFgoAutomataApi
): SupportSelectionProvider, IFgoAutomataApi by fgAutomataApi {
    override fun select(): SupportSelectionResult {
        Duration.seconds(0.5).wait()

        game.supportFirstSupportClick.click()

        // Handle the case of a friend not having set a support servant
        val supportPicked = game.supportScreenRegion.waitVanish(
            images[Images.SupportScreen],
            similarity = 0.85,
            timeout = Duration.seconds(10)
        )

        return if (supportPicked)
            SupportSelectionResult.Done
        else SupportSelectionResult.Refresh
    }
}