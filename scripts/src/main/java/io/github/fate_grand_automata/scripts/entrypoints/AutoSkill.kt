package io.github.fate_grand_automata.scripts.entrypoints

import io.github.fate_grand_automata.scripts.IFgoAutomataApi
import io.github.fate_grand_automata.scripts.Images
import io.github.fate_grand_automata.scripts.modules.ConnectionRetry
import io.github.lib_automata.EntryPoint
import io.github.lib_automata.ExitManager
import io.github.lib_automata.Region
import io.github.lib_automata.ScriptAbortException
import io.github.lib_automata.dagger.ScriptScope
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@ScriptScope
class AutoSkill @Inject constructor(
    private val connectionRetry: ConnectionRetry,
    exitManager: ExitManager,
    api: IFgoAutomataApi
) : EntryPoint(exitManager), IFgoAutomataApi by api {


    sealed class ExitReason {

        data object Abort : ExitReason()
        data object RanOutOfQP : ExitReason()

        data object NoServantSelected : ExitReason()

        class Unexpected(val e: Exception) : ExitReason()

        data object Done : ExitReason()
    }

    class SkillUpgradeException(val reason: ExitReason) : Exception()

    class ExitException(val reason: ExitReason, val state: ExitState) : Exception()


    sealed class EnhancementExitReason {
        data object OutOfMatsException : EnhancementExitReason()
        data object OutOfQPException : EnhancementExitReason()

        data object ExitEarlyOutOfQPException : EnhancementExitReason()

        data object TargetLevelMet : EnhancementExitReason()

        data object NoSkillUpgradeError : EnhancementExitReason()
    }

    class EnhancementException(val reason: EnhancementExitReason) : Exception()

    class Summary(
        val isCheckToUpgrade: Boolean,
        val isAvailable: Boolean,
        val enhancementExitReason: EnhancementException? = null,
        val startingLevel: Int? = null,
        val endLevel: Int? = null,
        val targetLevel: Int? = null
    )

    class ExitState(
        val skill1Summary: Summary,
        val skill2Summary: Summary,
        val skill3Summary: Summary
    )


    private var skill1count: Int? = null
    private var skill2count: Int? = null
    private var skill3count: Int? = null

    var skill1UpgradeResult: EnhancementException? = null
    var skill2UpgradeResult: EnhancementException? = null
    var skill3UpgradeResult: EnhancementException? = null

    private var skill1Available = true


    override fun script(): Nothing {
        try {
            skillUpgrade()
        } catch (e: SkillUpgradeException) {
            throw ExitException(reason = e.reason, state = makeExitState())
        } catch (e: ScriptAbortException) {
            throw ExitException(ExitReason.Abort, makeExitState())
        } catch (e: Exception) {
            val reason = ExitReason.Unexpected(e)
            throw ExitException(reason, makeExitState())
        }
    }


    private fun skillUpgrade(): Nothing {
        if (isServantEmpty()) {
            skill1Available = false
            throw SkillUpgradeException(ExitReason.NoServantSelected)
        }
        val skillUpgrade = prefs.skillUpgrade

        if (skillUpgrade.shouldUpgradeSkill1) {
            if (skillUpgrade.upgradeSkill1 > 0) {
                setupSkillUpgradeLoop(
                    skillNumber = 1,
                )
            } else {
                updateSkillUpgradeResult(EnhancementExitReason.NoSkillUpgradeError, 1)
            }

            ifRanOfQPEarlyException(
                e = skill1UpgradeResult?.reason,
                index = 1
            )
        }
        if (skillUpgrade.shouldUpgradeSkill2) {
            if (skillUpgrade.upgradeSkill2 > 0) {
                setupSkillUpgradeLoop(
                    skillNumber = 2,
                )
            } else {
                updateSkillUpgradeResult(EnhancementExitReason.NoSkillUpgradeError, 2)
            }
            ifRanOfQPEarlyException(
                e = skill2UpgradeResult?.reason,
                index = 1
            )
        }
        if (skillUpgrade.shouldUpgradeSkill3) {
            if (skillUpgrade.upgradeSkill3 > 0) {
                setupSkillUpgradeLoop(
                    skillNumber = 3,
                )
            } else {
                updateSkillUpgradeResult(EnhancementExitReason.NoSkillUpgradeError, 3)
            }
            ifRanOfQPEarlyException(
                e = skill3UpgradeResult?.reason,
                index = 3
            )
        }
        throw SkillUpgradeException(ExitReason.Done)
    }

    private fun setupSkillUpgradeLoop(
        skillNumber: Int,
    ) {
        val skillLocation = locations.skill.skillLocation(skillNumber)
        val skillRegion = currentSkillTextRegion(skillNumber)
        val targetLevel = currentTargetSkillLevel(skillNumber)

        skillLocation.click(2)
        1.0.seconds.wait()

        val screens: Map<() -> Boolean, () -> Unit> = mapOf(
            { connectionRetry.needsToRetry() } to { connectionRetry.retry() },
            // case when all skills are level up returning to the main menu
            { isServantEmpty() } to {
                exitEnhancementLoopAsAllSkillsAreMaxedOut(skillNumber=skillNumber)
            },
            {
                isTheTargetLevelConditionMet(
                    region = skillRegion,
                    targetLevel = targetLevel,
                    skillNumber = skillNumber
                )
            } to { throw EnhancementException(EnhancementExitReason.TargetLevelMet) },
            { isTemporaryServant() } to { locations.tempServantEnhancementLocation.click() },
            { isConfirmationDialog() } to { executeUpgradeSkill() },
            { isOutOfMats() } to { throw EnhancementException(EnhancementExitReason.OutOfMatsException) },
            { isOutOfQP() } to { throw EnhancementException(EnhancementExitReason.OutOfQPException) },
            {
                checkIfWillUpgradeSkill(targetLevel = targetLevel, index = skillNumber)
            } to { locations.enhancementClick.click() },
        )

        performSkillUpgradeLoop(
            currentSkillScreen = screens,
            skillNumber = skillNumber
        )
    }

    private fun exitEnhancementLoopAsAllSkillsAreMaxedOut(skillNumber: Int) {
        updateCurrentSkillLevel(level = 10, index = skillNumber)
        throw EnhancementException(EnhancementExitReason.TargetLevelMet)
    }

    private fun performSkillUpgradeLoop(
        currentSkillScreen: Map<() -> Boolean, () -> Unit>,
        skillNumber: Int,
    ) {
        while (true) {
            try {
                val actor = useSameSnapIn {
                    currentSkillScreen
                        .asSequence()
                        .filter { (validator, _) -> validator() }
                        .map { (_, actor) -> actor }
                        .firstOrNull()
                } ?: { locations.enhancementSkipRapidClick.click(5) }
                actor.invoke()

                0.5.seconds.wait()
            } catch (e: EnhancementException) {
                updateSkillUpgradeResult(e.reason, skillNumber)
                break
            }
        }
    }


    private fun isTheTargetLevelConditionMet(
        region: Region,
        targetLevel: Int,
        skillNumber: Int,
    ): Boolean {
        if (isConfirmationDialog()) return false

        val currentLevel = region.findNumberInText() ?: return false

        updateCurrentSkillLevel(level = currentLevel, index = skillNumber)
        val checkIfIsInSkillEnhancementMenu = isInSkillEnhancementMenu()

        return targetLevel <= currentLevel && checkIfIsInSkillEnhancementMenu
    }

    private fun executeUpgradeSkill() {
        locations.skill.confirmationDialogRegion.click()
        1.0.seconds.wait()
    }

    private fun updateSkillUpgradeResult(e: EnhancementExitReason, index: Int) {
        when (index) {
            1 -> skill1UpgradeResult = EnhancementException(e)
            2 -> skill2UpgradeResult = EnhancementException(e)
            3 -> skill3UpgradeResult = EnhancementException(e)
            else -> skill1UpgradeResult = EnhancementException(e)
        }
    }

    private fun currentSkillTextRegion(index: Int) = when (index) {
        1 -> locations.skill.skill1TextRegion
        2 -> locations.skill.skill2TextRegion
        3 -> locations.skill.skill3TextRegion
        else -> locations.skill.skill1TextRegion
    }

    private fun currentTargetSkillLevel(index: Int) = when (index) {
        1 -> prefs.skillUpgrade.minSkill1 + prefs.skillUpgrade.upgradeSkill1
        2 -> prefs.skillUpgrade.minSkill2 + prefs.skillUpgrade.upgradeSkill2
        3 -> prefs.skillUpgrade.minSkill3 + prefs.skillUpgrade.upgradeSkill3
        else -> prefs.skillUpgrade.minSkill1 + prefs.skillUpgrade.upgradeSkill1
    }


    private fun ifRanOfQPEarlyException(e: EnhancementExitReason?, index: Int) {
        if (e != EnhancementExitReason.OutOfQPException) return
        when (index) {
            1 -> {
                if (prefs.skillUpgrade.shouldUpgradeSkill2) {
                    skill2UpgradeResult = EnhancementException(EnhancementExitReason.ExitEarlyOutOfQPException)
                }
                if (prefs.skillUpgrade.shouldUpgradeSkill3) {
                    skill3UpgradeResult = EnhancementException(EnhancementExitReason.ExitEarlyOutOfQPException)
                }
            }

            2 -> {
                if (prefs.skillUpgrade.shouldUpgradeSkill3) {
                    skill3UpgradeResult = EnhancementException(EnhancementExitReason.ExitEarlyOutOfQPException)
                }
            }
        }
        throw SkillUpgradeException(ExitReason.RanOutOfQP)
    }

    private fun updateCurrentSkillLevel(level: Int, index: Int) {
        when (index) {
            1 -> skill1count = level
            2 -> skill2count = level
            3 -> skill3count = level
            else -> skill1count = level
        }
    }

    private fun checkIfWillUpgradeSkill(targetLevel: Int, index: Int): Boolean {
        if (isConfirmationDialog() || isTemporaryServant()) return false

        return when (index) {
            1 -> skill1count?.let {
                it < targetLevel
            } ?: false

            2 -> skill2count?.let {
                it < targetLevel
            } ?: false

            3 -> skill3count?.let {
                it < targetLevel
            } ?: false

            else -> skill1count?.let {
                it < targetLevel
            } ?: false
        } && isInSkillEnhancementMenu()
    }


    private fun isOutOfMats(): Boolean = images[Images.SkillInsufficientMaterials] in
            locations.skill.getInsufficientMatsRegion

    private fun isConfirmationDialog() = images[Images.Ok] in
            locations.skill.confirmationDialogRegion

    private fun isInSkillEnhancementMenu() = images[Images.SkillMenuBanner] in
            locations.enhancementBannerRegion

    private fun isOutOfQP(): Boolean = images[Images.SkillInsufficientQP] in
            locations.getInsufficientQPRegion

    private fun isServantEmpty() = images[Images.EmptyEnhance] in locations.emptyEnhanceRegion

    private fun isTemporaryServant() = images[Images.Execute] in locations.tempServantEnhancementRegion

    private fun makeExitState(): ExitState {
        return ExitState(
            skill1Summary = Summary(
                isCheckToUpgrade = prefs.skillUpgrade.shouldUpgradeSkill1,
                isAvailable = skill1Available,
                enhancementExitReason = skill1UpgradeResult,
                startingLevel = prefs.skillUpgrade.minSkill1,
                endLevel = skill1count,
                targetLevel = when (prefs.skillUpgrade.upgradeSkill1 > 0) {
                    true -> prefs.skillUpgrade.minSkill1 + prefs.skillUpgrade.upgradeSkill1
                    false -> null
                }
            ), skill2Summary = Summary(
                isCheckToUpgrade = prefs.skillUpgrade.shouldUpgradeSkill2,
                isAvailable = prefs.skillUpgrade.skill2Available,
                enhancementExitReason = skill2UpgradeResult,
                startingLevel = if (prefs.skillUpgrade.skill2Available) prefs.skillUpgrade.minSkill2 else null,
                endLevel = skill2count,
                targetLevel = when (prefs.skillUpgrade.upgradeSkill2 > 0) {
                    true -> prefs.skillUpgrade.minSkill2 + prefs.skillUpgrade.upgradeSkill2
                    false -> null
                },
            ),
            skill3Summary = Summary(
                isCheckToUpgrade = prefs.skillUpgrade.shouldUpgradeSkill3,
                isAvailable = prefs.skillUpgrade.skill3Available,
                enhancementExitReason = skill3UpgradeResult,
                startingLevel = if (prefs.skillUpgrade.skill3Available) prefs.skillUpgrade.minSkill3 else null,
                endLevel = skill3count,
                targetLevel = when (prefs.skillUpgrade.upgradeSkill3 > 0) {
                    true -> prefs.skillUpgrade.minSkill3 + prefs.skillUpgrade.upgradeSkill3
                    false -> null
                }
            )


        )
    }

}