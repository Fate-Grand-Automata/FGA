package com.mathewsachin.fategrandautomata.scripts.entrypoints

import com.mathewsachin.fategrandautomata.IStorageProvider
import com.mathewsachin.fategrandautomata.scripts.IFgoAutomataApi
import com.mathewsachin.fategrandautomata.scripts.Images
import com.mathewsachin.fategrandautomata.scripts.ScriptLog
import com.mathewsachin.fategrandautomata.scripts.ScriptNotify
import com.mathewsachin.fategrandautomata.scripts.enums.GameServerEnum
import com.mathewsachin.fategrandautomata.scripts.enums.MaterialEnum
import com.mathewsachin.fategrandautomata.scripts.models.BoostItem
import com.mathewsachin.fategrandautomata.scripts.modules.*
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences
import com.mathewsachin.libautomata.*
import javax.inject.Inject
import kotlin.math.absoluteValue
import kotlin.time.Duration

/**
 * Checks if Support Selection menu is up
 */
fun IFgoAutomataApi.isInSupport(): Boolean {
    return game.supportScreenRegion.exists(images[Images.SupportScreen], similarity = 0.85)
}

fun IFgoAutomataApi.isInventoryFull() =
    // We only have images for JP and NA
    prefs.gameServer in listOf(GameServerEnum.En, GameServerEnum.Jp)
            && images[Images.InventoryFull] in game.inventoryFullRegion

/**
 * Script for starting quests, selecting the support and doing battles.
 */
