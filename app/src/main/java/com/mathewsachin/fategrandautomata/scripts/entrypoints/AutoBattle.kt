package com.mathewsachin.fategrandautomata.scripts.entrypoints

import com.mathewsachin.fategrandautomata.core.*
import com.mathewsachin.fategrandautomata.scripts.ImageLocator
import com.mathewsachin.fategrandautomata.scripts.enums.GameServerEnum
import com.mathewsachin.fategrandautomata.scripts.enums.RefillResourceEnum
import com.mathewsachin.fategrandautomata.scripts.modules.*
import com.mathewsachin.fategrandautomata.scripts.prefs.Preferences
import kotlin.time.seconds

/**
 * Checks if Support Selection menu is up
 */
fun isInSupport(): Boolean {
    return Game.SupportScreenRegion.exists(ImageLocator.SupportScreen, Similarity = 0.85)
}

/**
 * Script for starting quests, selecting the support and doing battles.
 */
class AutoBattle : EntryPoint() {
    private val support = Support()
    private val card = Card()
    private val battle = Battle()
    private val autoSkill = AutoSkill()

    private var stonesUsed = 0
    private var isContinuing = false

    /**
     * Refills the AP with apples depending on [Preferences.Refill].
     */
    private fun refillStamina() {
        val refillPrefs = Preferences.Refill

        if (refillPrefs.enabled && stonesUsed < refillPrefs.repetitions) {
            when (refillPrefs.resource) {
                RefillResourceEnum.SQ -> Game.StaminaSqClick.click()
                RefillResourceEnum.AllApples -> {
                    Game.StaminaBronzeClick.click()
                    Game.StaminaSilverClick.click()
                    Game.StaminaGoldClick.click()
                }
                RefillResourceEnum.Gold -> Game.StaminaGoldClick.click()
                RefillResourceEnum.Silver -> Game.StaminaSilverClick.click()
                RefillResourceEnum.Bronze -> Game.StaminaBronzeClick.click()
            }

            AutomataApi.wait(1.seconds)
            Game.StaminaOkClick.click()
            ++stonesUsed

            AutomataApi.wait(3.seconds)
        } else throw ScriptExitException("AP ran out!")
    }

    /**
     * Checks if the window for withdrawing from the battle exists.
     */
    private fun needsToWithdraw(): Boolean {
        return Game.WithdrawRegion.exists(ImageLocator.Withdraw)
    }

    /**
     * Handles withdrawing from battle. Depending on [Preferences.WithdrawEnabled], the script either
     * withdraws automatically or stops completely.
     */
    private fun withdraw() {
        if (!Preferences.WithdrawEnabled) {
            throw ScriptExitException("All servants have been defeated and auto-withdrawing is disabled.")
        }

        Game.WithdrawRegion.click()

        AutomataApi.wait(0.5.seconds)

        // Click the "Accept" button after choosing to withdraw
        Game.WithdrawAcceptClick.click()

        AutomataApi.wait(1.seconds)

        // Click the "Close" button after accepting the withdrawal
        Game.StaminaBronzeClick.click()
    }

    /**
     * Checks if the SKIP button exists on the screen.
     */
    private fun needsToStorySkip(): Boolean {
        // TODO: Story Skip doesn't work correctly
        //if (Game.MenuStorySkipRegion.exists(ImageLocator.StorySkip))
        return Preferences.StorySkip
    }

    /**
     * Clicks on the button to start the quest in the Party selection, then selects the boost item
     * if applicable and then skips the story if story skip is activated.
     */
    private fun startQuest() {
        Game.MenuStartQuestClick.click()

        AutomataApi.wait(2.seconds)

        val boostItem = Preferences.BoostItemSelectionMode
        if (boostItem >= 0) {
            Game.MenuBoostItemClickArray[boostItem].click()

            // in case you run out of items
            Game.MenuBoostItemSkipClick.click()
        }

        if (Preferences.StorySkip) {
            AutomataApi.wait(10.seconds)

            if (needsToStorySkip()) {
                Game.MenuStorySkipClick.click()
                AutomataApi.wait(0.5.seconds)
                Game.MenuStorySkipYesClick.click()
            }
        }
    }

    /**
     *  Checks if in menu.png is on the screen, indicating that a quest can be chosen.
     */
    private fun isInMenu(): Boolean {
        return Game.MenuScreenRegion.exists(ImageLocator.Menu)
    }

    /**
     * Resets the battle state, clicks on the quest and refills the AP if needed.
     */
    private fun menu() {
        battle.resetState()

        if (Preferences.Refill.enabled) {
            val refillRepetitions = Preferences.Refill.repetitions
            if (refillRepetitions > 0) {
                AutomataApi.toast("$stonesUsed refills used out of $refillRepetitions")
            }
        }

        // Click uppermost quest
        Game.MenuSelectQuestClick.click()
        AutomataApi.wait(1.5.seconds)

        // Auto refill
        while (Game.StaminaScreenRegion.exists(ImageLocator.Stamina)) {
            refillStamina()
        }
    }

    /**
     * Checks if the Quest Completed screen is up. This can be one of many screens:
     * - Bond point distribution
     * - Bond level up
     * - Master EXP gains
     * - Dropped materials
     * - Master Level or Mystic Codes level ups
     *
     * All screens need to be included in case of getting stuck in one of them because of lags or
     * too few clicks.
     */
    private fun isInResult(): Boolean {
        val resultScreenItems = mapOf(
            Game.ResultScreenRegion to ImageLocator.Result,
            Game.ResultBondRegion to ImageLocator.Bond,
            Game.ResultMasterExpRegion to ImageLocator.MasterExp,
            Game.ResultMatRewardsRegion to ImageLocator.MatRewards,
            Game.ResultMasterLvlUpRegion to ImageLocator.MasterLvlUp
        )

        return resultScreenItems.any { (Region, Pattern) ->
            Region.exists(Pattern)
        }
    }

