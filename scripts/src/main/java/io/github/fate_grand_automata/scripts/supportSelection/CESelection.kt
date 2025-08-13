package io.github.fate_grand_automata.scripts.supportSelection

import io.github.fate_grand_automata.SupportImageKind
import io.github.fate_grand_automata.scripts.IFgoAutomataApi
import io.github.fate_grand_automata.scripts.Images
import io.github.fate_grand_automata.scripts.enums.BondCEEffectEnum
import io.github.fate_grand_automata.scripts.prefs.ISupportPreferences
import io.github.lib_automata.Region
import io.github.lib_automata.dagger.ScriptScope
import javax.inject.Inject
import kotlin.math.min

@ScriptScope
class CESelection @Inject constructor(
    api: IFgoAutomataApi,
    private val supportPrefs: ISupportPreferences,
    private val starChecker: SupportSelectionStarChecker,
    private val grandChecker: SupportSelectionGrandChecker
) : IFgoAutomataApi by api {
    fun check(ces: List<String>, bounds: SupportBounds): Boolean {
        // TODO: Only check the lower part (excluding Servant)
        val searchRegion = bounds.region.clip(locations.support.listRegion)
        val grandSearchRegion = bounds.region.clip(locations.support.grandCeListRegion)

        if (ces.isEmpty()) {
            // servant must not have blank ce
            return !searchRegion.exists(images[Images.SupportBlankCE])
        }

        val (bondCes, ces) = ces.partition {
            it == BondCEEffectEnum.Default.value || it == BondCEEffectEnum.NP.value
        }

        if (isGrandServant(grandSearchRegion)) {
            val matched = ces
                .flatMap { entry -> images.loadSupportPattern(SupportImageKind.CE, entry) }
                .mapNotNull {
                    grandSearchRegion.find(it)
                }
                .filter {
                    !supportPrefs.mlb || isGrandLimitBroken(it.region)
                }

            val grandCeRegion1 = locations.support.grandCeRegion1.copy(y = searchRegion.y + locations.support.grandCeRegion1.y)
            val grandCeRegion2 = locations.support.grandCeRegion2.copy(y = searchRegion.y + locations.support.grandCeRegion2.y)
            val grandCeRegion3 = locations.support.grandCeRegion3.copy(y = searchRegion.y + locations.support.grandCeRegion3.y)
            val bondRegion = locations.support.bondCeRegion.copy(y = searchRegion.y + locations.support.bondCeRegion.y)

            var count = if (matched.any { grandCeRegion1.contains(it.region) }) 1 else 0
            count += if (matched.any { grandCeRegion3.contains(it.region)}) 1 else 0

            if (matched.any { grandCeRegion2.contains(it.region)}) {
                count += 1
            } else {
                val bondMatched = bondCes
                    .mapNotNull {
                        val image = if (BondCEEffectEnum.Default.value == it) images[Images.BondCeEffectDefault] else images[Images.BondCeEffectNP]
                        bondRegion.find(image)
                    }.any()

                count += if (bondMatched) 1 else 0
            }

            return supportPrefs.ceMatchCount.ordinal < count
        } else {
            val matched = ces
                .flatMap { entry -> images.loadSupportPattern(SupportImageKind.CE, entry) }
                .mapNotNull {
                    searchRegion.find(it)
                }
                .filter {
                    !supportPrefs.mlb || isLimitBroken(it.region)
                }
            // When Pref is exceed ceMatchCount.One, I want non-grand's CE matching to fail.
            return matched.isNotEmpty()  &&  supportPrefs.ceMatchCount.ordinal < min(matched.count(), 1)
        }
    }

    private fun isLimitBroken(craftEssence: Region): Boolean {
        val limitBreakRegion = locations.support.limitBreakRegion
            .copy(y = craftEssence.y)

        return starChecker.isStarPresent(limitBreakRegion)
    }

    private fun isGrandServant(ces: Region): Boolean {
        val multiCeLabelRegion = locations.support.grandCeLabelRegion
            .copy(y = ces.y + 40)

        return grandChecker.isGrandPresent(multiCeLabelRegion)
    }

    private fun isGrandLimitBroken(craftEssence: Region): Boolean {
        val limitBreakRegion = locations.support.grandLimitBreakRegion
            .copy(y = craftEssence.y)

        return starChecker.isStarPresent(limitBreakRegion)
    }

}