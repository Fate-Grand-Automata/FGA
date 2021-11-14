package com.mathewsachin.fategrandautomata.scripts.supportSelection

import com.mathewsachin.fategrandautomata.scripts.IFgoAutomataApi
import com.mathewsachin.fategrandautomata.scripts.Images
import com.mathewsachin.fategrandautomata.scripts.ScriptLog
import com.mathewsachin.fategrandautomata.scripts.modules.Support
import com.mathewsachin.libautomata.Region
import com.mathewsachin.libautomata.dagger.ScriptScope
import javax.inject.Inject

@ScriptScope
class SupportBoundsFinder @Inject constructor(
    api: IFgoAutomataApi
): IFgoAutomataApi by api {
    fun findSupportBounds(support: Region) =
        locations.support.confirmSetupButtonRegion
            .findAll(
                images[Images.SupportConfirmSetupButton],
                Support.supportRegionToolSimilarity
            )
            .map {
                locations.support.defaultBounds
                    .copy(y = it.region.y - 70)
            }
            .firstOrNull { support in it }
            ?: locations.support.defaultBounds.also {
                messages.log(ScriptLog.DefaultSupportBounds)
            }
}