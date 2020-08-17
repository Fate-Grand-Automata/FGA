package com.mathewsachin.fategrandautomata.scripts.entrypoints

import com.mathewsachin.fategrandautomata.StorageDirs
import com.mathewsachin.fategrandautomata.scripts.IFGAutomataApi
import com.mathewsachin.fategrandautomata.scripts.enums.GameServerEnum
import com.mathewsachin.fategrandautomata.scripts.models.BoostItem
import com.mathewsachin.fategrandautomata.scripts.models.RefillResource
import com.mathewsachin.fategrandautomata.scripts.modules.*
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences
import com.mathewsachin.libautomata.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
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
    fgAutomataApi: IFGAutomataApi,
    val storageDirs: StorageDirs
) : EntryPoint(exitManager, platformImpl), IFGAutomataApi by fgAutomataApi {
    private val support = Support(fgAutomataApi)
    private val card = Card(fgAutomataApi)
    private val battle = Battle(fgAutomataApi)
    private val autoSkill = AutoSkill(fgAutomataApi)

    private var stonesUsed = 0
    private var isContinuing = false
    private var partySelected = false

    override fun script(): Nothing {
        init()

        // a map of validators and associated actions
        // if the validator function evaluates to true, the associated action function is called
        val screens: Map<() -> Boolean, () -> Unit> = mapOf(
            { battle.needsToRetry() } to { battle.retry() },
            { battle.isIdle() } to { battle.performBattle() },
            { isInMenu() } to { menu() },
            { isInResult() } to { result() },
            { isInQuestRewardScreen() } to { questReward() },
            { isInSupport() } to { support() },
            { isRepeatScreen() } to { repeatQuest() },
            { needsToWithdraw() } to { withdraw() },
            { needsToStorySkip() } to { skipStory() },
            { isFriendRequestScreen() } to { skipFriendRequestScreen() },
            { isCeReward() } to { ceReward() }
            //{ isGudaFinalRewardsScreen() } to { gudaFinalReward() }
        )

        // Loop through SCREENS until a Validator returns true
        while (true) {
            val actor = screenshotManager.useSameSnapIn {
                screens
                    .asSequence()
                    .filter { (validator, _) -> validator() }
                    .map { (_, actor) -> actor }
                    .firstOrNull()
            }

            actor?.invoke()

            1.seconds.wait()
        }
    }

    override fun postActions() {
        if (prefs.refill.autoDecrement) {
            prefs.refill.repetitions -= stonesUsed
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
    private fun isInMenu() = images.menu in game.menuScreenRegion

    /**
     * Resets the battle state, clicks on the quest and refills the AP if needed.
     */
    private fun menu() {
        battle.resetState()

        showRefillsAndRunsMessage()

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
        if (images.result in game.resultScreenRegion
            || images.bond in game.resultBondRegion
            // We're assuming CN and TW use the same Master/Mystic Code Level up image
            || images.masterLvlUp in game.resultMasterLvlUpRegion
        ) {
            return true
        }

        val gameServer = prefs.gameServer

        // We don't have TW images for these
        if (gameServer != GameServerEnum.Tw) {
            return images.masterExp in game.resultMasterExpRegion
                    || images.matRewards in game.resultMatRewardsRegion
        }

        // Not in any result screen
        return false
    }

    /**
     * Clicks through the reward screens.
     */
    private fun result() {
        if (prefs.stopOnCEDrop && images.ceDrop in game.resultCeDropRegion) {
            throw ScriptExitException("CE Dropped!")
        }

        if (prefs.screenshotDrops) {
            resultForDrops()
        } else game.resultNextClick.click(20)
    }

    private fun resultForDrops() {
        while (true) {
            var dropScreen = false

            screenshotManager.useSameSnapIn {
                when {
                    isCeReward() -> ceReward()
                    images.matRewards in game.resultMatRewardsRegion -> dropScreen = true
                    else -> {
                        game.resultNextClick.click()
                        0.1.seconds.wait()
                    }
                }
            }

            if (dropScreen) {
                screenshotDrops()
                game.resultNextClick.click()
                return
            }
        }
    }

    private fun screenshotDrops() {
        0.5.seconds.wait()

        val dropsFolder = File(
            storageDirs.storageRoot,
            "drops"
        )

        if (!dropsFolder.exists()) {
            dropsFolder.mkdirs()
        }

        val sdf = SimpleDateFormat("dd-M-yyyy-hh-mm-ss", Locale.US)
        val timeString = sdf.format(Date())

        for (i in 0..1) {
            val dropFileName = "${timeString}.${i}.png"

            val shotService = screenshotManager.screenshotService
            val shot = if (shotService is IColorScreenshotProvider) {
                shotService.takeColorScreenshot()
            } else screenshotManager.getScreenshot()

            shot.use {
                it.save(
                    File(dropsFolder, dropFileName).absolutePath
                )
            }

            // check if we need to scroll to see more drops
            if (images.dropScrollbar in game.resultDropScrollbarRegion) {
                // scroll to end
                Location(2306, 1032).click()
            } else break
        }
    }

    private fun isRepeatScreen() =
        when (prefs.gameServer) {
            // We only have images for JP and NA
            GameServerEnum.En, GameServerEnum.Jp -> {
                images.confirm in game.continueRegion
            }
            else -> false
        }

    private fun repeatQuest() {
        // Needed to show we don't need to enter the "StartQuest" function
        isContinuing = true

        // Pressing Continue option after completing a quest, reseting the state as would occur in "Menu" function
        battle.resetState()
        game.continueClick.click()

        showRefillsAndRunsMessage()

        // If Stamina is empty, follow same protocol as is in "Menu" function Auto refill.
        afterSelectingQuest()
    }

    private fun isFriendRequestScreen() =
        images.friendRequest in game.resultFriendRequestRegion

    private fun skipFriendRequestScreen() {
        // Friend request dialogue. Appears when non-friend support was selected this battle. Ofc it's defaulted not sending request.
        game.resultFriendRequestRejectClick.click()
    }

    private fun isCeReward() =
        images.bond10Reward in game.resultCeRewardRegion

    private fun ceReward() {
        if (prefs.stopOnCEGet) {
            throw ScriptExitException("CE GET!")
        }

        game.resultCeRewardCloseClick.click()
        1.seconds.wait()
        game.resultCeRewardCloseClick.click()
    }

    /**
     * Checks if FGO is on the quest reward screen for Mana Prisms, SQ, ...
     */
    private fun isInQuestRewardScreen() =
        images.questReward in game.resultQuestRewardRegion

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
        images.withdraw in game.withdrawRegion

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
     * Will show a toast informing the user of number of runs and how many apples have been used so far.
     */
    private fun showRefillsAndRunsMessage() {
        val message = StringBuilder().apply {
            val refill = prefs.refill

            if (refill.shouldLimitRuns && refill.limitRuns > 0) {
                appendln("Ran ${battle.runs} out of ${refill.limitRuns} time(s)")
            } else if (battle.runs > 0) {
                appendln("Ran ${battle.runs} time(s)")
            }

            if (refill.enabled) {
                val refillRepetitions = refill.repetitions
                if (refillRepetitions > 0) {
                    appendln("$stonesUsed refills used out of $refillRepetitions")
                }
            }
        }.toString().trimEnd()

        if (message.isNotBlank()) {
            platformImpl.toast(message)
        }
    }

    private fun afterSelectingQuest() {
        1.5.seconds.wait()

        // Inventory full. Stop script.
        when (prefs.gameServer) {
            // We only have images for JP and NA
            GameServerEnum.En, GameServerEnum.Jp -> {
                if (images.inventoryFull in game.inventoryFullRegion) {
                    throw ScriptExitException("Inventory Full")
                }
            }
        }

        // Auto refill
        while (images.stamina in game.staminaScreenRegion) {
            refillStamina()
        }
    }
}