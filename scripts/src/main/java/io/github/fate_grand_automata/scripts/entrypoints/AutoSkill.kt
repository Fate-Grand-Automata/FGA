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
        /**
         * The script was aborted by the user.
         */
        data object Abort : ExitReason()

        /**
         * The script ran out of QP.
         */
        data object RanOutOfQP : ExitReason()

        /**
         * No servant was selected.
         */
        data object NoServantSelected : ExitReason()

        /**
         * An unexpected exception was thrown.
         */
        class Unexpected(val e: Exception) : ExitReason()

        /**
         * The script finished successfully.
         */
        data object Done : ExitReason()
    }

    /**
     * The current script Exception
     */
    class SkillUpgradeException(val reason: ExitReason) : Exception()

    class ExitException(val reason: ExitReason, val state: ExitState) : Exception()


    sealed class EnhancementExitReason {
        /**
         * The script ran out of materials.
         */
        data object OutOfMatsException : EnhancementExitReason()

        /**
         * The script ran out of QP.
         */
        data object OutOfQPException : EnhancementExitReason()

        /**
         * The script exited early because it ran out of QP in previous skills.
         */

        data object ExitEarlyOutOfQPException : EnhancementExitReason()

        /**
         * The script reached the target level.
         */
        data object TargetLevelMet : EnhancementExitReason()
        /**
         * The script exited early because no skill was selected.
         */
        data object NoSkillUpgradeError : EnhancementExitReason()
    }

    /**
     * Individually, each skill can exit with one of these reasons.
     */
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

    private var skillCountList: MutableList<Int?> = mutableListOf(null, null, null)

    private var upgradeResultList: MutableList<EnhancementException?> = mutableListOf(null, null, null)

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

        for (skillNumber in 1..3){
            val shouldUpgrade = when(skillNumber){
                1 -> skillUpgrade.shouldUpgradeSkill1
                2 -> skillUpgrade.shouldUpgradeSkill2
                3 -> skillUpgrade.shouldUpgradeSkill3
                else -> false
            }
            val canUpgrade = when(skillNumber){
                1 -> skillUpgrade.upgradeSkill1 > 0
                2 -> skillUpgrade.upgradeSkill2 > 0
                3 -> skillUpgrade.upgradeSkill3 > 0
                else -> false
            }
            if(shouldUpgrade){
                if(canUpgrade){
                    setupSkillUpgradeLoop(skillNumber = skillNumber)
                } else {
                    updateSkillUpgradeResult(EnhancementExitReason.NoSkillUpgradeError, skillNumber)
                }
                ifRanOfQPEarlyException(
                    e = upgradeResultList[skillNumber - 1]?.reason,
                    index = skillNumber - 1
                )
            }
        }

        throw SkillUpgradeException(ExitReason.Done)
    }

    private fun setupSkillUpgradeLoop(
        skillNumber: Int,
    ) {
        val skillLocation = locations.skill.skillLocation(skillNumber)
        val skillRegion = locations.skill.skillTextRegion(skillNumber)
        val targetLevel = currentTargetSkillLevel(skillNumber)

        skillLocation.click(2)
        1.0.seconds.wait()

        val screens: Map<() -> Boolean, () -> Unit> = mapOf(
            { connectionRetry.needsToRetry() } to { connectionRetry.retry() },
            // case when all skills are level up returning to the main menu
            { isServantEmpty() } to {
                exitEnhancementLoopAsAllSkillsAreMaxedOut(skillNumber = skillNumber)
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

    /**
     * This special function used to exit the skill upgrade loop as all skills are maxed out
     * @param skillNumber the index of the skill
     */
    private fun exitEnhancementLoopAsAllSkillsAreMaxedOut(skillNumber: Int) {
        updateCurrentSkillLevel(level = 10, index = skillNumber)
        throw EnhancementException(EnhancementExitReason.TargetLevelMet)
    }

    /**
     * This function is used to perform the current skill upgrade loop
     * @param currentSkillScreen the current skill screen
     * @param skillNumber the index of the skill
     */
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


    /**
     * This function is used to check if the target level is met
     * @param region the region of the skill
     * @param targetLevel the target level of the skill
     * @param skillNumber the index of the skill
     * @return true if the target level is met
     */
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

    /**
     * This function is used to execute the skill upgrade
     */
    private fun executeUpgradeSkill() {
        locations.skill.confirmationDialogRegion.click()
        1.0.seconds.wait()
    }

    /**
     * This function is used to update the skill upgrade result
     * @param e the exception that is thrown
     * @param index the index of the skill
     */
    private fun updateSkillUpgradeResult(e: EnhancementExitReason, index: Int) {
        upgradeResultList[index - 1] = EnhancementException(e)
    }

    private fun currentTargetSkillLevel(index: Int) = when (index) {
        1 -> prefs.skillUpgrade.minSkill1 + prefs.skillUpgrade.upgradeSkill1
        2 -> prefs.skillUpgrade.minSkill2 + prefs.skillUpgrade.upgradeSkill2
        3 -> prefs.skillUpgrade.minSkill3 + prefs.skillUpgrade.upgradeSkill3
        else -> prefs.skillUpgrade.minSkill1 + prefs.skillUpgrade.upgradeSkill1
    }

    /**
     * This function is used to update the skill upgrade result if the skill upgrade is ran out of QP early
     * @param e the exception that is thrown
     * @param index the index of the skill
     */
    private fun ifRanOfQPEarlyException(e: EnhancementExitReason?, index: Int) {
        if (e != EnhancementExitReason.OutOfQPException) return
        val skillUpgrade = prefs.skillUpgrade
        val exitEarlyException = EnhancementException(EnhancementExitReason.ExitEarlyOutOfQPException)
        when (index) {
            1 -> {
                if (skillUpgrade.shouldUpgradeSkill2) upgradeResultList[1] = exitEarlyException
                if (skillUpgrade.shouldUpgradeSkill3) upgradeResultList[2] = exitEarlyException
            }

            2 -> if (skillUpgrade.shouldUpgradeSkill3) upgradeResultList[2] = exitEarlyException
        }
        throw SkillUpgradeException(ExitReason.RanOutOfQP)
    }

    private fun updateCurrentSkillLevel(level: Int, index: Int) {
        skillCountList[index - 1] = level
    }

    /**
     * This function is used to check if the skill will be upgraded
     * @param targetLevel the target level of the skill
     * @param index the index of the skill
     * @return true if the skill will be upgraded
     */
    private fun checkIfWillUpgradeSkill(targetLevel: Int, index: Int): Boolean {
        if (isConfirmationDialog() || isTemporaryServant()) return false
        val skillCount = skillCountList[index - 1]
        val skillCountConditionMet = skillCount?.let { it < targetLevel } ?: false

        return skillCountConditionMet && isInSkillEnhancementMenu()
    }


    private fun isOutOfMats(): Boolean = images[Images.SkillInsufficientMaterials] in
            locations.skill.insufficientMaterialsRegion

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
                enhancementExitReason = upgradeResultList[0],
                startingLevel = prefs.skillUpgrade.minSkill1,
                endLevel = skillCountList[0],
                targetLevel = when (prefs.skillUpgrade.upgradeSkill1 > 0) {
                    true -> prefs.skillUpgrade.minSkill1 + prefs.skillUpgrade.upgradeSkill1
                    false -> null
                }
            ), skill2Summary = Summary(
                isCheckToUpgrade = prefs.skillUpgrade.shouldUpgradeSkill2,
                isAvailable = prefs.skillUpgrade.skill2Available,
                enhancementExitReason = upgradeResultList[1],
                startingLevel = if (prefs.skillUpgrade.skill2Available) prefs.skillUpgrade.minSkill2 else null,
                endLevel = skillCountList[1],
                targetLevel = when (prefs.skillUpgrade.upgradeSkill2 > 0) {
                    true -> prefs.skillUpgrade.minSkill2 + prefs.skillUpgrade.upgradeSkill2
                    false -> null
                },
            ),
            skill3Summary = Summary(
                isCheckToUpgrade = prefs.skillUpgrade.shouldUpgradeSkill3,
                isAvailable = prefs.skillUpgrade.skill3Available,
                enhancementExitReason = upgradeResultList[2],
                startingLevel = if (prefs.skillUpgrade.skill3Available) prefs.skillUpgrade.minSkill3 else null,
                endLevel = skillCountList[2],
                targetLevel = when (prefs.skillUpgrade.upgradeSkill3 > 0) {
                    true -> prefs.skillUpgrade.minSkill3 + prefs.skillUpgrade.upgradeSkill3
                    false -> null
                }
            )
        )
    }

}