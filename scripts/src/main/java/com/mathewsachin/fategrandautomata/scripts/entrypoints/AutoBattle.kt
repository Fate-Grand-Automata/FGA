package com.mathewsachin.fategrandautomata.scripts.entrypoints

import com.mathewsachin.fategrandautomata.IStorageProvider
import com.mathewsachin.fategrandautomata.scripts.IFgoAutomataApi
import com.mathewsachin.fategrandautomata.scripts.Images
import com.mathewsachin.fategrandautomata.scripts.enums.GameServerEnum
import com.mathewsachin.fategrandautomata.scripts.enums.MaterialEnum
import com.mathewsachin.fategrandautomata.scripts.models.BoostItem
import com.mathewsachin.fategrandautomata.scripts.modules.*
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences
import com.mathewsachin.libautomata.*
import timber.log.Timber
import timber.log.debug
import javax.inject.Inject
import kotlin.math.absoluteValue
import kotlin.time.seconds

/**
 * Checks if Support Selection menu is up
 */
fun IFgoAutomataApi.isInSupport(): Boolean {
    return game.supportScreenRegion.exists(images[Images.SupportScreen], Similarity = 0.85)
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
        object Abort: ExitReason()
        class Unexpected(val e: Exception): ExitReason()
        object CEGet: ExitReason()
        object CEDropped: ExitReason()
        class LimitMaterials(val count: Int): ExitReason()
        object WithdrawDisabled: ExitReason()
        object APRanOut: ExitReason()
        object InventoryFull: ExitReason()
        class LimitRuns(val count: Int): ExitReason()
        object SupportSelectionManual: ExitReason()
        object SupportSelectionFriendNotSet: ExitReason()
        object SupportSelectionPreferredNotSet: ExitReason()
        class SkillCommandParseError(val e: Exception): ExitReason()
        class CardPriorityParseError(val msg: String): ExitReason()
    }

    internal class BattleExitException(val reason: ExitReason): Exception()

    class ExitException(val reason: ExitReason, val msg: String): Exception()

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
            throw ExitException(e.reason, makeExitMessage(e.reason))
        } catch (e: ScriptAbortException) {
            val msg = makeExitMessage(ExitReason.Abort)

            throw ExitException(ExitReason.Abort, msg)
        } catch (e: Exception) {
            val reason = ExitReason.Unexpected(e)

            throw ExitException(reason, makeExitMessage(reason))
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

    private fun ExitReason.text(): String = when (this) {
        ExitReason.Abort -> messages.stoppedByUser
        is ExitReason.Unexpected -> "${messages.unexpectedError}: ${e.message}"
        ExitReason.CEGet -> messages.ceGet
        ExitReason.CEDropped -> messages.ceDropped
        is ExitReason.LimitMaterials -> messages.farmedMaterials(count)
        ExitReason.WithdrawDisabled -> messages.withdrawDisabled
        ExitReason.APRanOut -> messages.apRanOut
        ExitReason.InventoryFull -> messages.inventoryFull
        is ExitReason.LimitRuns -> messages.timesRan(count)
        ExitReason.SupportSelectionManual -> messages.supportSelectionManual
        ExitReason.SupportSelectionFriendNotSet -> messages.supportSelectionFriendNotSet
        ExitReason.SupportSelectionPreferredNotSet -> messages.supportSelectionPreferredNotSet
        is ExitReason.SkillCommandParseError -> "AutoSkill Parse error:\n\n${e.message}"
        is ExitReason.CardPriorityParseError -> msg
    }

    private fun makeExitMessage(reason: ExitReason) = buildString {
        appendLine(reason.text())
        appendLine()

        makeRefillAndRunsMessage().let { msg ->
            if (msg.isNotBlank()) {
                appendLine(msg)
            }
        }

        if (!prefs.stopOnCEDrop && ceDropCount > 0) {
            appendLine("$ceDropCount ${messages.ceDropped}")
            appendLine()
        }

        if (prefs.selectedBattleConfig.materials.isNotEmpty()) {
            appendLine(messages.materials(matsGot))
            appendLine()
        }

        appendLine(messages.time(battle.state.totalBattleTime))

        if (battle.state.runs > 1) {
            appendLine(messages.avgTimePerRun(battle.state.averageRunTime))

            with(battle.state) {
                appendLine(messages.turns(minTurns, averageTurns, maxTurns))
            }
        } else if (battle.state.runs == 1) {
            appendLine(messages.turns(battle.state.totalTurns))
        }

        if (withdrawCount > 0) {
            appendLine(messages.timesWithdrew(withdrawCount))
        }
    }.trimEnd()

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
            { isCeRewardDetails() } to { ceRewardDetails() }
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
        game.resultCeRewardRegion.exists(images[Images.Bond10Reward], Similarity = 0.75)

    /**
     * It seems like we need to click on CE (center of screen) to accept them
     */
    private fun bond10CEReward() =
        game.scriptArea.center.click()

    private fun isCeRewardDetails() =
        images[Images.CEDetails] in game.resultCeRewardDetailsRegion

    private fun ceRewardDetails() {
        if (prefs.stopOnCEGet) {
            throw BattleExitException(ExitReason.CEGet)
        } else notify(messages.ceGet)

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
                throw BattleExitException(ExitReason.CEDropped)
            } else notify(messages.ceDropped)
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

        val region = game.continueRegion.find(images[Images.Repeat])?.Region
            ?: return

        // If Boost items are usable, Continue button shifts to the right
        val useBoost = if (region.X > 1630) {
            val boost = BoostItem.of(prefs.boostItemSelectionMode)

            boost is BoostItem.Enabled && boost != BoostItem.Enabled.Skip
        } else false

        if (useBoost) {
            game.continueBoostClick.click()
            useBoostItem()
        } else game.continueClick.click()

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
    private fun questReward() = game.resultClick.click()

    // Selections Support option
    private fun support() {
        // Friend selection
        val hasSelectedSupport =
            support.selectSupport(prefs.selectedBattleConfig.support.selectionMode, isContinuing)

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

        withdrawRegion.Region.click()

        0.5.seconds.wait()

        // Click the "Accept" button after choosing to withdraw
        game.withdrawAcceptClick.click()

        1.seconds.wait()

        // Click the "Close" button after accepting the withdrawal
        game.withdrawCloseClick.click()

        ++withdrawCount
    }

    /**
     * Checks if the SKIP button exists on the screen.
     */
    private fun needsToStorySkip() =
        prefs.storySkip && game.menuStorySkipRegion.exists(images[Images.StorySkip], Similarity = 0.7)

    private fun skipStory() {
        game.menuStorySkipClick.click()
        0.5.seconds.wait()
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

            1.seconds.wait()
            game.staminaOkClick.click()
            ++stonesUsed

            3.seconds.wait()
        } else if (prefs.waitAPRegen) {
            game.staminaCloseClick.click()
            toast(messages.waitAPToast(1))
            60.seconds.wait()
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
                        (it.value.X - match.Region.center.X).absoluteValue
                    }?.index
                }

            Timber.debug { "Current Party: $currentParty" }

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

        useBoostItem()
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

    private fun makeRefillAndRunsMessage() = buildString {
        val refill = prefs.refill

        val runs = battle.state.runs

        if (refill.shouldLimitRuns && refill.limitRuns > 0) {
            appendLine(messages.timesRanOutOf(runs, refill.limitRuns))
        } else if (runs > 0) {
            appendLine(messages.timesRan(runs))
        }

        if (refill.resources.isNotEmpty()) {
            val refillRepetitions = refill.repetitions
            if (refillRepetitions > 0) {
                appendLine(messages.refillsUsedOutOf(stonesUsed, refillRepetitions))
            }
        }
    }.trimEnd()

    /**
     * Will show a toast informing the user of number of runs and how many apples have been used so far.
     */
    private fun showRefillsAndRunsMessage() {
        val message = makeRefillAndRunsMessage()

        if (message.isNotBlank()) {
            toast(message)
        }
    }

    private fun afterSelectingQuest() {
        1.5.seconds.wait()

        if (isInventoryFull()) {
            throw BattleExitException(ExitReason.InventoryFull)
        }

        // Auto refill
        while (images[Images.Stamina] in game.staminaScreenRegion) {
            refillStamina()
        }
    }
}
