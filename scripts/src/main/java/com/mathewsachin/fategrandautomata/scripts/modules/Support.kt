package com.mathewsachin.fategrandautomata.scripts.modules

import com.mathewsachin.fategrandautomata.scripts.IFGAutomataApi
import com.mathewsachin.fategrandautomata.scripts.enums.SupportSearchResultEnum
import com.mathewsachin.fategrandautomata.scripts.enums.SupportSelectionModeEnum
import com.mathewsachin.libautomata.*
import kotlin.time.seconds

private data class PreferredCEEntry(val Name: String, val PreferMlb: Boolean)

private data class SearchVisibleResult(val Result: SupportSearchResultEnum, val Support: Region?)

private data class SearchFunctionResult(val Support: Region?, val Bounds: Region?)

private typealias SearchFunction = () -> SearchFunctionResult

const val supportRegionToolSimilarity = 0.75

const val limitBrokenCharacter = '*'

class Support(fgAutomataApi: IFGAutomataApi) : IFGAutomataApi by fgAutomataApi {
    private val preferredServantArray = mutableListOf<String>()
    private val friendNameArray = mutableListOf<String>()

    private val preferredCEArray = mutableListOf<PreferredCEEntry>()

    private fun String.process(): Sequence<String> {
        return this
            .splitToSequence(',')
            .map { it.trim() }
            .filter { it.isNotBlank() && it.toLowerCase() != "any" }
    }

    private val autoSkillPrefs = prefs.selectedAutoSkillConfig.support

    fun init() {
        preferredServantArray.clear()
        friendNameArray.clear()
        preferredCEArray.clear()

        // Friend names
        for (friend in autoSkillPrefs.friendNames.process()) {
            friendNameArray.add(friend)
        }

        // Servants
        for (servant in autoSkillPrefs.preferredServants.process()) {
            preferredServantArray.add(servant)
        }

        val ceEntries = autoSkillPrefs.preferredCEs
            .process()
            .map {
                val mlb = it.startsWith(limitBrokenCharacter)
                var name = it

                if (mlb) {
                    name = name.substring(1)
                }

                PreferredCEEntry(
                    name,
                    mlb
                )
            }

        // Craft essences
        for (craftEssence in ceEntries) {
            preferredCEArray.add(craftEssence)
        }
    }

