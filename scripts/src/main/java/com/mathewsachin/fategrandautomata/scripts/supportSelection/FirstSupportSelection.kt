package com.mathewsachin.fategrandautomata.scripts.supportSelection

import com.mathewsachin.fategrandautomata.scripts.IFgoAutomataApi
import com.mathewsachin.fategrandautomata.scripts.Images
import com.mathewsachin.libautomata.dagger.ScriptScope
import javax.inject.Inject
import kotlin.time.Duration

@ScriptScope
class FirstSupportSelection @Inject constructor(
    fgAutomataApi: IFgoAutomataApi
) : SupportSelectionProvider, IFgoAutomataApi by fgAutomataApi {
    override fun select(): SupportSelectionResult {
        Duration.seconds(0.5).wait()

        if (images[Images.SupportNotFound] in locations.support.notFoundRegion) {
            return SupportSelectionResult.Refresh;
        }

        locations.support.firstSupportClick.click()

        // Handle the case of a friend not having set a support servant
        val supportPicked = locations.support.screenCheckRegion.waitVanish(
            images[Images.SupportScreen],
            similarity = 0.85,
            timeout = Duration.seconds(10)
        )

        return if (supportPicked)
            SupportSelectionResult.Done
        else SupportSelectionResult.Refresh
    }
}