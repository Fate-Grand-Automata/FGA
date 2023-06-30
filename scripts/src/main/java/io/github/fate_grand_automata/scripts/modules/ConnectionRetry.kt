package io.github.fate_grand_automata.scripts.modules

import io.github.fate_grand_automata.scripts.IFgoAutomataApi
import io.github.fate_grand_automata.scripts.Images
import io.github.lib_automata.dagger.ScriptScope
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