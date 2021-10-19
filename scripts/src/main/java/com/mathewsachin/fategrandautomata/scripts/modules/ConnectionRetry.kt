package com.mathewsachin.fategrandautomata.scripts.modules

import com.mathewsachin.fategrandautomata.scripts.IFgoAutomataApi
import com.mathewsachin.fategrandautomata.scripts.Images
import com.mathewsachin.libautomata.dagger.ScriptScope
import javax.inject.Inject
import kotlin.time.Duration

@ScriptScope
class ConnectionRetry @Inject constructor(
    fgAutomataApi: IFgoAutomataApi
) : IFgoAutomataApi by fgAutomataApi {
    fun needsToRetry() =
        images[Images.Retry] in locations.retryRegion

    fun retry() {
        locations.retryRegion.click()

        Duration.seconds(2).wait()
    }
}