    fun selectSupport(SelectionMode: SupportSelectionModeEnum): Boolean {
        val pattern = images.supportScreen
        while (!game.supportScreenRegion.exists(pattern)) {
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
        throw ScriptExitException("Support selection set to Manual")
    }

    private fun selectFirst(): Boolean {
        1.seconds.wait()
        game.supportFirstSupportClick.click()

        val pattern = images.supportScreen

        // https://github.com/29988122/Fate-Grand-Order_Lua/issues/192 , band-aid fix but it's working well.
        if (game.supportScreenRegion.exists(pattern)) {
            2.seconds.wait()

            while (game.supportScreenRegion.exists(pattern)) {
                10.seconds.wait()
                game.supportUpdateClick.click()
                1.seconds.wait()
                game.supportUpdateYesClick.click()
                3.seconds.wait()
                game.supportFirstSupportClick.click()
                1.seconds.wait()
            }
        }

        return true
    }

    private fun searchVisible(SearchMethod: SearchFunction): SearchVisibleResult {
        return screenshotManager.useSameSnapIn(fun(): SearchVisibleResult {
            if (!isFriend(game.supportFriendRegion)) {
                // no friends on screen, so there's no point in scrolling anymore
                return SearchVisibleResult(
                    SupportSearchResultEnum.NoFriendsFound,
                    null
                )
            }

            var (support, bounds) = SearchMethod()

            if (support == null) {
                // nope, not found this time. keep scrolling
                return SearchVisibleResult(
                    SupportSearchResultEnum.NotFound,
                    null
                )
            }

            // bounds are already returned by searchMethod.byServantAndCraftEssence, but not by the other methods
            bounds = bounds ?: findSupportBounds(support)

            if (!isFriend(bounds)) {
                // found something, but it doesn't belong to a friend. keep scrolling
                return SearchVisibleResult(
                    SupportSearchResultEnum.NotFound,
                    null
                )
            }

            return SearchVisibleResult(
                SupportSearchResultEnum.Found,
                support
            )
        })
    }

    private fun selectFriend(): Boolean {
        if (friendNameArray.size > 0) {
            return selectPreferred {
                SearchFunctionResult(
                    findFriendName(),
                    null
                )
            }
        }

        throw ScriptExitException("When using 'friend' support selection mode, specify at least one friend name.")
    }

    private fun selectPreferred(SearchMethod: SearchFunction): Boolean {
        var numberOfSwipes = 0
        var numberOfUpdates = 0

        while (true) {
            val (result, support) = searchVisible(SearchMethod)

            if (result == SupportSearchResultEnum.Found) {
                support?.click()
                return true
            }

            if (result == SupportSearchResultEnum.NotFound
                && numberOfSwipes < prefs.support.swipesPerUpdate
            ) {
                scrollList()
                ++numberOfSwipes
                0.3.seconds.wait()
            } else if (numberOfUpdates < prefs.support.maxUpdates) {
                toast("Support list will be updated in 3 seconds.")
                3.seconds.wait()

                game.supportUpdateClick.click()
                1.seconds.wait()
                game.supportUpdateYesClick.click()

                while (needsToRetry()) {
                    retry()
                }

                3.seconds.wait()

                ++numberOfUpdates
                numberOfSwipes = 0
            } else {
                // -- okay, we have run out of options, let's give up
                game.supportListTopClick.click()
                return selectSupport(autoSkillPrefs.fallbackTo)
            }
        }
    }

    private fun searchServantAndCE(): SearchFunctionResult {
        val servants = findServants()

        for (servant in servants) {
            if (servant.Support == null)
                continue

            val supportBounds = servant.Bounds
                ?: findSupportBounds(servant.Support)

            val craftEssence = findCraftEssence(supportBounds)

            // CEs are always below Servants in the support list
            // see docs/support_list_edge_case_fix.png to understand why this conditional exists
            if (craftEssence != null && craftEssence.Y > servant.Support.Y) {
                // only return if found. if not, try the other servants before scrolling
                return SearchFunctionResult(
                    craftEssence,
                    supportBounds
                )
            }
        }

        // not found, continue scrolling
        return SearchFunctionResult(
            null,
            null
        )
    }

    private fun decideSearchMethod(): SearchFunction {
        val hasServants = preferredServantArray.size > 0
        val hasCraftEssences = preferredCEArray.size > 0

        if (hasServants && hasCraftEssences) {
            return { searchServantAndCE() }
        }

        if (hasServants) {
            return {
                findServants().firstOrNull()
                    ?: SearchFunctionResult(null, null)
            }
        }

        if (hasCraftEssences) {
            return {
                SearchFunctionResult(
                    findCraftEssence(game.supportListRegion),
                    null
                )
            }
        }

        throw ScriptExitException("When using 'preferred' support selection mode, specify at least one Servant or Craft Essence.")
    }

    /**
     * Scroll support list considering [Preferences.supportSwipeMultiplier].
     */
    private fun scrollList() {
        val endY = lerp(
            game.supportSwipeStartClick.Y,
            game.supportSwipeEndClick.Y,
            prefs.support.supportSwipeMultiplier
        )

        swipe(
            game.supportSwipeStartClick,
            game.supportSwipeEndClick.copy(Y = endY)
        )
    }

    /**
     * linear interpolation
     */
    private fun lerp(start: Int, end: Int, fraction: Double) =
        (start + (end - start) * fraction).toInt()

    private fun findFriendName(): Region? {
        for (friendName in friendNameArray) {
            // Cached pattern. Don't dispose here.
            val pattern =
                images.loadSupportPattern(
                    friendName
                )

            for (theFriend in game.supportFriendsRegion.findAll(pattern)) {
                return theFriend.Region
            }
        }

        return null
    }

    private fun findServants(): Sequence<SearchFunctionResult> = sequence {
        for (preferredServant in preferredServantArray) {
            // Cached pattern. Don't dispose here.
            val pattern =
                images.loadSupportPattern(
                    preferredServant
                )

            cropFriendLock(pattern).use {
                for (servant in game.supportListRegion.findAll(it)) {
                    val skillLevels = listOf(
                        prefs.selectedAutoSkillConfig.skill1Max,
                        prefs.selectedAutoSkillConfig.skill2Max,
                        prefs.selectedAutoSkillConfig.skill3Max
                    )
                    val skillCheckNeeded = skillLevels.any()

                    val bounds =
                        if (skillCheckNeeded) findSupportBounds(servant.Region)
                        else null

                    if (bounds != null && !skillLevels(bounds, skillLevels)) {
                        continue
                    }

                    yield(SearchFunctionResult(servant.Region, bounds))
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

    private fun findCraftEssence(SearchRegion: Region): Region? {
        for (preferredCraftEssence in preferredCEArray) {
            // Cached pattern. Don't dispose here.
            val pattern =
                images.loadSupportPattern(
                    preferredCraftEssence.Name
                )

            val craftEssences = SearchRegion.findAll(pattern)

            for (craftEssence in craftEssences.map { it.Region }) {
                if (!preferredCraftEssence.PreferMlb || isLimitBroken(craftEssence)) {
                    return craftEssence
                }
            }
        }

        return null
    }

    private fun findSupportBounds(Support: Region): Region {
        var supportBound = Region(76, 0, 2356, 428)
        val regionAnchor = images.supportRegionTool

        val searchRegion = Region(2100, 0, 300, 1440)
        val regionArray =
            searchRegion.findAll(regionAnchor, Similarity = supportRegionToolSimilarity)

        val defaultRegion = supportBound

        for (testRegion in regionArray) {
            supportBound = supportBound.copy(Y = testRegion.Region.Y - 70)

            if (supportBound.contains(Support)) {
                return supportBound
            }
        }

        // AutomataApi.Toast("Default Region being returned; file an issue on the github for this issue");
        return defaultRegion
    }

    private fun isFriend(Region: Region): Boolean {
        val friendPattern = images.friend

        return !autoSkillPrefs.friendsOnly
                || Region.exists(friendPattern)
    }

    private fun isLimitBroken(CraftEssence: Region): Boolean {
        val limitBreakRegion = game.supportLimitBreakRegion
            .copy(Y = CraftEssence.Y)

        val limitBreakPattern = images.limitBroken

        val mlbSimilarity = prefs.support.mlbSimilarity
        return limitBreakRegion.exists(limitBreakPattern, Similarity = mlbSimilarity)
    }

    private fun skillLevels(bounds: Region, skillLevels: List<Boolean>): Boolean {
        val y = bounds.Y + 325
        val x = bounds.X + 1600

        val skillLoc = listOf(
            Location(x + 2 + 25, y),
            Location(x + 158 + 25, y),
            Location(x + 312 + 25, y)
        )

        val result = skillLoc.withIndex().map {
            if (!skillLevels[it.index])
                true
            else {
                val skillRegion = Region(it.value, Size(35, 45))
                skillRegion.exists(images.skillTen, Similarity = 0.68)
            }
        }

        val skillString = result.withIndex().joinToString("/") {
            val maxReq = skillLevels[it.index]
            when {
                !maxReq -> "x"
                it.value -> "10"
                else -> "f"
            }
        }
        println(skillString)

        return result.all { it }
    }
}
