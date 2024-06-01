package io.github.fate_grand_automata.scripts.supportSelection

import io.github.fate_grand_automata.SupportImageKind
import io.github.fate_grand_automata.scripts.IFgoAutomataApi
import io.github.fate_grand_automata.scripts.Images
import io.github.fate_grand_automata.scripts.ScriptLog
import io.github.fate_grand_automata.scripts.enums.GameServer
import io.github.fate_grand_automata.scripts.prefs.ISupportPreferences
import io.github.lib_automata.Location
import io.github.lib_automata.Pattern
import io.github.lib_automata.Region
import io.github.lib_automata.Size
import io.github.lib_automata.dagger.ScriptScope
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

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

    fun checkForAppends(bounds: List<Region>): List<Region> {
        if (bounds.isEmpty()) return emptyList()

        var matched: List<Region> = emptyList()

        // Don't need to check all just the first available one
        checkIfSkillsAreToggled(bounds.first())

        useSameSnapIn {
            matched = bounds
                .filter { bound ->
                    val followRegion = locations.support.friendRegion.copy(
                        y = bound.y + 70,
                        height = 350
                    )
                    val isFollow = images[Images.Follow] in followRegion

                    if (isFollow) {
                        true
                    } else {
                        val needMaxedAppends = listOf(
                            supportPrefs.append1Max,
                            supportPrefs.append2Max,
                            supportPrefs.append3Max
                        )
                        val appendCheckNeeded = needMaxedAppends.any { it }
                        !appendCheckNeeded ||
                                checkMaxedSkills(needMaxedAppends, whichSkillsAreMaxed(bound), isSkill = false)
                    }
                }
        }

        // return to the skill display
        repeat(2) {
            locations.support.skillDisplayRegion.click()
            0.2.seconds.wait()
        }

        return matched

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

    private fun checkIfSkillsAreToggled(bounds: Region) {
        if (prefs.gameServer in listOf(GameServer.Tw, GameServer.Kr, GameServer.Cn)) {
            locations.support.skillDisplayRegion.click()
            0.25.seconds.wait()
            return
        }
        val y = bounds.y + 225
        val skillMargin = 90
        val x = bounds.x + 1610
        val width = 3 * skillMargin

        val skillRegion = Region(x, y, width, 100)

        val isInServantSkills = skillRegion.exists(images[Images.SupportServantSkill], similarity = 0.70)

        if (isInServantSkills) {
            locations.support.skillDisplayRegion.click()
            0.25.seconds.wait()
        }
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

    private fun checkMaxedSkills(
        expectedSkills: List<Boolean>,
        actualSkills: List<Boolean>,
        isSkill: Boolean = true
    ): Boolean {
        val result = expectedSkills
            .zip(actualSkills) { expected, actual ->
                !expected || actual
            }

        if (isSkill) {
            messages.log(
                ScriptLog.MaxSkills(
                    needMaxedSkills = expectedSkills,
                    isSkillMaxed = result
                )
            )
        } else {
            messages.log(
                ScriptLog.MaxAppends(
                    needMaxedAppend = expectedSkills,
                    isAppendMaxed = result
                )
            )
        }

        return result.all { it }
    }
}