package com.mathewsachin.fategrandautomata.scripts.entrypoints

import com.mathewsachin.fategrandautomata.scripts.IFGAutomataApi
import com.mathewsachin.fategrandautomata.scripts.enums.GameServerEnum
import com.mathewsachin.fategrandautomata.scripts.enums.RefillResourceEnum
import com.mathewsachin.fategrandautomata.scripts.modules.*
import com.mathewsachin.libautomata.EntryPoint
import com.mathewsachin.libautomata.ExitManager
import com.mathewsachin.libautomata.IPlatformImpl
import com.mathewsachin.libautomata.ScriptExitException
import kotlin.time.seconds

/**
 * Checks if Support Selection menu is up
 */
fun isInSupport(): Boolean {
    return Game.SupportScreenRegion.exists(images.supportScreen, Similarity = 0.85)
}

/**
 * Script for starting quests, selecting the support and doing battles.
 */
open class AutoBattle(
    exitManager: ExitManager,
    platformImpl: IPlatformImpl,
    fgAutomataApi: IFGAutomataApi
) : EntryPoint(exitManager, platformImpl), IFGAutomataApi by fgAutomataApi {
    private val support =
        Support()
    private val card = Card()
    private val battle =
        Battle()
    private val autoSkill =
        AutoSkill()

    private var stonesUsed = 0
    private var isContinuing = false
    private var partySelected = false

    override fun script(): Nothing {
        init()

        // a map of validators and associated actions
        // if the validator function evaluates to true, the associated action function is called
        val screens = mapOf(
            { Game.needsToRetry() } to { Game.retry() },
            { battle.isIdle() } to { battle.performBattle() },
            { isInMenu() } to { menu() },
            { isInResult() } to { result() },
            { isInQuestRewardScreen() } to { questReward() },
            { isInSupport() } to { support() },
            { needsToWithdraw() } to { withdraw() },
            { isGudaFinalRewardsScreen() } to { gudaFinalReward() }
        )

        // Loop through SCREENS until a Validator returns true
        while (true) {
            val actor = screenshotManager.useSameSnapIn {
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
     * Initialize Aspect Ratio adjustment for different sized screens,ask for input from user for
     * Autoskill plus confirming Apple/Stone usage.
     *
     * Then initialize the AutoSkill, Battle, and Card modules in modules.
     */
    private fun init() {
        initScaling()

        autoSkill.init(battle, card)
        battle.init(autoSkill, card)
        card.init(autoSkill, battle)

        support.init()
    }

    /**
     *  Checks if in menu.png is on the screen, indicating that a quest can be chosen.
     */
    private fun isInMenu(): Boolean {
        return Game.MenuScreenRegion.exists(images.menu)
    }

    /**
     * Resets the battle state, clicks on the quest and refills the AP if needed.
     */
    private fun menu() {
        battle.resetState()

        showRefillsUsedMessage()

        // Click uppermost quest
        Game.MenuSelectQuestClick.click()

        afterSelectingQuest()
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
        if (Game.ResultScreenRegion.exists(images.result)
            || Game.ResultBondRegion.exists(images.bond)
            // We're assuming CN and TW use the same Master/Mystic Code Level up image
            || Game.ResultMasterLvlUpRegion.exists(images.masterLvlUp)
        ) {
            return true
        }

        val gameServer = prefs.gameServer

        // We don't have TW images for these
        if (gameServer != GameServerEnum.Tw) {
            return Game.ResultMasterExpRegion.exists(images.masterExp)
                    || Game.ResultMatRewardsRegion.exists(images.matRewards)
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
        if (Game.ResultCeRewardRegion.exists(images.bond10Reward)) {
            if (prefs.stopAfterBond10) {
                throw ScriptExitException("Bond 10 CE GET!")
            }

            Game.ResultCeRewardCloseClick.click()

            // Still need to proceed through reward screen.
            Game.ResultNextClick.click(35)
        }

        5.seconds.wait()

        // Friend request dialogue. Appears when non-friend support was selected this battle. Ofc it's defaulted not sending request.
        if (Game.ResultFriendRequestRegion.exists(images.friendRequest)) {
            Game.ResultFriendRequestRejectClick.click()
        }

        1.seconds.wait()

        // Searches for the Continue option after select Free Quests
        when (prefs.gameServer) {
            // We only have images for JP and NA
            GameServerEnum.En, GameServerEnum.Jp -> {
                if (Game.ContinueRegion.exists(images.confirm)) {
                    // Needed to show we don't need to enter the "StartQuest" function
                    isContinuing = true

                    // Pressing Continue option after completing a quest, reseting the state as would occur in "Menu" function
                    Game.ContinueClick.click()
                    battle.resetState()

                    showRefillsUsedMessage()

                    // If Stamina is empty, follow same protocol as is in "Menu" function Auto refill.
                    afterSelectingQuest()

                    return
                }
            }
        }

        // Post-battle story is sometimes there.
        if (prefs.storySkip) {
            if (Game.MenuStorySkipRegion.exists(images.storySkip)) {
                Game.MenuStorySkipClick.click()
                0.5.seconds.wait()
                Game.MenuStorySkipYesClick.click()
            }
        }

        10.seconds.wait()

        // Quest Completion reward. Exits the screen when it is presented.
        if (Game.ResultCeRewardRegion.exists(images.bond10Reward)) {
            Game.ResultCeRewardCloseClick.click()
            1.seconds.wait()
            Game.ResultCeRewardCloseClick.click()
        }
    }

    /**
     * Checks if FGO is on the quest reward screen for Mana Prisms, SQ, ...
     */
    private fun isInQuestRewardScreen() =
        Game.ResultQuestRewardRegion.exists(images.questReward)

    /**
     * Handles the quest rewards screen.
     */
    private fun questReward() = Game.ResultNextClick.click()

    // Selections Support option
    private fun support() {
        // Friend selection
        val hasSelectedSupport =
            support.selectSupport(prefs.selectedAutoSkillConfig.support.selectionMode)

        if (hasSelectedSupport && !isContinuing) {
            4.seconds.wait()
            startQuest()

            // Wait timer till battle starts.
            // Uses less battery to wait than to search for images for a few seconds.
            // Adjust according to device.
            10.seconds.wait()
        }
    }

    /**
     * Checks if the window for withdrawing from the battle exists.
     */
    private fun needsToWithdraw() =
        Game.WithdrawRegion.exists(images.withdraw)

    /**
     * Handles withdrawing from battle. Depending on [prefs.withdrawEnabled], the script either
     * withdraws automatically or stops completely.
     */
    private fun withdraw() {
        if (!prefs.withdrawEnabled) {
            throw ScriptExitException("All servants have been defeated and auto-withdrawing is disabled.")
        }

        // Withdraw Region can vary depending on if you have Command Spells/Quartz
        val withdrawRegion = Game.WithdrawRegion
            .findAll(images.withdraw)
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
     * Special result screen check for GudaGuda Final Honnouji.
     *
     * The check only runs if `GudaFinal` is activated in the preferences and if the GameServer is
     * set to Japanese.
     *
     * When this event comes to other regions, the GameServer condition needs to be extended and image should be added.
     */
    private fun isGudaFinalRewardsScreen(): Boolean {
        return false
//        if (!prefs.GudaFinal || prefs.GameServer != GameServerEnum.Jp)
//            return false
//
//        return Game.GudaFinalRewardsRegion.exists(images.GudaFinalRewards)
    }

    /**
     * Clicks on the Close button for the special GudaGuda Final Honnouji reward window if it was
     * detected.
     */
    private fun gudaFinalReward() = Game.GudaFinalRewardsRegion.click()

    /**
     * Checks if the SKIP button exists on the screen.
     */
    private fun needsToStorySkip(): Boolean {
        // TODO: Story Skip doesn't work correctly
        //if (Game.MenuStorySkipRegion.exists(images.StorySkip))
        return prefs.storySkip
    }

    /**
     * Refills the AP with apples depending on [prefs.refill].
     */
    private fun refillStamina() {
        val refillPrefs = prefs.refill

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

    fun selectParty() {
        val party = prefs.selectedAutoSkillConfig.party

        if (!partySelected && party in Game.PartySelectionArray.indices) {
            // Start Quest Button becomes unresponsive if the same party is clicked.
            // So we switch to one party and then to the user-specified one.
            val tempParty = if (party == 0) 1 else 0
            Game.PartySelectionArray[tempParty].click()

            1.seconds.wait()

            Game.PartySelectionArray[party].click()

            1.2.seconds.wait()

            // If we select the party once, the same party will be used by the game for next fight
            // So, we don't have to select it again
            partySelected = true
        }
    }

    /**
     * Starts the quest after the support has already been selected. The following features are done optionally:
     * 1. The configured party is selected if it is set in the selected AutoSkill config
     * 2. A boost item is selected if [prefs.boostItemSelectionMode] is set (needed in some events)
     * 3. The story is skipped if [prefs.storySkip] is activated
     */
    private fun startQuest() {
        selectParty()

        Game.MenuStartQuestClick.click()

        2.seconds.wait()

        val boostItem = prefs.boostItemSelectionMode
        if (boostItem >= 0) {
            Game.MenuBoostItemClickArray[boostItem].click()

            // in case you run out of items
            Game.MenuBoostItemSkipClick.click()
        }

        if (prefs.storySkip) {
            10.seconds.wait()

            if (needsToStorySkip()) {
                Game.MenuStorySkipClick.click()
                0.5.seconds.wait()
                Game.MenuStorySkipYesClick.click()
            }
        }
    }

    /**
     * Will show a toast informing the user of how many apples have been used so far.
     */
    private fun showRefillsUsedMessage() {
        if (prefs.refill.enabled) {
            val refillRepetitions = prefs.refill.repetitions
            if (refillRepetitions > 0) {
                platformImpl.toast("$stonesUsed refills used out of $refillRepetitions")
            }
        }
    }

    private fun afterSelectingQuest() {
        1.5.seconds.wait()

        // Inventory full. Stop script.
        when (prefs.gameServer) {
            // We only have images for JP and NA
            GameServerEnum.En, GameServerEnum.Jp -> {
                if (Game.InventoryFullRegion.exists(images.inventoryFull)) {
                    throw ScriptExitException("Inventory Full")
                }
            }
        }

        // Auto refill
        while (Game.StaminaScreenRegion.exists(images.stamina)) {
            refillStamina()
        }
    }
}