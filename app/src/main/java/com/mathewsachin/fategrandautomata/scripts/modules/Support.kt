package com.mathewsachin.fategrandautomata.scripts.modules

import com.mathewsachin.fategrandautomata.scripts.ImageLocator
import com.mathewsachin.fategrandautomata.scripts.enums.SupportSearchResultEnum
import com.mathewsachin.fategrandautomata.scripts.enums.SupportSelectionModeEnum
import com.mathewsachin.fategrandautomata.scripts.loadSupportImagePattern
import com.mathewsachin.fategrandautomata.scripts.prefs.Preferences
import com.mathewsachin.libautomata.*
import kotlin.time.seconds

private data class PreferredCEEntry(val Name: String, val PreferMlb: Boolean)

private data class SearchVisibleResult(val Result: SupportSearchResultEnum, val Support: Region?)

private data class SearchFunctionResult(val Support: Region?, val Bounds: Region?)

private typealias SearchFunction = () -> SearchFunctionResult

const val supportRegionToolSimilarity = 0.75

const val limitBrokenCharacter = '*'

class Support {
    private val preferredServantArray = mutableListOf<String>()
    private val friendNameArray = mutableListOf<String>()

    private val preferredCEArray = mutableListOf<PreferredCEEntry>()

    private fun String.process(): Sequence<String> {
        return this
            .splitToSequence(',')
            .map { it.trim() }
            .filter { it.isNotBlank() && it.toLowerCase() != "any" }
    }

    fun init() {
        preferredServantArray.clear()
        friendNameArray.clear()
        preferredCEArray.clear()

        // Friend names
        for (friend in Preferences.Support.friendNames.process()) {
            friendNameArray.add(friend)
        }

        // Servants
        for (servant in Preferences.Support.preferredServants.process()) {
            preferredServantArray.add(servant)
        }

        val ceEntries = Preferences.Support.preferredCEs
            .process()
            .map {
                val mlb = it.startsWith(limitBrokenCharacter)
                var name = it

                if (mlb) {
                    name = name.substring(1)
                }

                PreferredCEEntry(name, mlb)
            }

        // Craft essences
        for (craftEssence in ceEntries) {
            preferredCEArray.add(craftEssence)
        }
    }

    fun selectSupport(SelectionMode: SupportSelectionModeEnum): Boolean {
        val pattern = ImageLocator.SupportScreen
        while (!Game.SupportScreenRegion.exists(pattern)) {
        }

        return when (SelectionMode) {
            SupportSelectionModeEnum.First -> selectFirst()
            SupportSelectionModeEnum.Manual -> selectManual()
            SupportSelectionModeEnum.Friend -> selectFriend()
            SupportSelectionModeEnum.Preferred -> {
                val searchMethod = decideSearchMethod()
                selectPreferred(searchMethod)
            }

            else -> throw ScriptExitException("Invalid support selection mode")
        }
    }

    private fun selectManual(): Boolean {
        throw ScriptExitException("Support selection set to Manual")
    }

    private fun selectFirst(): Boolean {
        1.seconds.wait()
        Game.SupportFirstSupportClick.click()

        val pattern = ImageLocator.SupportScreen

        // https://github.com/29988122/Fate-Grand-Order_Lua/issues/192 , band-aid fix but it's working well.
        if (Game.SupportScreenRegion.exists(pattern)) {
            2.seconds.wait()

            while (Game.SupportScreenRegion.exists(pattern)) {
                10.seconds.wait()
                Game.SupportUpdateClick.click()
                1.seconds.wait()
                Game.SupportUpdateYesClick.click()
                3.seconds.wait()
                Game.SupportFirstSupportClick.click()
                1.seconds.wait()
            }
        }

        return true
    }

    private fun searchVisible(SearchMethod: SearchFunction): SearchVisibleResult {
        return ScreenshotManager.useSameSnapIn(fun(): SearchVisibleResult {
            if (!isFriend(Game.SupportFriendRegion)) {
                // no friends on screen, so there's no point in scrolling anymore
                return SearchVisibleResult(SupportSearchResultEnum.NoFriendsFound, null)
            }

            var (support, bounds) = SearchMethod()

            if (support == null) {
                // nope, not found this time. keep scrolling
                return SearchVisibleResult(SupportSearchResultEnum.NotFound, null)
            }

            // bounds are already returned by searchMethod.byServantAndCraftEssence, but not by the other methods
            bounds = bounds ?: findSupportBounds(support)

            if (!isFriend(bounds)) {
                // found something, but it doesn't belong to a friend. keep scrolling
                return SearchVisibleResult(SupportSearchResultEnum.NotFound, null)
            }

            return SearchVisibleResult(SupportSearchResultEnum.Found, support)
        })
    }

