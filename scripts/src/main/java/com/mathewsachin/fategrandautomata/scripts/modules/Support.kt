package com.mathewsachin.fategrandautomata.scripts.modules

import com.mathewsachin.fategrandautomata.scripts.IFgoAutomataApi
import com.mathewsachin.fategrandautomata.scripts.ISwipeLocations
import com.mathewsachin.fategrandautomata.scripts.enums.SupportClass
import com.mathewsachin.fategrandautomata.scripts.enums.SupportSelectionModeEnum
import com.mathewsachin.fategrandautomata.scripts.models.SearchFunctionResult
import com.mathewsachin.fategrandautomata.scripts.models.SearchVisibleResult
import com.mathewsachin.libautomata.*
import timber.log.Timber
import timber.log.debug
import java.util.*
import kotlin.streams.asStream
import kotlin.streams.toList
import kotlin.time.TimeMark
import kotlin.time.TimeSource
import kotlin.time.seconds

private data class PreferredCEEntry(val Name: String, val PreferMlb: Boolean)

private typealias SearchFunction = () -> SearchFunctionResult

const val supportRegionToolSimilarity = 0.75

const val limitBrokenCharacter = '*'

class Support(
    fgAutomataApi: IFgoAutomataApi,
    val swipeLocations: ISwipeLocations
) : IFgoAutomataApi by fgAutomataApi {
    private var preferredServantArray = listOf<String>()
    private var friendNameArray = listOf<String>()
    private var preferredCEArray = listOf<PreferredCEEntry>()

    private fun String.process(): List<String> {
        return this
            .split(',')
            .map { it.trim() }
            .filter { it.isNotBlank() && it.toLowerCase(Locale.US) != "any" }
    }

    private val autoSkillPrefs get() = prefs.selectedBattleConfig.support

    fun init() {
        friendNameArray = autoSkillPrefs.friendNames.process()
        preferredServantArray = autoSkillPrefs.preferredServants.process()

        preferredCEArray = autoSkillPrefs.preferredCEs
            .process()
            .map {
                val mlb = it.startsWith(limitBrokenCharacter)
                var name = it

                if (mlb) {
                    name = name.substring(1)
                }

                PreferredCEEntry(name, mlb)
            }
    }

    fun selectSupport(SelectionMode: SupportSelectionModeEnum, continuing: Boolean): Boolean {
        waitForSupportScreenToLoad()

        if (!continuing && autoSkillPrefs.supportClass != SupportClass.None) {
            autoSkillPrefs.supportClass.clickLocation.click()

            0.5.seconds.wait()
        }

        return when (SelectionMode) {
            SupportSelectionModeEnum.First -> selectFirst()
            SupportSelectionModeEnum.Manual -> selectManual()
            SupportSelectionModeEnum.Friend -> selectFriend()
            SupportSelectionModeEnum.Preferred -> {
                val searchMethod = decideSearchMethod()
                selectPreferred(searchMethod)
            }
        }
    }

    private fun selectManual(): Boolean {
        throw ScriptExitException(messages.supportSelectionManual)
    }

    private var lastSupportRefreshTimestamp: TimeMark? = null
    private val supportRefreshThreshold = 10.seconds

    private fun refreshSupportList() {
        lastSupportRefreshTimestamp?.elapsedNow()?.let { elapsed ->
            val toWait = supportRefreshThreshold - elapsed

            if (toWait.isPositive()) {
                toast(messages.supportListUpdatedIn(toWait))

                toWait.wait()
            }
        }

        Game.supportUpdateClick.click()
        1.seconds.wait()

        Game.supportUpdateYesClick.click()

        waitForSupportScreenToLoad()
        updateLastSupportRefreshTimestamp()
    }

    private fun updateLastSupportRefreshTimestamp() {
        lastSupportRefreshTimestamp = TimeSource.Monotonic.markNow()
    }

    private fun waitForSupportScreenToLoad() {
        while (true) {
            when {
                needsToRetry() -> retry()
                // wait for dialogs to close
                images.supportExtra !in Game.supportExtraRegion -> 1.seconds.wait()
                images.supportNotFound in Game.supportNotFoundRegion -> {
                    updateLastSupportRefreshTimestamp()
                    refreshSupportList()
                    return
                }
                Game.supportRegionToolSearchRegion.exists(
                    images.supportRegionTool,
                    Similarity = supportRegionToolSimilarity
                ) -> return
                images.guest in Game.supportFriendRegion -> return
            }
        }
    }

    private fun selectFirst(): Boolean {
        while (true) {
            0.5.seconds.wait()

            Game.supportFirstSupportClick.click()

            // Handle the case of a friend not having set a support servant
            if (Game.supportScreenRegion.waitVanish(
                    images.supportScreen,
                    Similarity = 0.85,
                    Timeout = 10.seconds
                )
            ) {
                return true
            }

            refreshSupportList()
        }
    }

    private fun searchVisible(SearchMethod: SearchFunction) =
        useSameSnapIn(fun(): SearchVisibleResult {
            if (!isFriend(Game.supportFriendRegion)) {
                // no friends on screen, so there's no point in scrolling anymore
                return SearchVisibleResult.NoFriendsFound
            }

            val result = SearchMethod()

            if (result is SearchFunctionResult.Found) {
                val bounds = when (result) {
                    is SearchFunctionResult.FoundWithBounds -> result.Bounds
                    // bounds are not returned by all methods
                    else -> findSupportBounds(result.Support)
                }

                if (!isFriend(bounds)) {
                    // found something, but it doesn't belong to a friend. keep scrolling
                    return SearchVisibleResult.NotFound
                }

                return SearchVisibleResult.Found(result.Support)
            }

            // nope, not found this time. keep scrolling
            return SearchVisibleResult.NotFound
        })

    private fun selectFriend(): Boolean {
        if (friendNameArray.isNotEmpty()) {
            return selectPreferred { findFriendName() }
        }

        throw ScriptExitException(messages.supportSelectionFriendNotSet)
    }

    private fun selectPreferred(SearchMethod: SearchFunction): Boolean {
        var numberOfSwipes = 0
        var numberOfUpdates = 0

        while (true) {
            val result = searchVisible(SearchMethod)
            val swipeLocation = swipeLocations.supportList

            when {
                result is SearchVisibleResult.Found -> {
                    result.support.click()
                    return true
                }
                result is SearchVisibleResult.NotFound
                        && numberOfSwipes < prefs.support.swipesPerUpdate -> {

                    swipe(swipeLocation.start, swipeLocation.end)

                    ++numberOfSwipes
                    0.3.seconds.wait()
                }
                numberOfUpdates < prefs.support.maxUpdates -> {
                    refreshSupportList()

                    ++numberOfUpdates
                    numberOfSwipes = 0
                }
                else -> {
                    // -- okay, we have run out of options, let's give up
                    Game.supportListTopClick.click()
                    return selectSupport(autoSkillPrefs.fallbackTo, true)
                }
            }
        }
    }

    data class FoundServantAndCE(val supportBounds: Region, val ce: FoundCE)

    private fun searchServantAndCE(): SearchFunctionResult =
        findServants()
            .mapNotNull {
                val supportBounds = when (it) {
                    is SearchFunctionResult.FoundWithBounds -> it.Bounds
                    else -> findSupportBounds(it.Support)
                }

                val ceBounds = Game.supportDefaultCeBounds + Location(0, supportBounds.Y)
                findCraftEssences(ceBounds).firstOrNull()
                    ?.let { ce -> FoundServantAndCE(supportBounds, ce) }
            }
            .sortedBy { it.ce }
            .map { SearchFunctionResult.FoundWithBounds(it.ce.region, it.supportBounds) }
            .firstOrNull() ?: SearchFunctionResult.NotFound

    private fun decideSearchMethod(): SearchFunction {
        val hasServants = preferredServantArray.isNotEmpty()
        val hasCraftEssences = preferredCEArray.isNotEmpty()

        return when {
            hasServants && hasCraftEssences -> { -> searchServantAndCE() }
            hasServants -> { -> findServants().firstOrNull() ?: SearchFunctionResult.NotFound }
            hasCraftEssences -> { ->
                findCraftEssences(Game.supportListRegion)
                    .map { SearchFunctionResult.Found(it.region) }
                    .firstOrNull() ?: SearchFunctionResult.NotFound
            }
            else -> throw ScriptExitException(messages.supportSelectionPreferredNotSet)
        }
    }

    private fun findFriendName(): SearchFunctionResult {
        for (friendName in friendNameArray) {
            // Cached pattern. Don't dispose here.
            val pattern = images.loadSupportPattern(friendName)

            for (theFriend in Game.supportFriendsRegion.findAll(pattern).sorted()) {
                return SearchFunctionResult.Found(theFriend.Region)
            }
        }

        return SearchFunctionResult.NotFound
    }

    private fun findServants(): List<SearchFunctionResult.Found> =
        preferredServantArray
            .parallelStream()
            .flatMap { entry ->
                val pattern = images.loadSupportPattern(entry)

                val needMaxedSkills = listOf(
                    autoSkillPrefs.skill1Max,
                    autoSkillPrefs.skill2Max,
                    autoSkillPrefs.skill3Max
                )
                val skillCheckNeeded = needMaxedSkills.any { it }

                cropFriendLock(pattern).use { cropped ->
                    Game.supportListRegion
                        .findAll(cropped)
                        .filter { !autoSkillPrefs.maxAscended || isMaxAscended(it.Region) }
                        .map {
                            if (skillCheckNeeded)
                                SearchFunctionResult.FoundWithBounds(it.Region, findSupportBounds(it.Region))
                            else SearchFunctionResult.Found(it.Region)
                        }
                        .filter {
                            it !is SearchFunctionResult.FoundWithBounds || checkMaxedSkills(it.Bounds, needMaxedSkills)
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

    data class FoundCE(val region: Region, val mlb: Boolean) : Comparable<FoundCE> {
        override fun compareTo(other: FoundCE) = when {
            // Prefer MLB
            mlb && !other.mlb -> -1
            !mlb && other.mlb -> 1
            else -> region.compareTo(other.region)
        }
    }

    private fun findCraftEssences(SearchRegion: Region): List<FoundCE> =
        preferredCEArray
            .parallelStream()
            .flatMap { entry ->
                val pattern = images.loadSupportPattern(entry.Name)

                SearchRegion
                    .findAll(pattern)
                    .asStream()
                    .map { FoundCE(it.Region, isLimitBroken(it.Region)) }
                    .filter { !entry.PreferMlb || it.mlb }
            }
            .toList()
            .sorted()

    private fun findSupportBounds(Support: Region) =
        Game.supportRegionToolSearchRegion
            .findAll(
                images.supportRegionTool,
                supportRegionToolSimilarity
            )
            .map {
                Game.supportDefaultBounds
                    .copy(Y = it.Region.Y - 70)
            }
            .firstOrNull { Support in it }
            ?: Game.supportDefaultBounds.also {
                Timber.debug { "Default Region being returned" }
            }

    private fun isFriend(Region: Region): Boolean {
        val onlySelectFriends = autoSkillPrefs.friendsOnly
                || autoSkillPrefs.selectionMode == SupportSelectionModeEnum.Friend

        if (!onlySelectFriends)
            return true

        return sequenceOf(
            images.friend,
            images.guest,
            images.follow
        ).any { it in Region }
    }

    private fun isStarPresent(region: Region): Boolean {
        val mlbSimilarity = prefs.support.mlbSimilarity
        return region.exists(images.limitBroken, Similarity = mlbSimilarity)
    }

    private fun isMaxAscended(servant: Region): Boolean {
        val maxAscendedRegion = Game.supportMaxAscendedRegion
            .copy(Y = servant.Y)

        return isStarPresent(maxAscendedRegion)
    }

    private fun isLimitBroken(CraftEssence: Region): Boolean {
        val limitBreakRegion = Game.supportLimitBreakRegion
            .copy(Y = CraftEssence.Y)

        return isStarPresent(limitBreakRegion)
    }

    private fun checkMaxedSkills(bounds: Region, needMaxedSkills: List<Boolean>): Boolean {
        val y = bounds.Y + 325
        val x = bounds.X + 1627

        val skillLoc = listOf(
            Location(x, y),
            Location(x + 156, y),
            Location(x + 310, y)
        )

        val result = skillLoc
            .zip(needMaxedSkills)
            .map { (location, shouldBeMaxed) ->
                if (!shouldBeMaxed)
                    true
                else {
                    val skillRegion = Region(location, Size(35, 45))
                    skillRegion.exists(images.skillTen, Similarity = 0.68)
                }
            }

        Timber.debug {
            // Detected skill levels as string for debugging
            result
                .zip(needMaxedSkills)
                .joinToString("/") { (success, shouldBeMaxed) ->
                    when {
                        !shouldBeMaxed -> "x"
                        success -> "10"
                        else -> "f"
                    }
                }
        }

        return result.all { it }
    }
}
