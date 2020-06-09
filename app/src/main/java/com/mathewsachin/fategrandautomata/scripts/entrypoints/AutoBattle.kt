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
open class AutoBattle : EntryPoint() {
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

            1.seconds.wait()
            Game.StaminaOkClick.click()
            ++stonesUsed

            3.seconds.wait()
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

        // Withdraw Region can vary depending on if you have Command Spells/Quartz
        val withdrawRegion = Game.WithdrawRegion
            .findAll(ImageLocator.Withdraw)
            .firstOrNull() ?: return

        withdrawRegion.Region.click()

        0.5.seconds.wait()

        // Click the "Accept" button after choosing to withdraw
        Game.WithdrawAcceptClick.click()

        1.seconds.wait()

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

        2.seconds.wait()

        val boostItem = Preferences.BoostItemSelectionMode
        if (boostItem >= 0) {
            Game.MenuBoostItemClickArray[boostItem].click()

            // in case you run out of items
            Game.MenuBoostItemSkipClick.click()
        }

        if (Preferences.StorySkip) {
            10.seconds.wait()

            if (needsToStorySkip()) {
                Game.MenuStorySkipClick.click()
                0.5.seconds.wait()
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
                AutomataApi.PlatformImpl.toast("$stonesUsed refills used out of $refillRepetitions")
            }
        }

        // Click uppermost quest
        Game.MenuSelectQuestClick.click()

        afterSelectingQuest()
    }

    private fun afterSelectingQuest() {
        1.5.seconds.wait()

        // Inventory full. Stop script.
        when (Preferences.GameServer) {
            // We only have images for JP and NA
            GameServerEnum.En, GameServerEnum.Jp -> {
                if (Game.InventoryFullRegion.exists(ImageLocator.InventoryFull)) {
                    throw ScriptExitException("Inventory Full")
                }
            }
        }

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
        if (Game.ResultScreenRegion.exists(ImageLocator.Result)
            || Game.ResultBondRegion.exists(ImageLocator.Bond)
            // We're assuming CN and TW use the same Master/Mystic Code Level up image
            || Game.ResultMasterLvlUpRegion.exists(ImageLocator.MasterLvlUp)) {
            return true
        }

        val gameServer = Preferences.GameServer

        // We don't have TW images for these
        if (gameServer != GameServerEnum.Tw) {
            return Game.ResultMasterExpRegion.exists(ImageLocator.MasterExp)
                    || Game.ResultMatRewardsRegion.exists(ImageLocator.MatRewards)
        }

        // Not in any result screen
        return false
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

        5.seconds.wait()

        // Friend request dialogue. Appears when non-friend support was selected this battle. Ofc it's defaulted not sending request.
        if (Game.ResultFriendRequestRegion.exists(ImageLocator.FriendRequest)) {
            Game.ResultFriendRequestRejectClick.click()
        }

        1.seconds.wait()

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

            // If Stamina is empty, follow same protocol as is in "Menu" function Auto refill.
            afterSelectingQuest()

            return
        }

        // Post-battle story is sometimes there.
        if (Preferences.StorySkip) {
            if (Game.MenuStorySkipRegion.exists(ImageLocator.StorySkip)) {
                Game.MenuStorySkipClick.click()
                0.5.seconds.wait()
                Game.MenuStorySkipYesClick.click()
            }
        }

        10.seconds.wait()

        // Quest Completion reward. Exits the screen when it is presented.
        if (Game.ResultCeRewardRegion.exists(ImageLocator.Bond10Reward)) {
            Game.ResultCeRewardCloseClick.click()
            1.seconds.wait()
            Game.ResultCeRewardCloseClick.click()
        }

        5.seconds.wait()

        // 1st time quest reward screen, eg. Mana Prisms, Event CE, Materials, etc.
        if (Game.ResultQuestRewardRegion.exists(ImageLocator.QuestReward)) {
            1.seconds.wait()
            Game.ResultNextClick.click()
        }
    }

    // Selections Support option
    private fun support() {
        // Friend selection
        val hasSelectedSupport = support.selectSupport(Preferences.Support.selectionMode)

        if (hasSelectedSupport && !isContinuing) {
            4.seconds.wait()
            startQuest()

            // Wait timer till battle starts.
            // Uses less battery to wait than to search for images for a few seconds.
            // Adjust according to device.
            10.seconds.wait()
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
            val actor = ScreenshotManager.useSameSnapIn {
                screens
                    .filter { (validator, _) -> validator() }
                    .map { (_, actor) -> actor }
                    .firstOrNull()
            }

            actor?.invoke()

            1.seconds.wait()
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