package com.mathewsachin.fategrandautomata.scripts.modules

import com.mathewsachin.fategrandautomata.scripts.IFgoAutomataApi
import com.mathewsachin.fategrandautomata.scripts.Images
import com.mathewsachin.libautomata.dagger.ScriptScope
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@ScriptScope
class ConnectionRetry @Inject constructor(
    api: IFgoAutomataApi
) : IFgoAutomataApi by api {
    fun needsToRetry() =
        images[Images.Retry] in locations.retryRegion

    fun retry() {
        locations.retryRegion.click()

        2.seconds.wait()
    }
}