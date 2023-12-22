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

    var append1UpgradeResult: EnhancementExitException? = null
    var append2UpgradeResult: EnhancementExitException? = null
    var append3UpgradeResult: EnhancementExitException? = null

    var append1NumberOfUpgrade: Int? = null
    var append2NumberOfUpgrade: Int? = null
    var append3NumberOfUpgrade: Int? = null
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
        for (i in 0..2) {
            val toUpgradeLevel = getAppendUpgradeLevel(i)
            if (isAppendLockedStatus(i) && getShouldUnlockAppendStatus(i)) {
                appendUpgrade(i, toUpgradeLevel, true)
            } else {
                if (toUpgradeLevel > 0) {
                    appendUpgrade(i, toUpgradeLevel, false)
                } else {
                    updateAppendSkillResult(i, EnhancementExitException(EnhancementExitReason.NotSelected))
                }
            }
        }
        throw AppendException(ExitReason.Done)
    }

    private fun appendUpgrade(
        index: Int,
        upgradeLevel: Int,
        shouldUnlock: Boolean
    ) {
        locations.append.lockLocations(index).click()
        0.5.seconds.wait()

        var doneUnlock = false
        var upgradeDone = 0

        val screens: Map<() -> Boolean, () -> Unit> = mapOf(
            { connectionRetry.needsToRetry() } to { connectionRetry.retry() },
            { shouldUnlock && !doneUnlock } to {
                unlockAppend()
                doneUnlock = true
                waitUntilMenuVanish()
            },
            { shouldUnlock && doneUnlock && isInAppendMenu() && upgradeLevel == 0 } to {
                // waiting for animations to be done
                3.seconds.wait()
                throw EnhancementExitException(EnhancementExitReason.UnlockSuccess)
            },
            { upgradeDone == upgradeLevel && isInAppendMenu() } to {
                throw EnhancementExitException(EnhancementExitReason.Success)
            },
            { isOutOfQP() } to { throw EnhancementExitException(EnhancementExitReason.RanOutOfQP) },
            { isOutOfMats() } to { throw EnhancementExitException(EnhancementExitReason.RanOutOfMats) },
            { isOk() } to {
                while (true) {
                    locations.append.okRegion.click()
                    val okGone = locations.append.okRegion.waitVanish(images[Images.Ok], 2.seconds)
                    if (okGone) {
                        upgradeDone++
                        updateCurrentAppendNoOfUpgrade(index, upgradeDone)
                        break
                    }
                }
                waitUntilMenuVanish()
            },
            { upgradeDone < upgradeLevel && isInAppendMenu() } to {
                if (shouldUnlock && upgradeDone == 0){
                    // waiting for animations to be done
                    3.seconds.wait()
                }
                performAppendUpgrade()
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
                updateAppendSkillResult(index, e)
                break
            }
        }
        0.5.seconds.wait()
    }

    private fun updateAppendSkillResult(index: Int, result: EnhancementExitException) {
        when (index) {
            0 -> append1UpgradeResult = result
            1 -> append2UpgradeResult = result
            2 -> append3UpgradeResult = result
            else -> append1UpgradeResult = result
        }
        ifRanOfQPEarlyException(result.reason, index)
    }

    private fun ifRanOfQPEarlyException(e: EnhancementExitReason, index: Int) {
        if (e != EnhancementExitReason.RanOutOfQP) return
        when (index) {
            0 -> {
                if (prefs.append.upgradeAppend2 > 0) {
                    updateAppendSkillResult(
                        1,
                        EnhancementExitException(EnhancementExitReason.ExitEarlyOutOfQPException)
                    )
                }
                if (prefs.append.upgradeAppend3 > 0) {
                    updateAppendSkillResult(
                        2,
                        EnhancementExitException(EnhancementExitReason.ExitEarlyOutOfQPException)
                    )
                }
            }

            1 -> {
                if (prefs.append.upgradeAppend3 > 0) {
                    updateAppendSkillResult(
                        2,
                        EnhancementExitException(EnhancementExitReason.ExitEarlyOutOfQPException)
                    )
                }
            }
        }

        throw AppendException(ExitReason.RanOutOfQP)
    }

    private fun unlockAppend() {
        val end = 2
        for (i in 0..end) {
            locations.enhancementClick.click()
            val okExist = locations.append.okRegion.exists(images[Images.Ok], timeout = 2.seconds, similarity = 0.7)
            when {
                okExist -> {
                    locations.append.okRegion.click()
                    break
                }

                !okExist && i != end -> 0.5.seconds.wait()
                else -> throw EnhancementExitException(EnhancementExitReason.UnableToUnlock)
            }
            if (connectionRetry.needsToRetry()) {
                connectionRetry.retry()
            }
        }

    }

    private fun performAppendUpgrade() {
        val numberOfRetry = 2
        for (i in 0..numberOfRetry) {
            locations.enhancementClick.click()
            val okExist = locations.append.okRegion.exists(images[Images.Ok], timeout = 2.seconds, similarity = 0.7)
            when {
                okExist -> break
                !okExist && i != numberOfRetry -> 0.5.seconds.wait()
                else -> throw EnhancementExitException(EnhancementExitReason.UnableToUpgradeFurther)
            }
            if (connectionRetry.needsToRetry()) {
                connectionRetry.retry()
            }
        }
    }

    private fun updateCurrentAppendNoOfUpgrade(index: Int, upgradeDone: Int) {
        when (index) {
            0 -> append1NumberOfUpgrade = upgradeDone
            1 -> append2NumberOfUpgrade = upgradeDone
            2 -> append3NumberOfUpgrade = upgradeDone
            else -> append1NumberOfUpgrade = upgradeDone
        }
    }

    private fun getShouldUnlockAppendStatus(index: Int): Boolean {
        return when (index) {
            0 -> prefs.append.shouldUnlockAppend1
            1 -> prefs.append.shouldUnlockAppend2
            2 -> prefs.append.shouldUnlockAppend3
            else -> prefs.append.shouldUnlockAppend1
        }
    }

    private fun isAppendLockedStatus(index: Int): Boolean {
        return when (index) {
            0 -> prefs.append.isAppend1Locked
            1 -> prefs.append.isAppend2Locked
            2 -> prefs.append.isAppend3Locked
            else -> prefs.append.isAppend1Locked
        }
    }

    private fun getAppendUpgradeLevel(index: Int): Int {
        return when (index) {
            0 -> prefs.append.upgradeAppend1
            1 -> prefs.append.upgradeAppend2
            2 -> prefs.append.upgradeAppend3
            else -> prefs.append.upgradeAppend1
        }
    }

    sealed class ExitReason {
        data object Abort : ExitReason()
        data object RanOutOfQP : ExitReason()

        data object NoServantSelected : ExitReason()

        class Unexpected(val e: Exception) : ExitReason()

        data object Done : ExitReason()
    }

    sealed class EnhancementExitReason {
        data object RanOutOfQP : EnhancementExitReason()

        data object RanOutOfMats : EnhancementExitReason()

        data object UnableToUnlock : EnhancementExitReason()

        data object UnableToUpgradeFurther : EnhancementExitReason()

        data object ExitEarlyOutOfQPException : EnhancementExitReason()
        data object UnlockSuccess : EnhancementExitReason()

        data object Success : EnhancementExitReason()

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

    class ExitState(
        val append1: Summary,
        val append2: Summary,
        val append3: Summary
    )

    private fun makeExitState(): ExitState {
        return ExitState(
            append1 = Summary(
                upgradeLevel = prefs.append.upgradeAppend1,
                shouldUnlock = prefs.append.shouldUnlockAppend1,
                numberOfUpgradePerform = append1NumberOfUpgrade,
                upgradeResult = append1UpgradeResult
            ),
            append2 = Summary(
                upgradeLevel = prefs.append.upgradeAppend2,
                shouldUnlock = prefs.append.shouldUnlockAppend2,
                numberOfUpgradePerform = append2NumberOfUpgrade,
                upgradeResult = append2UpgradeResult
            ),
            append3 = Summary(
                upgradeLevel = prefs.append.upgradeAppend3,
                shouldUnlock = prefs.append.shouldUnlockAppend3,
                numberOfUpgradePerform = append3NumberOfUpgrade,
                upgradeResult = append3UpgradeResult
            )
        )
    }

    private fun isServantEmpty() = images[Images.EmptyEnhance] in locations.emptyEnhanceRegion

    private fun isOk() = images[Images.Ok] in locations.append.okRegion

    private fun isOutOfQP(): Boolean = images[Images.SkillInsufficientQP] in
            locations.getInsufficientQPRegion

    private fun isOutOfMats(): Boolean = images[Images.SkillInsufficientMaterials] in
            locations.skillUpgrade.getInsufficientMatsRegion

    private fun isInAppendMenu() = images[Images.AppendSkill] in locations.append.getAppendBannerRegion

    private fun waitUntilMenuVanish() = locations.append.getAppendBannerRegion.waitVanish(
        images[Images.AppendSkill],
        3.seconds
    )
}