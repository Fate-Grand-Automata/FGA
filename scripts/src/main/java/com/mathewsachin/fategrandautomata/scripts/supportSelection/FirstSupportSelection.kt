package com.mathewsachin.fategrandautomata.scripts.supportSelection

import com.mathewsachin.fategrandautomata.scripts.IFgoAutomataApi
import com.mathewsachin.fategrandautomata.scripts.Images
import com.mathewsachin.libautomata.dagger.ScriptScope
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@ScriptScope
class FirstSupportSelection @Inject constructor(
    api: IFgoAutomataApi
) : SupportSelectionProvider, IFgoAutomataApi by api {
    override fun select(): SupportSelectionResult {
        0.5.seconds.wait()

        locations.support.firstSupportClick.click()

        // Handle the case of a friend not having set a support servant
        val supportPicked = locations.support.screenCheckRegion.waitVanish(
            images[Images.SupportScreen],
            similarity = 0.85,
            timeout = 10.seconds
        )

        return if (supportPicked)
            SupportSelectionResult.Done
        else SupportSelectionResult.Refresh
    }
}