open class AutoBattle @Inject constructor(
    exitManager: ExitManager,
    fgAutomataApi: IFgoAutomataApi,
    val storageProvider: IStorageProvider
) : EntryPoint(exitManager), IFgoAutomataApi by fgAutomataApi {
    sealed class ExitReason {
        object Abort : ExitReason()
        class Unexpected(val e: Exception) : ExitReason()
        object CEGet : ExitReason()
        object CEDropped : ExitReason()
        object FirstClearRewards : ExitReason()
        class LimitMaterials(val count: Int) : ExitReason()
        object WithdrawDisabled : ExitReason()
        object APRanOut : ExitReason()
        object InventoryFull : ExitReason()
        class LimitRuns(val count: Int) : ExitReason()
        object SupportSelectionManual : ExitReason()
        object SupportSelectionFriendNotSet : ExitReason()
        object SupportSelectionPreferredNotSet : ExitReason()
        class SkillCommandParseError(val e: Exception) : ExitReason()
        class CardPriorityParseError(val msg: String) : ExitReason()
    }

    internal class BattleExitException(val reason: ExitReason) : Exception()

    class ExitException(val reason: ExitReason, val state: ExitState) : Exception()

    private val support = Support(fgAutomataApi)
    private val card = Card(fgAutomataApi)
    private val battle = Battle(fgAutomataApi)
    private val autoSkill = AutoSkill(fgAutomataApi)

    private var stonesUsed = 0
    private var withdrawCount = 0
    private var isContinuing = false
    private var partySelected = false
    private var matsGot = mutableMapOf<MaterialEnum, Int>()
    private var ceDropCount = 0

    override fun script(): Nothing {
        init()

        try {
            loop()
        } catch (e: BattleExitException) {
            throw ExitException(e.reason, makeExitState())
        } catch (e: ScriptAbortException) {
            throw ExitException(ExitReason.Abort, makeExitState())
        } catch (e: Exception) {
            val reason = ExitReason.Unexpected(e)

            throw ExitException(reason, makeExitState())
        } finally {
            val refill = prefs.refill

            // Auto-decrement apples
            refill.repetitions -= stonesUsed

            // Auto-decrement runs
            if (refill.shouldLimitRuns) {
                refill.limitRuns -= battle.state.runs

                // Turn off run limit when done
                if (refill.limitRuns <= 0) {
                    refill.limitRuns = 1
                    refill.shouldLimitRuns = false
                }
            }

            // Auto-decrement materials
            if (refill.shouldLimitMats) {
                refill.limitMats -= matsGot.values.sum()

                // Turn off limit by materials when done
                if (refill.limitMats <= 0) {
                    refill.limitMats = 1
                    refill.shouldLimitMats = false
                }
            }
        }
    }

    private fun useBoostItem() {
        val boostItem = BoostItem.of(prefs.boostItemSelectionMode)
        if (boostItem is BoostItem.Enabled) {
            game.locate(boostItem).click()

            // in case you run out of items
            if (boostItem !is BoostItem.Enabled.Skip) {
                game.locate(BoostItem.Enabled.Skip).click()
            }
        }
    }

    class ExitState(
        val timesRan: Int,
        val runLimit: Int?,
        val timesRefilled: Int,
        val refillLimit: Int,
        val ceDropCount: Int,
        val materials: Map<MaterialEnum, Int>,
        val matLimit: Int?,
        val withdrawCount: Int,
        val totalTime: Duration,
        val averageTimePerRun: Duration,
        val minTurnsPerRun: Int,
        val maxTurnsPerRun: Int,
        val averageTurnsPerRun: Int
    )

    private fun makeExitState(): ExitState {
        return ExitState(
            timesRan = battle.state.runs,
            runLimit = if (prefs.refill.shouldLimitRuns) prefs.refill.limitRuns else null,
            timesRefilled = stonesUsed,
            refillLimit = prefs.refill.repetitions,
            ceDropCount = ceDropCount,
            materials = matsGot,
            matLimit = if (prefs.refill.shouldLimitMats) prefs.refill.limitMats else null,
            withdrawCount = withdrawCount,
            totalTime = battle.state.totalBattleTime,
            averageTimePerRun = battle.state.averageTimePerRun,
            minTurnsPerRun = battle.state.minTurnsPerRun,
            maxTurnsPerRun = battle.state.maxTurnsPerRun,
            averageTurnsPerRun = battle.state.averageTurnsPerRun
        )
    }

    private fun loop(): Nothing {
        // a map of validators and associated actions
        // if the validator function evaluates to true, the associated action function is called
        val screens: Map<() -> Boolean, () -> Unit> = mapOf(
            { battle.needsToRetry() } to { battle.retry() },
            { battle.isIdle() } to { battle.performBattle() },
            { isInMenu() } to { menu() },
            { isInResult() } to { result() },
            { isInDropsScreen() } to { dropScreen() },
            { isInQuestRewardScreen() } to { questReward() },
            { isInSupport() } to { support() },
            { isRepeatScreen() } to { repeatQuest() },
            { needsToWithdraw() } to { withdraw() },
            { needsToStorySkip() } to { skipStory() },
            { isFriendRequestScreen() } to { skipFriendRequestScreen() },
            { isBond10CEReward() } to { bond10CEReward() },
            { isCeRewardDetails() } to { ceRewardDetails() },
            { isDeathAnimation() } to { game.skipDeathAnimationClick.click() }
        )

        // Loop through SCREENS until a Validator returns true
        while (true) {
            val actor = useSameSnapIn {
                screens
                    .asSequence()
                    .filter { (validator, _) -> validator() }
                    .map { (_, actor) -> actor }
                    .firstOrNull()
            }

            actor?.invoke()

            Duration.seconds(1).wait()
        }
    }

    /**
     * Initialize Aspect Ratio adjustment for different sized screens,ask for input from user for
     * Autoskill plus confirming Apple/Stone usage.
     *
     * Then initialize the AutoSkill, Battle, and Card modules in modules.
     */
    private fun init() {
        autoSkill.init(battle, card)
        battle.init(autoSkill, card)
        card.init(autoSkill, battle)

        support.init()

        // Set all Materials to 0
        prefs.selectedBattleConfig
            .materials
            .associateWithTo(matsGot) { 0 }
    }

    /**
     *  Checks if in menu.png is on the screen, indicating that a quest can be chosen.
     */
    private fun isInMenu() = images[Images.Menu] in game.menuScreenRegion

    /**
     * Resets the battle state, clicks on the quest and refills the AP if needed.
     */
    private fun menu() {
        // In case the repeat loop breaks and we end up in menu (like withdrawing from quests)
        isContinuing = false

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
        val cases = sequenceOf(
            images[Images.Result] to game.resultScreenRegion,
            images[Images.Bond] to game.resultBondRegion,
            images[Images.MasterLevelUp] to game.resultMasterLvlUpRegion,
            images[Images.MasterExp] to game.resultMasterExpRegion
        )

        return cases.any { (image, region) -> image in region }
    }

    private fun isBond10CEReward() =
        game.resultCeRewardRegion.exists(images[Images.Bond10Reward], similarity = 0.75)

    /**
     * It seems like we need to click on CE (center of screen) to accept them
     */
    private fun bond10CEReward() =
        game.scriptArea.center.click()

    private fun isCeRewardDetails() =
        images[Images.CEDetails] in game.resultCeRewardDetailsRegion

    private fun isDeathAnimation() =
        game.servantDeathCheckRegions
            .count { images[Images.ServantExist] in it } in 1..2

    private fun ceRewardDetails() {
        if (prefs.stopOnCEGet) {
            // Count the current run
            battle.state.nextRun()

            throw BattleExitException(ExitReason.CEGet)
        } else messages.notify(ScriptNotify.CEGet)

        game.resultCeRewardCloseClick.click()
    }

    /**
     * Clicks through the reward screens.
     */
    private fun result() =
        game.resultClick.click(15)

    private fun isInDropsScreen() =
        images[Images.MatRewards] in game.resultMatRewardsRegion

    private fun dropScreen() {
        checkCEDrops()

        trackMaterials()

        if (prefs.screenshotDrops) {
            screenshotDrops()
        }

        game.resultMatRewardsRegion.click()
    }

    private fun checkCEDrops() {
        val starsRegion = Region(40, -40, 80, 40)

        val ceDropped = game.scriptArea
            .findAll(images[Images.DropCE])
            .map { (region, _) ->
                starsRegion + region.location
            }
            .count { images[Images.DropCEStars] in it }

        if (ceDropped > 0) {
            ceDropCount += ceDropped

            if (prefs.stopOnCEDrop) {
                // Count the current run
                battle.state.nextRun()

                throw BattleExitException(ExitReason.CEDropped)
            } else messages.notify(ScriptNotify.CEDropped)
        }
    }

    private fun trackMaterials() {
        for (material in prefs.selectedBattleConfig.materials) {
            val pattern = images.loadMaterial(material)

            // TODO: Make the search region smaller
            val count = game.scriptArea
                .findAll(pattern)
                .count()

            // Increment material count
            matsGot.merge(material, count, Int::plus)
        }

        if (prefs.refill.shouldLimitMats) {
            val totalMats = matsGot
                .values
                .sum()

            if (totalMats >= prefs.refill.limitMats) {
                // Count the current run
                battle.state.nextRun()

                throw BattleExitException(ExitReason.LimitMaterials(totalMats))
            }
        }
    }

    private fun screenshotDrops() {
        val drops = mutableListOf<IPattern>()

        for (i in 0..1) {
            drops.add(takeColorScreenshot())

            // check if we need to scroll to see more drops
            if (i == 0 && images[Images.DropScrollbar] in game.resultDropScrollbarRegion) {
                // scroll to end
                game.resultDropScrollEndClick.click()
            } else break
        }

        storageProvider.dropScreenshot(drops)
    }

    private fun isRepeatScreen() = images[Images.Repeat] in game.continueRegion

    private fun repeatQuest() {
        // Needed to show we don't need to enter the "StartQuest" function
        isContinuing = true

        // Pressing Continue option after completing a quest, resetting the state as would occur in "Menu" function
        battle.resetState()

        val continueButtonRegion = game.continueRegion.find(images[Images.Repeat])?.region
            ?: return

        // If Boost items are usable, Continue button shifts to the right
        val useBoost = if (continueButtonRegion.x > game.scriptArea.center.x + 350) {
            val boost = BoostItem.of(prefs.boostItemSelectionMode)

            boost is BoostItem.Enabled && boost != BoostItem.Enabled.Skip
        } else false

        if (useBoost) {
            game.continueBoostClick.click()
            useBoostItem()
        } else continueButtonRegion.click()

        showRefillsAndRunsMessage()

        // If Stamina is empty, follow same protocol as is in "Menu" function Auto refill.
        afterSelectingQuest()
    }

    private fun isFriendRequestScreen() =
        images[Images.SupportExtra] in game.resultFriendRequestRegion

    private fun skipFriendRequestScreen() {
        // Friend request dialogue. Appears when non-friend support was selected this battle. Ofc it's defaulted not sending request.
        game.resultFriendRequestRejectClick.click()
    }

    /**
     * Checks if FGO is on the quest reward screen for Mana Prisms, SQ, ...
     */
    private fun isInQuestRewardScreen() =
        images[Images.QuestReward] in game.resultQuestRewardRegion

    /**
     * Handles the quest rewards screen.
     */
    private fun questReward() {
        if (prefs.stopOnFirstClearRewards) {
            // Count the current run
            battle.state.nextRun()

            throw BattleExitException(ExitReason.FirstClearRewards)
        }

        game.resultClick.click()
    }

    // Selections Support option
    private fun support() {
        // Friend selection
        val hasSelectedSupport =
            support.selectSupport(prefs.selectedBattleConfig.support.selectionMode, isContinuing)

        if (hasSelectedSupport && !isContinuing) {
            Duration.seconds(4).wait()
            startQuest()

            // Wait timer till battle starts.
            // Uses less battery to wait than to search for images for a few seconds.
            // Adjust according to device.
            Duration.seconds(5).wait()
        }
    }

    /**
     * Checks if the window for withdrawing from the battle exists.
     */
    private fun needsToWithdraw() =
        images[Images.Withdraw] in game.withdrawRegion

    /**
     * Handles withdrawing from battle. Depending on [IPreferences.withdrawEnabled], the script either
     * withdraws automatically or stops completely.
     */
    private fun withdraw() {
        if (!prefs.withdrawEnabled) {
            throw BattleExitException(ExitReason.WithdrawDisabled)
        }

        // Withdraw Region can vary depending on if you have Command Spells/Quartz
        val withdrawRegion = game.withdrawRegion.find(images[Images.Withdraw])
            ?: return

        withdrawRegion.region.click()

        Duration.seconds(0.5).wait()

        // Click the "Accept" button after choosing to withdraw
        game.withdrawAcceptClick.click()

        Duration.seconds(1).wait()

        // Click the "Close" button after accepting the withdrawal
        game.withdrawCloseClick.click()

        ++withdrawCount
    }

    /**
     * Checks if the SKIP button exists on the screen.
     */
    private fun needsToStorySkip() =
        prefs.storySkip && game.menuStorySkipRegion.exists(images[Images.StorySkip], similarity = 0.7)

    private fun skipStory() {
        game.menuStorySkipClick.click()
        Duration.seconds(0.5).wait()
        game.menuStorySkipYesClick.click()
    }

    /**
     * Refills the AP with apples depending on [IPreferences.refill].
     * Otherwise if [IPreferences.waitAPRegen] is true, loops and wait for AP regeneration
     */
    private fun refillStamina() {
        val refill = prefs.refill

        if (refill.resources.isNotEmpty()
            && stonesUsed < refill.repetitions
        ) {
            refill.resources
                .map { game.locate(it) }
                .forEach { it.click() }

            Duration.seconds(1).wait()
            game.staminaOkClick.click()
            ++stonesUsed

            Duration.seconds(3).wait()
        } else if (prefs.waitAPRegen) {
            game.staminaCloseClick.click()

            messages.notify(ScriptNotify.WaitForAPRegen())

            Duration.seconds(60).wait()
        } else throw BattleExitException(ExitReason.APRanOut)
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
        val party = prefs.selectedBattleConfig.party

        if (!partySelected && party in game.partySelectionArray.indices) {
            val currentParty = game.selectedPartyRegion
                .find(images[Images.SelectedParty])
                ?.let { match ->
                    // Find party with min distance from center of matched region
                    game.partySelectionArray.withIndex().minByOrNull {
                        (it.value.x - match.region.center.x).absoluteValue
                    }?.index
                }

            messages.log(
                ScriptLog.CurrentParty(currentParty)
            )

            /* If the currently selected party cannot be detected, we need to switch to a party
               which was not configured. The reason is that the "Start Quest" button becomes
               unresponsive if you switch from a party to the same one. */
            if (currentParty == null) {
                val tempParty = if (party == 0) 1 else 0
                game.partySelectionArray[tempParty].click()

                Duration.seconds(1).wait()
            }

            // Switch to the configured party
            if (currentParty != party) {
                game.partySelectionArray[party].click()

                Duration.seconds(1.2).wait()
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

        Duration.seconds(2).wait()

        useBoostItem()
    }

    /**
     * Will show a toast informing the user of number of runs and how many apples have been used so far.
     */
    private fun showRefillsAndRunsMessage() =
        messages.notify(
            ScriptNotify.BetweenRuns(
                refills = stonesUsed,
                runs = battle.state.runs
            )
        )

    private fun afterSelectingQuest() {
        Duration.seconds(1.5).wait()

        if (isInventoryFull()) {
            throw BattleExitException(ExitReason.InventoryFull)
        }

        // Auto refill
        while (images[Images.Stamina] in game.staminaScreenRegion) {
            refillStamina()
        }
    }
}
