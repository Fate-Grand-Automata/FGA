package io.github.fate_grand_automata.scripts.supportSelection

import io.github.fate_grand_automata.SupportImageKind
import io.github.fate_grand_automata.scripts.IFgoAutomataApi
import io.github.fate_grand_automata.scripts.Images
import io.github.fate_grand_automata.scripts.ScriptLog
import io.github.fate_grand_automata.scripts.prefs.ISupportPreferences
import io.github.lib_automata.Location
import io.github.lib_automata.Pattern
import io.github.lib_automata.Region
import io.github.lib_automata.Size
import io.github.lib_automata.dagger.ScriptScope
import javax.inject.Inject

@ScriptScope
class ServantSelection @Inject constructor(
    api: IFgoAutomataApi,
    private val supportPrefs: ISupportPreferences,
    private val starChecker: SupportSelectionStarChecker
) : IFgoAutomataApi by api {
    fun check(servants: List<String>, bounds: SupportBounds): Boolean {
        // TODO: Only check the upper part (excluding CE)
        val searchRegion = bounds.region.clip(locations.support.listRegion)

        if (servants.isEmpty())
            return true

        val matched = servants
            .flatMap { entry -> images.loadSupportPattern(SupportImageKind.Servant, entry) }
            .mapNotNull {
                cropFriendLock(it).use { cropped ->
                    searchRegion.find(cropped)
                }
            }
            .filter {
                !supportPrefs.maxAscended || isMaxAscended(it.region)
            }
            .filter {
                val needMaxedSkills = listOf(
                    supportPrefs.skill1Max,
                    supportPrefs.skill2Max,
                    supportPrefs.skill3Max
                )
                val skillCheckNeeded = needMaxedSkills.any { it }
                !skillCheckNeeded || checkMaxedSkills(needMaxedSkills, whichSkillsAreMaxed(bounds.region))
            }

        return matched.isNotEmpty()
    }

    /**
     * If you lock your friends, a lock icon shows on the left of servant image,
     * which can cause matching to fail.
     *
     * Instead of modifying in-built images and Support Image Maker,
     * which would need everyone to regenerate their images,
     * crop out the part which can potentially have the lock.
     */
    private fun cropFriendLock(servant: Pattern): Pattern {
        val lockCropLeft = 15
        val lockCropRegion = Region(
            lockCropLeft, 0,
            servant.width - lockCropLeft, servant.height
        )
        return servant.crop(lockCropRegion)
    }

    private fun isMaxAscended(servant: Region): Boolean {
        val maxAscendedRegion = locations.support.maxAscendedRegion
            .copy(y = servant.y)

        return starChecker.isStarPresent(maxAscendedRegion)
    }

    private fun whichSkillsAreMaxed(bounds: Region): List<Boolean> {
        val y = bounds.y + 325
        val x = bounds.x + 1610

        val skillMargin = 90

        val skillLoc = listOf(
            Location(x, y),
            Location(x + skillMargin, y),
            Location(x + 2 * skillMargin, y)
        )

        return skillLoc
            .map {
                val skillRegion = Region(it, Size(60, 50))

                skillRegion.exists(images[Images.SkillTen], similarity = 0.68)
            }
    }

    private fun checkMaxedSkills(expectedSkills: List<Boolean>, actualSkills: List<Boolean>): Boolean {
        val result = expectedSkills
            .zip(actualSkills) { expected, actual ->
                !expected || actual
            }

        messages.log(
            ScriptLog.MaxSkills(
                needMaxedSkills = expectedSkills,
                isSkillMaxed = result
            )
        )

        return result.all { it }
    }
}