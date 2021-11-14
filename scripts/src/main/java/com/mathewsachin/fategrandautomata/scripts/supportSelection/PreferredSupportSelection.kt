package com.mathewsachin.fategrandautomata.scripts.supportSelection

import com.mathewsachin.fategrandautomata.SupportImageKind
import com.mathewsachin.fategrandautomata.scripts.IFgoAutomataApi
import com.mathewsachin.fategrandautomata.scripts.Images
import com.mathewsachin.fategrandautomata.scripts.ScriptLog
import com.mathewsachin.fategrandautomata.scripts.entrypoints.AutoBattle
import com.mathewsachin.fategrandautomata.scripts.enums.GameServerEnum
import com.mathewsachin.fategrandautomata.scripts.prefs.ISupportPreferences
import com.mathewsachin.libautomata.IPattern
import com.mathewsachin.libautomata.Location
import com.mathewsachin.libautomata.Region
import com.mathewsachin.libautomata.Size
import com.mathewsachin.libautomata.dagger.ScriptScope
import javax.inject.Inject
import kotlin.streams.asStream
import kotlin.streams.toList

@ScriptScope
class PreferredSupportSelection @Inject constructor(
    supportPrefs: ISupportPreferences,
    api: IFgoAutomataApi
): SpecificSupportSelection(supportPrefs, api) {
    private val servants = supportPrefs.preferredServants
    private val ces = supportPrefs.preferredCEs

    private enum class Mode {
        Servants, CEs, Both, None
    }

    private val mode by lazy {
        val hasServants = servants.isNotEmpty()
        val hasCEs = ces.isNotEmpty()

        when {
            hasServants && hasCEs -> Mode.Both
            hasServants -> Mode.Servants
            hasCEs -> Mode.CEs
            else -> Mode.None
        }
    }

    override fun search() = when (mode) {
        Mode.Servants -> findServants().firstOrNull() ?: SpecificSupportSearchResult.NotFound
        Mode.CEs -> {
            findCraftEssences(locations.support.listRegion)
                .map { SpecificSupportSearchResult.Found(it.region) }
                .firstOrNull() ?: SpecificSupportSearchResult.NotFound
        }
        Mode.Both -> searchServantAndCE()
        Mode.None -> throw AutoBattle.BattleExitException(AutoBattle.ExitReason.SupportSelectionPreferredNotSet)
    }

    private fun searchServantAndCE(): SpecificSupportSearchResult =
        findServants()
            .mapNotNull {
                val supportBounds = when (it) {
                    is SpecificSupportSearchResult.FoundWithBounds -> it.Bounds
                    else -> findSupportBounds(it.Support)
                }

                val ceBounds = locations.support.defaultCeBounds + Location(0, supportBounds.y)
                findCraftEssences(ceBounds).firstOrNull()
                    ?.let { ce -> FoundServantAndCE(supportBounds, ce) }
            }
            .sortedBy { it.ce }
            .map { SpecificSupportSearchResult.FoundWithBounds(it.ce.region, it.supportBounds) }
            .firstOrNull() ?: SpecificSupportSearchResult.NotFound

    private data class FoundServantAndCE(val supportBounds: Region, val ce: FoundCE)
    private data class FoundCE(val region: Region, val mlb: Boolean) : Comparable<FoundCE> {
        override fun compareTo(other: FoundCE) = when {
            // Prefer MLB
            mlb && !other.mlb -> -1
            !mlb && other.mlb -> 1
            else -> region.compareTo(other.region)
        }
    }

    private fun findCraftEssences(SearchRegion: Region): List<FoundCE> =
        ces
            .flatMap { entry -> images.loadSupportPattern(SupportImageKind.CE, entry) }
            .parallelStream()
            .flatMap { pattern ->
                SearchRegion
                    .findAll(pattern)
                    .asStream()
                    .map { FoundCE(it.region, isLimitBroken(it.region)) }
                    .filter { !supportPrefs.mlb || it.mlb }
            }
            .toList()
            .sorted()

    private fun findServants(): List<SpecificSupportSearchResult.Found> =
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
                                SpecificSupportSearchResult.FoundWithBounds(it.region, findSupportBounds(it.region))
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
    private fun cropFriendLock(servant: IPattern): IPattern {
        val lockCropLeft = 15
        val lockCropRegion = Region(
            lockCropLeft, 0,
            servant.width - lockCropLeft, servant.height
        )
        return servant.crop(lockCropRegion)
    }

    private fun isStarPresent(region: Region): Boolean {
        val mlbSimilarity = prefs.support.mlbSimilarity
        return region.exists(images[Images.LimitBroken], similarity = mlbSimilarity)
    }

    private fun isMaxAscended(servant: Region): Boolean {
        val maxAscendedRegion = locations.support.maxAscendedRegion
            .copy(y = servant.y)

        return isStarPresent(maxAscendedRegion)
    }

    private fun isLimitBroken(CraftEssence: Region): Boolean {
        val limitBreakRegion = locations.support.limitBreakRegion
            .copy(y = CraftEssence.y)

        return isStarPresent(limitBreakRegion)
    }

    private fun checkMaxedSkills(bounds: Region, needMaxedSkills: List<Boolean>): Boolean {
        val y = bounds.y + 325
        val x = bounds.x + 1620

        val appendSkillsIntroduced = prefs.gameServer == GameServerEnum.Jp
        val skillMargin = if (appendSkillsIntroduced) 90 else 155

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