    private fun selectFriend(): Boolean {
        if (friendNameArray.size > 0) {
            return selectPreferred { SearchFunctionResult(findFriendName(), null) }
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
                && numberOfSwipes < Preferences.Support.swipesPerUpdate
            ) {
                scrollList()
                ++numberOfSwipes
                0.3.seconds.wait()
            } else if (numberOfUpdates < Preferences.Support.maxUpdates) {
                AutomataApi.PlatformImpl.toast("Support list will be updated in 3 seconds.")
                3.seconds.wait()

                Game.SupportUpdateClick.click()
                1.seconds.wait()
                Game.SupportUpdateYesClick.click()

                while (Game.needsToRetry()) {
                    Game.retry()
                }

                3.seconds.wait()

                ++numberOfUpdates
                numberOfSwipes = 0
            } else {
                // -- okay, we have run out of options, let's give up
                Game.SupportListTopClick.click()
                return selectSupport(Preferences.Support.fallbackTo)
            }
        }
    }

    private fun searchServantAndCE(): SearchFunctionResult {
        val servants = findServants()

        for (servant in servants) {
            val supportBounds = findSupportBounds(servant)
            val craftEssence = findCraftEssence(supportBounds)

            // CEs are always below Servants in the support list
            // see docs/support_list_edge_case_fix.png to understand why this conditional exists
            if (craftEssence != null && craftEssence.Y > servant.Y) {
                // only return if found. if not, try the other servants before scrolling
                return SearchFunctionResult(craftEssence, supportBounds)
            }
        }

        // not found, continue scrolling
        return SearchFunctionResult(null, null)
    }

    private fun decideSearchMethod(): SearchFunction {
        val hasServants = preferredServantArray.size > 0
        val hasCraftEssences = preferredCEArray.size > 0

        if (hasServants && hasCraftEssences) {
            return { searchServantAndCE() }
        }

        if (hasServants) {
            return { SearchFunctionResult(findServants().firstOrNull(), null) }
        }

        if (hasCraftEssences) {
            return { SearchFunctionResult(findCraftEssence(Game.SupportListRegion), null) }
        }

        throw ScriptExitException("When using 'preferred' support selection mode, specify at least one Servant or Craft Essence.")
    }

    private fun scrollList() {
        swipe(Game.SupportSwipeStartClick, Game.SupportSwipeEndClick)
    }

    private fun findFriendName(): Region? {
        for (friendName in friendNameArray) {
            // Cached pattern. Don't dispose here.
            val pattern = loadSupportImagePattern(friendName)

            for (theFriend in Game.SupportFriendsRegion.findAll(pattern)) {
                return theFriend.Region
            }
        }

        return null
    }

    private fun findServants(): Sequence<Region> = sequence {
        for (preferredServant in preferredServantArray) {
            // Cached pattern. Don't dispose here.
            val pattern = loadSupportImagePattern(preferredServant)

            for (servant in Game.SupportListRegion.findAll(pattern)) {
                yield(servant.Region)
            }
        }
    }

    private fun findCraftEssence(SearchRegion: Region): Region? {
        for (preferredCraftEssence in preferredCEArray) {
            // Cached pattern. Don't dispose here.
            val pattern = loadSupportImagePattern(preferredCraftEssence.Name)

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
        val regionAnchor = ImageLocator.SupportRegionTool

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
        val friendPattern = ImageLocator.Friend

        return !Preferences.Support.friendsOnly
                || Region.exists(friendPattern)
    }

    private fun isLimitBroken(CraftEssence: Region): Boolean {
        val limitBreakRegion = Game.SupportLimitBreakRegion
            .copy(Y = CraftEssence.Y)

        val limitBreakPattern = ImageLocator.LimitBroken

        return limitBreakRegion.exists(limitBreakPattern, Similarity = 0.8)
    }
}