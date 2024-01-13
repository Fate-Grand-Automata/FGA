package io.github.fate_grand_automata.scripts.entrypoints

import io.github.fate_grand_automata.scripts.IFgoAutomataApi
import io.github.fate_grand_automata.scripts.Images
import io.github.fate_grand_automata.scripts.modules.ConnectionRetry
import io.github.lib_automata.EntryPoint
import io.github.lib_automata.ExitManager
import io.github.lib_automata.ScriptAbortException
import io.github.lib_automata.dagger.ScriptScope
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@ScriptScope
class AutoAppend @Inject constructor(
    exitManager: ExitManager,
    api: IFgoAutomataApi,
    private val connectionRetry: ConnectionRetry,
) : EntryPoint(exitManager), IFgoAutomataApi by api {

    sealed class ExitReason {
        data object Abort : ExitReason()
        data object RanOutOfQP : ExitReason()

        data object NoServantSelected : ExitReason()

        class Unexpected(val e: Exception) : ExitReason()

        data object Done : ExitReason()
    }

    sealed class EnhancementExitReason {
        /**
         * When the append have no more QP to use for upgrade
         */
        data object RanOutOfQP : EnhancementExitReason()

        /**
         * When the append have no more mats to use for upgrade
         */
        data object RanOutOfMats : EnhancementExitReason()

        /**
         * When the append is unable to unlock.
         * Might be because of lack of servants coins or probably network lag.
         */
        data object UnableToUnlock : EnhancementExitReason()

        /**
         * When the append is unable to upgrade further.
         * It would be the cause of lacking of mats to upgrade causing the dialog to not appear.
         * @see initializingAppendUpgrade
         */
        data object UnableToUpgradeFurther : EnhancementExitReason()

        /**
         * When the earlier appends have ran out of QP, causing the later appends to not be able to upgrade.
         */
        data object ExitEarlyOutOfQPException : EnhancementExitReason()

        /**
         * When the append is unlocked successfully and there is no upgrade count to perform.
         */
        data object UnlockSuccess : EnhancementExitReason()

        /**
         * When the append is unable to upgrade further due to lag.
         * @see loopUntilAppendBannerVanish
         */
        data object Lag : EnhancementExitReason()

        /**
         * When the append is successfully upgraded.
         */
        data object Success : EnhancementExitReason()

        /**
         * When the append is not selected to be upgraded.
         */
        data object NotSelected : EnhancementExitReason()
    }

    class EnhancementExitException(val reason: EnhancementExitReason) : Exception()
    class AppendException(val reason: ExitReason) : Exception()

    class ExitException(val reason: ExitReason, val state: ExitState) : Exception()

    class Summary(
        val upgradeLevel: Int,
        val shouldUnlock: Boolean,
        val numberOfUpgradePerform: Int? = null,
        val upgradeResult: EnhancementExitException? = null
    )

    class ExitState(val appendSummaryList: List<Summary>)


    var appendUpgradeResultList = mutableListOf<EnhancementExitException?>(null, null, null)

    var appendUpgradeCountList = mutableListOf<Int?>(null, null, null)

    private var doneUnlock = false
    private var upgradeDone = 0

    override fun script(): Nothing {
        try {
            loop()
        } catch (e: AppendException) {
            throw ExitException(reason = e.reason, state = makeExitState())
        } catch (e: ScriptAbortException) {
            throw ExitException(ExitReason.Abort, makeExitState())
        } catch (e: Exception) {
            val reason = ExitReason.Unexpected(e)
            throw ExitException(reason, makeExitState())
        }
    }

    private fun loop(): Nothing {
        if (isServantEmpty()) {
            throw AppendException(ExitReason.NoServantSelected)
        }
        for (appendNumber in 0..2) {
            val upgradeCount = when (appendNumber) {
                0 -> prefs.append.upgradeAppendOne
                1 -> prefs.append.upgradeAppendTwo
                2 -> prefs.append.upgradeAppendThree
                else -> prefs.append.upgradeAppendOne
            }
            val shouldUnlockAppend = when (appendNumber) {
                0 -> prefs.append.appendOneLocked && prefs.append.shouldUnlockAppendOne
                1 -> prefs.append.appendTwoLocked && prefs.append.shouldUnlockAppendTwo
                2 -> prefs.append.appendThreeLocked && prefs.append.shouldUnlockAppendThree
                else -> false
            }

            if (upgradeCount > 0) {
                setupAppendUpgradeLoop(
                    appendNumber = appendNumber,
                    upgradeCount = upgradeCount,
                    shouldUnlock = shouldUnlockAppend
                )
            } else {
                updateAppendSkillResult(
                    appendNumber,
                    EnhancementExitException(EnhancementExitReason.NotSelected)
                )
            }
        }
        throw AppendException(ExitReason.Done)
    }

    private fun setupAppendUpgradeLoop(
        appendNumber: Int,
        upgradeCount: Int,
        shouldUnlock: Boolean
    ) {
        locations.append.lockLocations(appendNumber).click()
        0.5.seconds.wait()

        doneUnlock = false
        upgradeDone = 0

        val screens: Map<() -> Boolean, () -> Unit> = mapOf(
            { connectionRetry.needsToRetry() } to { connectionRetry.retry() },
            { shouldUnlock && !doneUnlock && isInAppendMenu() } to {
                unlockAppend()
            },
            { shouldExitAfterAppendUnlock(shouldUnlock, upgradeCount) } to {
                ExitAfterAppendUnlocked()
            },
            { upgradeDone == upgradeCount && isInAppendMenu() } to {
                throw EnhancementExitException(EnhancementExitReason.Success)
            },
            { isOutOfQP() } to {
                throw EnhancementExitException(EnhancementExitReason.RanOutOfQP)
            },
            { isOutOfMats() } to {
                throw EnhancementExitException(EnhancementExitReason.RanOutOfMats)
            },
            { isOk() } to {
                performAppendUpgrade(appendNumber)
            },
            { upgradeDone < upgradeCount && isInAppendMenu() } to {
                if (shouldUnlock && upgradeDone == 0) {
                    // waiting for animations to be done
                    3.seconds.wait()
                }
                initializingAppendUpgrade()
            }
        )

        while (true) {
            try {
                val actor = useSameSnapIn {
                    screens
                        .asSequence()
                        .filter { (validator, _) -> validator() }
                        .map { (_, actor) -> actor }
                        .firstOrNull()
                } ?: { locations.enhancementSkipRapidClick.click(5) }
                actor.invoke()

                0.5.seconds.wait()
            } catch (e: EnhancementExitException) {
                updateAppendSkillResult(appendNumber, e)
                break
            }
        }
        0.5.seconds.wait()
    }

    /**
     * Perform dual tier waiting for before concluding that the upgrade is done
     * This is to help when there is a lag in the game.
     *
     * First there would be a looping of clicking the ok button until it is gone.
     * Then there would be a waiting for the append banner to be gone.
     */
    private fun performAppendUpgrade(appendNumber: Int) {
        while (true) {
            locations.append.okRegion.click()
            val okGone = locations.append.okRegion.waitVanish(images[Images.Ok], 2.seconds)
            if (okGone) break
        }
        // transition
        0.5.seconds.wait()

        loopUntilAppendBannerVanish()
        upgradeDone++
        updateCurrentAppendNoOfUpgrade(appendNumber, upgradeDone)
    }

    /**
     * Initialize the append upgrade
     * This will retry 2 times until the ok button appears.
     * If the ok button does not appear after 2 times, it will throw [EnhancementExitException]
     * @see EnhancementExitReason.UnableToUpgradeFurther
     *
     * Otherwise, it would be able to proceed to the next step.
     * @see performAppendUpgrade
     *
     */
    private fun initializingAppendUpgrade() {
        val retry = 2
        var canUpgrade = false
        run initUpgrade@{
            repeat(retry) {
                locations.enhancementClick.click()
                val okExist = locations.append.okRegion.exists(images[Images.Ok], timeout = 2.seconds, similarity = 0.7)
                if (okExist) {
                    canUpgrade = true
                    return@initUpgrade
                }
                if (connectionRetry.needsToRetry()) {
                    connectionRetry.retry()
                }
                0.5.seconds.wait()

            }
        }
        if (!canUpgrade) throw EnhancementExitException(EnhancementExitReason.UnableToUpgradeFurther)
    }

    /**
     * Loop until the append banner is gone
     *
     * Will wait for 20 iterations of 3 seconds of checking if the Append Banner Vanishes
     * before throwing [EnhancementExitException]
     */
    private fun loopUntilAppendBannerVanish() {
        var waitIterations = 20
        while (true) {
            val vanish = waitUntilAppendBannerVanish()
            if (vanish) break
            if (connectionRetry.needsToRetry()) {
                connectionRetry.retry()
            }
            if (waitIterations == 0) {
                throw EnhancementExitException(EnhancementExitReason.Lag)
            }
            waitIterations--
            0.5.seconds.wait()
        }
    }

    /**
     * Check if the script should exit after unlocking the append
     */
    private fun shouldExitAfterAppendUnlock(shouldUnlock: Boolean, upgradeCount: Int) =
        shouldUnlock && doneUnlock && isInAppendMenu() && upgradeCount == 0

    /**
     * Exit after unlocking the append but would wait for the animations to be done before doing so.
     */
    private fun ExitAfterAppendUnlocked() {
        // waiting for animations to be done
        3.seconds.wait()
        throw EnhancementExitException(EnhancementExitReason.UnlockSuccess)
    }

    private fun updateAppendSkillResult(appendNumber: Int, result: EnhancementExitException) {
        appendUpgradeResultList[appendNumber] = result
        ifRanOfQPEarlyException(result.reason, appendNumber)
    }

    /**
     * If the append ran out of QP early, it would update the other appends to ran out of QP early as well.
     */
    private fun ifRanOfQPEarlyException(e: EnhancementExitReason, index: Int) {
        if (e != EnhancementExitReason.RanOutOfQP) return
        val exception = EnhancementExitException(EnhancementExitReason.ExitEarlyOutOfQPException)
        when (index) {
            0 -> {
                if (prefs.append.upgradeAppendTwo > 0) {
                    updateAppendSkillResult(
                        1,
                        exception
                    )
                }
                if (prefs.append.upgradeAppendThree > 0) {
                    updateAppendSkillResult(
                        2,
                        exception
                    )
                }
            }

            1 -> {
                if (prefs.append.upgradeAppendThree > 0) {
                    updateAppendSkillResult(
                        2,
                        exception
                    )
                }
            }
        }

        throw AppendException(ExitReason.RanOutOfQP)
    }

    /**
     * Unlock append skill
     *
     * This will retry 2 times if the unlock button is clickable.
     * If the unlock button is not clickable after 2 times, it will throw [EnhancementExitException]
     * @see EnhancementExitReason.UnableToUnlock
     *
     * If the unlock button is clickable, it will click the unlock button and wait for the ok button to appear.
     * It will loop the clicking of button until the ok button is gone. It will also occasionally check if the
     * connection is lost. If the connection is lost, it will retry the clicking of the unlock button.
     *
     * If the ok button is gone, then it will wait until the menu banner is gone
     */
    private fun unlockAppend() {
        val retry = 2
        var unlockSuccess = false
        run unlock@{
            repeat(retry) {
                locations.enhancementClick.click()
                val okExist = locations.append.okRegion.exists(images[Images.Ok], timeout = 2.seconds, similarity = 0.7)
                if (okExist) {
                    unlockSuccess = true
                    return@unlock
                }
                0.5.seconds.wait()
            }
        }
        if (!unlockSuccess) throw EnhancementExitException(EnhancementExitReason.UnableToUnlock)

        doneUnlock = true

        while (true) {
            locations.append.okRegion.click()
            val vanish = locations.append.okRegion.waitVanish(
                images[Images.Ok],
                2.seconds,
                similarity = 0.7
            )
            if (vanish) break
            if (connectionRetry.needsToRetry()) {
                connectionRetry.retry()
            }
            0.5.seconds.wait()
        }
        // transition
        0.5.seconds.wait()

        loopUntilAppendBannerVanish()
    }


    private fun updateCurrentAppendNoOfUpgrade(index: Int, upgradeDone: Int) {
        appendUpgradeCountList[index] = upgradeDone
    }


    private fun makeExitState(): ExitState {
        return ExitState(
            appendSummaryList = listOf(
                Summary(
                    upgradeLevel = prefs.append.upgradeAppendOne,
                    shouldUnlock = prefs.append.shouldUnlockAppendOne,
                    numberOfUpgradePerform = appendUpgradeCountList[0],
                    upgradeResult = appendUpgradeResultList[0]
                ),
                Summary(
                    upgradeLevel = prefs.append.upgradeAppendTwo,
                    shouldUnlock = prefs.append.shouldUnlockAppendTwo,
                    numberOfUpgradePerform = appendUpgradeCountList[1],
                    upgradeResult = appendUpgradeResultList[1]
                ),
                Summary(
                    upgradeLevel = prefs.append.upgradeAppendThree,
                    shouldUnlock = prefs.append.shouldUnlockAppendThree,
                    numberOfUpgradePerform = appendUpgradeCountList[2],
                    upgradeResult = appendUpgradeResultList[2]
                )
            )
        )
    }

    private fun isServantEmpty() = images[Images.EmptyEnhance] in locations.emptyEnhanceRegion

    private fun isOk() = images[Images.Ok] in locations.append.okRegion

    private fun isOutOfQP(): Boolean = images[Images.SkillInsufficientQP] in
            locations.getInsufficientQPRegion

    private fun isOutOfMats(): Boolean = images[Images.SkillInsufficientMaterials] in
            locations.insufficientMaterialsRegion

    private fun isInAppendMenu() = images[Images.AppendBanner] in locations.enhancementBannerRegion

    private fun waitUntilAppendBannerVanish() = locations.enhancementBannerRegion.waitVanish(
        images[Images.AppendBanner],
        3.seconds
    )
}