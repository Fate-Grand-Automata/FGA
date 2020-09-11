package com.mathewsachin.fategrandautomata.scripts.modules

import com.mathewsachin.fategrandautomata.scripts.IFgoAutomataApi
import com.mathewsachin.fategrandautomata.scripts.enums.SupportClass
import com.mathewsachin.fategrandautomata.scripts.enums.SupportSelectionModeEnum
import com.mathewsachin.fategrandautomata.scripts.models.SearchFunctionResult
import com.mathewsachin.fategrandautomata.scripts.models.SearchVisibleResult
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences
import com.mathewsachin.libautomata.*
import mu.KotlinLogging
import java.util.*
import kotlin.time.TimeMark
import kotlin.time.TimeSource
import kotlin.time.seconds

private data class PreferredCEEntry(val Name: String, val PreferMlb: Boolean)

private typealias SearchFunction = () -> SearchFunctionResult

const val supportRegionToolSimilarity = 0.75

const val limitBrokenCharacter = '*'

private val logger = KotlinLogging.logger {}

class Support(fgAutomataApi: IFgoAutomataApi) : IFgoAutomataApi by fgAutomataApi {
    private var preferredServantArray = listOf<String>()
    private var friendNameArray = listOf<String>()
    private var preferredCEArray = listOf<PreferredCEEntry>()

    private fun String.process(): List<String> {
        return this
            .split(',')
            .map { it.trim() }
            .filter { it.isNotBlank() && it.toLowerCase(Locale.US) != "any" }
    }

    private val autoSkillPrefs get() = prefs.selectedAutoSkillConfig.support

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

    fun selectSupport(SelectionMode: SupportSelectionModeEnum): Boolean {
        waitForSupportScreenToLoad()

        if (autoSkillPrefs.supportClass != SupportClass.None) {
            autoSkillPrefs.supportClass.clickLocation.click()

            0.3.seconds.wait()
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
        screenshotManager.useSameSnapIn(fun(): SearchVisibleResult {
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

            when {
                result is SearchVisibleResult.Found -> {
                    result.support.click()
                    return true
                }
                result is SearchVisibleResult.NotFound
                        && numberOfSwipes < prefs.support.swipesPerUpdate -> {
                    scrollList()
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
                    return selectSupport(autoSkillPrefs.fallbackTo)
                }
            }
        }
    }

    private fun searchServantAndCE(): SearchFunctionResult {
        val servants = findServants()

        for (servant in servants) {
            if (servant is SearchFunctionResult.Found) {
                val supportBounds = when (servant) {
                    is SearchFunctionResult.FoundWithBounds -> servant.Bounds
                    else -> findSupportBounds(servant.Support)
                }

                val ceBounds = Game.supportDefaultCeBounds + Location(0, supportBounds.Y)
                val craftEssence = findCraftEssence(ceBounds)

                if (craftEssence is SearchFunctionResult.Found) {
                    // only return if found. if not, try the other servants before scrolling
                    return SearchFunctionResult.FoundWithBounds(
                        craftEssence.Support,
                        supportBounds
                    )
                }
            }
        }

        // not found, continue scrolling
        return SearchFunctionResult.NotFound
    }

    private fun decideSearchMethod(): SearchFunction {
        val hasServants = preferredServantArray.isNotEmpty()
        val hasCraftEssences = preferredCEArray.isNotEmpty()

        return when {
            hasServants && hasCraftEssences -> { -> searchServantAndCE() }
            hasServants -> { -> findServants().firstOrNull() ?: SearchFunctionResult.NotFound }
            hasCraftEssences -> { -> findCraftEssence(Game.supportListRegion) }
            else -> throw ScriptExitException(messages.supportSelectionPreferredNotSet)
        }
    }

    /**
     * Scroll support list considering [IPreferences.supportSwipeMultiplier].
     */
    private fun scrollList() {
        val endY = lerp(
            Game.supportSwipeStartClick.Y,
            Game.supportSwipeEndClick.Y,
            prefs.support.supportSwipeMultiplier
        )

        swipe(
            Game.supportSwipeStartClick,
            Game.supportSwipeEndClick.copy(Y = endY)
        )
    }

    /**
     * linear interpolation
     */
    private fun lerp(start: Int, end: Int, fraction: Double) =
        (start + (end - start) * fraction).toInt()

    private fun findFriendName(): SearchFunctionResult {
        for (friendName in friendNameArray) {
            // Cached pattern. Don't dispose here.
            val pattern =
                images.loadSupportPattern(
                    friendName
                )

            for (theFriend in Game.supportFriendsRegion.findAll(pattern)) {
                return SearchFunctionResult.Found(theFriend.Region)
            }
        }

        return SearchFunctionResult.NotFound
    }

    private fun findServants(): Sequence<SearchFunctionResult> = sequence {
        for (preferredServant in preferredServantArray) {
            // Cached pattern. Don't dispose here.
            val pattern = images.loadSupportPattern(
                preferredServant
            )

            cropFriendLock(pattern).use {
                for (servant in Game.supportListRegion.findAll(it)) {
                    if (autoSkillPrefs.maxAscended && !isMaxAscended(servant.Region)) {
                        continue
                    }

                    val needMaxedSkills = listOf(
                        autoSkillPrefs.skill1Max,
                        autoSkillPrefs.skill2Max,
                        autoSkillPrefs.skill3Max
                    )
                    val skillCheckNeeded = needMaxedSkills.any()

                    val bounds =
                        if (skillCheckNeeded) findSupportBounds(servant.Region)
                        else null

                    if (bounds != null && !checkMaxedSkills(bounds, needMaxedSkills)) {
                        continue
                    }

                    val result = if (bounds != null)
                        SearchFunctionResult.FoundWithBounds(servant.Region, bounds)
                    else SearchFunctionResult.Found(servant.Region)

                    yield(result)
                }
            }
        }
    }

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

    private fun findCraftEssence(SearchRegion: Region): SearchFunctionResult {
        for (preferredCraftEssence in preferredCEArray) {
            // Cached pattern. Don't dispose here.
            val pattern = images.loadSupportPattern(
                preferredCraftEssence.Name
            )

            val craftEssences = SearchRegion.findAll(pattern)

            for (craftEssence in craftEssences.map { it.Region }) {
                if (!preferredCraftEssence.PreferMlb || isLimitBroken(craftEssence)) {
                    return SearchFunctionResult.Found(craftEssence)
                }
            }
        }

        return SearchFunctionResult.NotFound
    }

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
                logger.debug("Default Region being returned")
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

        logger.debug {
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