    /**
     * Clicks through the reward screen, continue if the option presents itself, otherwise continue
     * clicking through the rest of the screens until the quest selection screen is reached.
     */
    private fun result() {
        // Validator document https://github.com/29988122/Fate-Grand-Order_Lua/wiki/In-Game-Result-Screen-Flow for detail.
        Game.ResultNextClick.click(55)

        // Checking if there was a Bond CE reward
        if (Game.ResultCeRewardRegion.exists(ImageLocator.Bond10Reward)) {
            if (Preferences.StopAfterBond10) {
                throw ScriptExitException("Bond 10 CE GET!")
            }

            Game.ResultCeRewardCloseClick.click()

            // Still need to proceed through reward screen.
            Game.ResultNextClick.click(35)
        }

        AutomataApi.wait(5.seconds)

        // Friend request dialogue. Appears when non-friend support was selected this battle. Ofc it's defaulted not sending request.
        if (Game.ResultFriendRequestRegion.exists(ImageLocator.FriendRequest)) {
            Game.ResultFriendRequestRejectClick.click()
        }

        AutomataApi.wait(1.seconds)

        // Only for JP currently. Searches for the Continue option after select Free Quests
        if (Preferences.GameServer == GameServerEnum.Jp && Game.ContinueRegion.exists(
                ImageLocator.Confirm
            )
        ) {
            // Needed to show we don't need to enter the "StartQuest" function
            isContinuing = true

            // Pressing Continue option after completing a quest, reseting the state as would occur in "Menu" function
            Game.ContinueClick.click()
            battle.resetState()

            AutomataApi.wait(1.5.seconds)

            // If Stamina is empty, follow same protocol as is in "Menu" function Auto refill.
            while (Game.StaminaScreenRegion.exists(ImageLocator.Stamina)) {
                refillStamina()
            }

            return
        }

        // Post-battle story is sometimes there.
        if (Preferences.StorySkip) {
            if (Game.MenuStorySkipRegion.exists(ImageLocator.StorySkip)) {
                Game.MenuStorySkipClick.click()
                AutomataApi.wait(0.5.seconds)
                Game.MenuStorySkipYesClick.click()
            }
        }

        AutomataApi.wait(10.seconds)

        // Quest Completion reward. Exits the screen when it is presented.
        if (Game.ResultCeRewardRegion.exists(ImageLocator.Bond10Reward)) {
            Game.ResultCeRewardCloseClick.click()
            AutomataApi.wait(1.seconds)
            Game.ResultCeRewardCloseClick.click()
        }

        AutomataApi.wait(5.seconds)

        // 1st time quest reward screen, eg. Mana Prisms, Event CE, Materials, etc.
        if (Game.ResultQuestRewardRegion.exists(ImageLocator.QuestReward)) {
            AutomataApi.wait(1.seconds)
            Game.ResultNextClick.click()
        }
    }

    // Selections Support option
    private fun support() {
        // Friend selection
        val hasSelectedSupport = support.selectSupport(Preferences.Support.selectionMode)

        if (hasSelectedSupport && !isContinuing) {
            AutomataApi.wait(4.seconds)
            startQuest()

            // Wait timer till battle starts.
            // Uses less battery to wait than to search for images for a few seconds.
            // Adjust according to device.
            AutomataApi.wait(10.seconds)
        }
    }

    // Initialize Aspect Ratio adjustment for different sized screens,ask for input from user for Autoskill plus confirming Apple/Stone usage
    // Then initialize the AutoSkill, Battle, and Card modules in modules.
    private fun init() {
        initScaling()

        autoSkill.init(battle, card)
        battle.init(autoSkill, card)
        card.init(autoSkill, battle)

        support.init()
    }

    override fun script(): Nothing {
        init()

        // a map of validators and associated actions
        // if the validator function evaluates to true, the associated action function is called
        val screens = mapOf(
            { Game.needsToRetry() } to { Game.retry() },
            { battle.isIdle() } to { battle.performBattle() },
            { isInMenu() } to { menu() },
            { isInResult() } to { result() },
            { isInSupport() } to { support() },
            { needsToWithdraw() } to { withdraw() },
            { isGudaFinalRewardsScreen() } to { gudaFinalReward() }
        )

        // Loop through SCREENS until a Validator returns true
        while (true) {
            val actor = AutomataApi.useSameSnapIn {
                screens
                    .filter { (validator, _) -> validator() }
                    .map { (_, actor) -> actor }
                    .firstOrNull()
            }

            actor?.invoke()

            AutomataApi.wait(1.seconds)
        }
    }

    /**
     * Special result screen check for GudaGuda Final Honnouji.
     *
     * The check only runs if `GudaFinal` is activated in the preferences and if the GameServer is
     * set to Japanese.
     *
     * When this event comes to other regions, the GameServer condition needs to be extended.
     */
    private fun isGudaFinalRewardsScreen(): Boolean {
        if (!Preferences.GudaFinal || Preferences.GameServer != GameServerEnum.Jp)
            return false

        return Game.GudaFinalRewardsRegion.exists(ImageLocator.GudaFinalRewards)
    }

    /**
     * Clicks on the Close button for the special GudaGuda Final Honnouji reward window if it was
     * detected.
     */
    private fun gudaFinalReward() = Game.GudaFinalRewardsRegion.click()
}