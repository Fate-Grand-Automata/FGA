package io.github.fate_grand_automata.scripts.supportSelection

import io.github.fate_grand_automata.SupportImageKind
import io.github.fate_grand_automata.scripts.IFgoAutomataApi
import io.github.fate_grand_automata.scripts.Images
import io.github.fate_grand_automata.scripts.enums.BondCEEffectEnum
import io.github.fate_grand_automata.scripts.prefs.ISupportPreferences
import io.github.lib_automata.Region
import io.github.lib_automata.dagger.ScriptScope
import javax.inject.Inject

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
            val grandCeRegion3 = locations.support.grandCeRegion3.copy(y = searchRegion.y + locations.support.grandCeRegion3.y)
            val bondRegion = locations.support.bondCeRegion.copy(y = searchRegion.y + locations.support.bondCeRegion.y)

            val normalMatch = matched.any { grandCeRegion1.contains(it.region) }
            val rewardMatch = matched.any { grandCeRegion3.contains(it.region) }

            val ceSlotMatch = if (supportPrefs.requireBothNormalAndRewardMatch) {
                normalMatch && rewardMatch
            } else {
                normalMatch || rewardMatch
            }

            val bondEffectMatch = when (supportPrefs.bondCEEffect) {
                BondCEEffectEnum.Default -> {
                    bondRegion.find(images[Images.BondCeEffectDefault]) != null
                }
                BondCEEffectEnum.NP -> {
                    bondRegion.find(images[Images.BondCeEffectNP]) != null
                }
                else -> {
                    true
                }
            }

            return ceSlotMatch && bondEffectMatch
        } else {
            val matched = ces
                .flatMap { entry -> images.loadSupportPattern(SupportImageKind.CE, entry) }
                .mapNotNull {
                    searchRegion.find(it)
                }
                .filter {
                    !supportPrefs.mlb || isLimitBroken(it.region)
                }
            return matched.isNotEmpty()
                && !supportPrefs.requireBothNormalAndRewardMatch
                && supportPrefs.bondCEEffect == BondCEEffectEnum.Ignore
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