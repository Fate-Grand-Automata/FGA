package io.github.fate_grand_automata.scripts.supportSelection

import io.github.fate_grand_automata.scripts.IFgoAutomataApi
import io.github.fate_grand_automata.scripts.Images
import io.github.fate_grand_automata.scripts.modules.Support
import io.github.lib_automata.Region
import io.github.lib_automata.dagger.ScriptScope
import javax.inject.Inject

data class SupportBounds(val region: Region)

@ScriptScope
class SupportBoundsFinder @Inject constructor(
    api: IFgoAutomataApi
) : IFgoAutomataApi by api {
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
            }.sortedBy {
                it.region
            }
}