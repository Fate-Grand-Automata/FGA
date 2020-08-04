package com.mathewsachin.fategrandautomata.scripts.entrypoints

import com.mathewsachin.fategrandautomata.scripts.IFGAutomataApi
import com.mathewsachin.fategrandautomata.scripts.enums.GameServerEnum
import com.mathewsachin.fategrandautomata.scripts.models.BoostItem
import com.mathewsachin.fategrandautomata.scripts.models.RefillResource
import com.mathewsachin.fategrandautomata.scripts.modules.*
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences
import com.mathewsachin.libautomata.EntryPoint
import com.mathewsachin.libautomata.ExitManager
import com.mathewsachin.libautomata.IPlatformImpl
import com.mathewsachin.libautomata.ScriptExitException
import javax.inject.Inject
import kotlin.math.absoluteValue
import kotlin.time.seconds

/**
 * Checks if Support Selection menu is up
 */
fun IFGAutomataApi.isInSupport(): Boolean {
    return game.supportScreenRegion.exists(images.supportScreen, Similarity = 0.85)
}

/**
 * Script for starting quests, selecting the support and doing battles.
 */
open class AutoBattle @Inject constructor(
    exitManager: ExitManager,
    platformImpl: IPlatformImpl,
    fgAutomataApi: IFGAutomataApi
) : EntryPoint(exitManager, platformImpl), IFGAutomataApi by fgAutomataApi {
    private val support =
        Support(fgAutomataApi)
    private val card = Card(fgAutomataApi)
    private val battle =
        Battle(fgAutomataApi)
    private val autoSkill =
        AutoSkill(fgAutomataApi)

    private var stonesUsed = 0
    private var isContinuing = false
    private var partySelected = false

    override fun script(): Nothing {
        init()

        // a map of validators and associated actions
        // if the validator function evaluates to true, the associated action function is called
        val screens = mapOf(
            { battle.needsToRetry() } to { battle.retry() },
            { battle.isIdle() } to { battle.performBattle() },
            { isInMenu() } to { menu() },
            { isInResult() } to { result() },
            { isInQuestRewardScreen() } to { questReward() },
            { isInSupport() } to { support() },
            { needsToWithdraw() } to { withdraw() },
            { needsToStorySkip() } to { skipStory() }
            //{ isGudaFinalRewardsScreen() } to { gudaFinalReward() }
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
        scaling.init()

        autoSkill.init(battle, card)
        battle.init(autoSkill, card)
        card.init(autoSkill, battle)

        support.init()
    }

    /**
     *  Checks if in menu.png is on the screen, indicating that a quest can be chosen.
     */
    private fun isInMenu(): Boolean {
        return game.menuScreenRegion.exists(images.menu)
    }

    /**
     * Resets the battle state, clicks on the quest and refills the AP if needed.
     */
    private fun menu() {
        battle.resetState()

        showRefillsUsedMessage()

        // Click uppermost quest
        game.menuSelectQuestClick.click()

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
        if (game.resultScreenRegion.exists(images.result)
            || game.resultBondRegion.exists(images.bond)
            // We're assuming CN and TW use the same Master/Mystic Code Level up image
            || game.resultMasterLvlUpRegion.exists(images.masterLvlUp)
        ) {
            return true
        }

        val gameServer = prefs.gameServer

        // We don't have TW images for these
        if (gameServer != GameServerEnum.Tw) {
            return game.resultMasterExpRegion.exists(images.masterExp)
                    || game.resultMatRewardsRegion.exists(images.matRewards)
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
        game.resultNextClick.click(55)

        // Checking if there was a Bond CE reward
        if (game.resultCeRewardRegion.exists(images.bond10Reward)) {
            if (prefs.stopAfterBond10) {
                throw ScriptExitException("Bond 10 CE GET!")
            }

            game.resultCeRewardCloseClick.click()

            // Still need to proceed through reward screen.
            game.resultNextClick.click(35)
        }

        5.seconds.wait()

        // Friend request dialogue. Appears when non-friend support was selected this battle. Ofc it's defaulted not sending request.
        if (game.resultFriendRequestRegion.exists(images.friendRequest)) {
            game.resultFriendRequestRejectClick.click()
        }

        1.seconds.wait()

        // Searches for the Continue option after select Free Quests
        when (prefs.gameServer) {
            // We only have images for JP and NA
            GameServerEnum.En, GameServerEnum.Jp -> {
                if (game.continueRegion.exists(images.confirm)) {
                    // Needed to show we don't need to enter the "StartQuest" function
                    isContinuing = true

                    // Pressing Continue option after completing a quest, reseting the state as would occur in "Menu" function
                    game.continueClick.click()
                    battle.resetState()

                    showRefillsUsedMessage()

                    // If Stamina is empty, follow same protocol as is in "Menu" function Auto refill.
                    afterSelectingQuest()

                    return
                }
            }
        }

        // TODO: Move to outer loop
        // Quest Completion reward. Exits the screen when it is presented.
        if (game.resultCeRewardRegion.exists(images.bond10Reward)) {
            game.resultCeRewardCloseClick.click()
            1.seconds.wait()
            game.resultCeRewardCloseClick.click()
        }
    }

    /**
     * Checks if FGO is on the quest reward screen for Mana Prisms, SQ, ...
     */
    private fun isInQuestRewardScreen() =
        game.resultQuestRewardRegion.exists(images.questReward)

    /**
     * Handles the quest rewards screen.
     */
    private fun questReward() = game.resultNextClick.click()

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
            5.seconds.wait()
        }
    }

    /**
     * Checks if the window for withdrawing from the battle exists.
     */
    private fun needsToWithdraw() =
        game.withdrawRegion.exists(images.withdraw)

    /**
     * Handles withdrawing from battle. Depending on [IPreferences.withdrawEnabled], the script either
     * withdraws automatically or stops completely.
     */
    private fun withdraw() {
        if (!prefs.withdrawEnabled) {
            throw ScriptExitException("All servants have been defeated and auto-withdrawing is disabled.")
        }

        // Withdraw Region can vary depending on if you have Command Spells/Quartz
        val withdrawRegion = game.withdrawRegion
            .findAll(images.withdraw)
            .firstOrNull() ?: return

        withdrawRegion.Region.click()

        0.5.seconds.wait()

        // Click the "Accept" button after choosing to withdraw
        game.withdrawAcceptClick.click()

        1.seconds.wait()

        // Click the "Close" button after accepting the withdrawal
        game.withdrawCloseClick.click()
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
//        return game.GudaFinalRewardsRegion.exists(images.GudaFinalRewards)
    }

    /**
     * Clicks on the Close button for the special GudaGuda Final Honnouji reward window if it was
     * detected.
     */
    private fun gudaFinalReward() = game.gudaFinalRewardsRegion.click()

    /**
     * Checks if the SKIP button exists on the screen.
     */
    private fun needsToStorySkip() =
        prefs.storySkip && game.menuStorySkipRegion.exists(images.storySkip, Similarity = 0.7)

    private fun skipStory() {
        game.menuStorySkipClick.click()
        0.5.seconds.wait()
        game.menuStorySkipYesClick.click()
    }

    /**
     * Refills the AP with apples depending on [IPreferences.refill].
     */
    private fun refillStamina() {
        val refillPrefs = prefs.refill

        if (refillPrefs.enabled && stonesUsed < refillPrefs.repetitions) {
            when (val resource = RefillResource.of(refillPrefs.resource)) {
                is RefillResource.Single -> resource.clickLocation.click()
                is RefillResource.Multiple -> resource.items.forEach {
                    it.clickLocation.click()
                }
            }

            1.seconds.wait()
            game.staminaOkClick.click()
            ++stonesUsed

            3.seconds.wait()
        } else throw ScriptExitException("AP ran out!")
    }

    /**
     * Selects the party for the quest based on the AutoSkill configuration.
     *
     * The possible behaviors of this method are:
     * 1. If no value is specified, the currently selected party is used.
     * 2. If a value is specified and is the same as the currently selected party, the party is not
     * changed.
     * 3. If a value is specified and is different than the currently selected party, the party is
     * changed to the configured one by clicking on the little dots above the party names.
     */
    fun selectParty() {
        val party = prefs.selectedAutoSkillConfig.party

        if (!partySelected && party in game.partySelectionArray.indices) {
            val currentParty = game.selectedPartyRegion
                .findAll(images.selectedParty)
                .map { match ->
                    // Find party with min distance from center of matched region
                    game.partySelectionArray.withIndex().minBy {
                        (it.value.X - match.Region.center.X).absoluteValue
                    }?.index
                }
                .firstOrNull()

            /* If the currently selected party cannot be detected, we need to switch to a party
               which was not configured. The reason is that the "Start Quest" button becomes
               unresponsive if you switch from a party to the same one. */
            if (currentParty == null) {
                val tempParty = if (party == 0) 1 else 0
                game.partySelectionArray[tempParty].click()

                1.seconds.wait()
            }

            // Switch to the configured party
            if (currentParty != party) {
                game.partySelectionArray[party].click()

                1.2.seconds.wait()
            }

            /* If we select the party once, the same party will be used by the game for next fight.
               So, we don't have to select it again. */
            partySelected = true
        }
    }

    /**
     * Starts the quest after the support has already been selected. The following features are done optionally:
     * 1. The configured party is selected if it is set in the selected AutoSkill config
     * 2. A boost item is selected if [IPreferences.boostItemSelectionMode] is set (needed in some events)
     * 3. The story is skipped if [IPreferences.storySkip] is activated
     */
    private fun startQuest() {
        selectParty()

        game.menuStartQuestClick.click()

        2.seconds.wait()

        val boostItem = BoostItem.of(prefs.boostItemSelectionMode)
        if (boostItem is BoostItem.Enabled) {
            boostItem.clickLocation.click()

            // in case you run out of items
            if (boostItem !is BoostItem.Enabled.Skip) {
                BoostItem.Enabled.Skip.clickLocation.click()
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
                if (game.inventoryFullRegion.exists(images.inventoryFull)) {
                    throw ScriptExitException("Inventory Full")
                }
            }
        }

        // Auto refill
        while (game.staminaScreenRegion.exists(images.stamina)) {
            refillStamina()
        }
    }
}