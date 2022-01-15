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
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

@ScriptScope
class ServantSelection @Inject constructor(
    api: IFgoAutomataApi,
    private val supportPrefs: ISupportPreferences,
    private val starChecker: SupportSelectionStarChecker
): IFgoAutomataApi by api {
    class FoundServant(val maxedSkills: List<Boolean>, val maxAscended: Boolean)

    suspend fun check(servants: List<String>, bounds: SupportBounds): FoundServant? {
        // TODO: Only check the upper part (excluding CE)
        val searchRegion = bounds.region intersect locations.support.listRegion ?: return null

        val maxAscended = isMaxAscended(searchRegion)

        if (supportPrefs.maxAscended && !maxAscended) {
            return null
        }

        val needMaxedSkills = listOf(
            supportPrefs.skill1Max,
            supportPrefs.skill2Max,
            supportPrefs.skill3Max
        )
        val skillCheckNeeded = needMaxedSkills.any { it }
        val maxedSkills = whichSkillsAreMaxed(bounds.region)

        if (skillCheckNeeded && !checkMaxedSkills(needMaxedSkills, maxedSkills)) {
            return null
        }

        if (servants.isEmpty())
            return FoundServant(maxedSkills, maxAscended)

        val matched = coroutineScope {
            servants
                .flatMap { entry -> images.loadSupportPattern(SupportImageKind.Servant, entry) }
                .map {
                    async {
                        cropFriendLock(it).use { cropped ->
                            cropped in searchRegion
                        }
                    }
                }
                .any { it.await() }
        }

        return FoundServant(maxedSkills, maxAscended).takeIf { matched }
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
        val x = bounds.x + 1620

        val appendSkillsIntroduced = prefs.gameServer == GameServerEnum.Jp
        val skillMargin = if (appendSkillsIntroduced) 90 else 155

        val skillLoc = listOf(
            Location(x, y),
            Location(x + skillMargin, y),
            Location(x + 2 * skillMargin, y)
        )

        return skillLoc
            .map {
                val skillRegion = Region(it, Size(50, 50))

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