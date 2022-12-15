package com.mathewsachin.fategrandautomata.scripts.supportSelection

import com.mathewsachin.fategrandautomata.SupportImageKind
import com.mathewsachin.fategrandautomata.scripts.IFgoAutomataApi
import com.mathewsachin.fategrandautomata.scripts.Images
import com.mathewsachin.fategrandautomata.scripts.ScriptLog
import com.mathewsachin.fategrandautomata.scripts.enums.GameServerEnum
import com.mathewsachin.fategrandautomata.scripts.prefs.ISupportPreferences
import com.mathewsachin.libautomata.Location
import com.mathewsachin.libautomata.Pattern
import com.mathewsachin.libautomata.Region
import com.mathewsachin.libautomata.Size
import com.mathewsachin.libautomata.dagger.ScriptScope
import javax.inject.Inject
import kotlin.streams.toList

@ScriptScope
class ServantSelection @Inject constructor(
    api: IFgoAutomataApi,
    private val supportPrefs: ISupportPreferences,
    private val starChecker: SupportSelectionStarChecker,
    private val boundsFinder: SupportBoundsFinder
) : IFgoAutomataApi by api {
    fun findServants(servants: List<String>): List<SpecificSupportSearchResult.Found> =
        servants
            .flatMap { entry -> images.loadSupportPattern(SupportImageKind.Servant, entry) }
            .parallelStream()
            .flatMap { pattern ->
                val needMaxedSkills = listOf(
                    supportPrefs.skill1Max,
                    supportPrefs.skill2Max,
                    supportPrefs.skill3Max
                )
                val skillCheckNeeded = needMaxedSkills.any { it }

                cropFriendLock(pattern).use { cropped ->
                    locations.support.listRegion
                        .findAll(cropped)
                        .filter { !supportPrefs.maxAscended || isMaxAscended(it.region) }
                        .map {
                            if (skillCheckNeeded)
                                SpecificSupportSearchResult.FoundWithBounds(
                                    it.region,
                                    boundsFinder.findSupportBounds(it.region)
                                )
                            else SpecificSupportSearchResult.Found(it.region)
                        }
                        .filter {
                            it !is SpecificSupportSearchResult.FoundWithBounds || checkMaxedSkills(it.Bounds, needMaxedSkills)
                        }
                        // We want the processing to be finished before cropped pattern is released
                        .toList()
                        .stream()
                }
            }
            .toList()
            .sortedBy { it.Support }

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

    private fun checkMaxedSkills(bounds: Region, needMaxedSkills: List<Boolean>): Boolean {
        val y = bounds.y + 325
        val x = bounds.x + 1620

        val skillMargin = when (prefs.gameServer) {
            GameServerEnum.Tw -> 155
            else -> 90
        }

        val skillLoc = listOf(
            Location(x, y),
            Location(x + skillMargin, y),
            Location(x + 2 * skillMargin, y)
        )

        val result = skillLoc
            .zip(needMaxedSkills)
            .map { (location, shouldBeMaxed) ->
                if (!shouldBeMaxed)
                    true
                else {
                    val skillRegion = Region(location, Size(50, 50))

                    skillRegion.exists(images[Images.SkillTen], similarity = 0.68)
                }
            }

        messages.log(
            ScriptLog.MaxSkills(
                needMaxedSkills = needMaxedSkills,
                isSkillMaxed = result
            )
        )

        return result.all { it }
    }
}