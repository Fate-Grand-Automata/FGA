package com.mathewsachin.fategrandautomata.scripts.supportSelection

import com.mathewsachin.fategrandautomata.scripts.IFgoAutomataApi
import com.mathewsachin.fategrandautomata.scripts.Images
import com.mathewsachin.fategrandautomata.scripts.modules.Support
import com.mathewsachin.libautomata.Region
import com.mathewsachin.libautomata.dagger.ScriptScope
import javax.inject.Inject

data class SupportBounds(val region: Region)

@ScriptScope
class SupportBoundsFinder @Inject constructor(
    api: IFgoAutomataApi
): IFgoAutomataApi by api {
    fun all(): Sequence<SupportBounds> =
        locations.support.confirmSetupButtonRegion
            .findAll(
                images[Images.SupportConfirmSetupButton],
                Support.supportRegionToolSimilarity
            )
            .map {
                SupportBounds(
                    locations.support.defaultBounds
                        .copy(y = it.region.y - 70)
                )
            }
}