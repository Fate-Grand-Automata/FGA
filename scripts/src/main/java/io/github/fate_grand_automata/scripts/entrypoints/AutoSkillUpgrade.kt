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
class AutoSkillUpgrade @Inject constructor(
    private val connectionRetry: ConnectionRetry,
    exitManager: ExitManager,
    api: IFgoAutomataApi
) : EntryPoint(exitManager), IFgoAutomataApi by api {

    /**
     * This is the overall exit reason of the script.
     */
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

    /**
     * The current script exit Exception
     * @param reason the reason why the script exited
     * @param state the current state of the script
     * @see ExitState
     * @see ExitReason
     * @see Summary
     * @see EnhancementExitReason
     */

    class ExitException(val reason: ExitReason, val state: ExitState) : Exception()

    /**
     * This is the reason why the skill exited.
     */
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

    /**
     * The summary of the skill upgrade.
     * @param isCheckToUpgrade whether the skill is checked to upgrade
     * @param isAvailable whether the skill is available
     * This is applicable to skill two and three only. While skill one is always available.
     *
     * @param enhancementExitReason the reason why the skill exited
     * This gives the result of the enhancement.
     *
     * @param startingLevel the starting level of the skill.
     * This is the minimum level of the skill.
     *
     * @param endLevel the ending level of the skill
     * This is the current level of the skill.
     * This is added due to aborting of the script.
     * So, the script can return the current level of the skill.
     *
     * @param targetLevel the target level of the skill
     */
    class Summary(
        val isCheckToUpgrade: Boolean,
        val isAvailable: Boolean,
        val enhancementExitReason: EnhancementException? = null,
        val startingLevel: Int? = null,
        val endLevel: Int? = null,
        val targetLevel: Int? = null
    )

    class ExitState(val skillSummaryList: List<Summary>)

    /**
     * This list is used to store the current skill level
     */

    private var skillCountList: MutableList<Int?> = mutableListOf(null, null, null)

    /**
     * This list is used to store the skill upgrade result via EnhancementException
     * @see EnhancementException
     */

    private var upgradeResultList: MutableList<EnhancementException?> = mutableListOf(null, null, null)
    
    /**
     * This variable is used to store the skill one availability
     * While skill one is always available. This is added if there is no
     * servant selected. So, the script can return the correct result.
     */

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
        val skillUpgrade = prefs.skill

        for (skillNumber in 1..3) {
            val shouldUpgrade = when (skillNumber) {
                1 -> skillUpgrade.shouldUpgradeSkillOne
                2 -> skillUpgrade.shouldUpgradeSkillTwo
                3 -> skillUpgrade.shouldUpgradeSkillThree
                else -> false
            }
            val canUpgrade = when (skillNumber) {
                1 -> skillUpgrade.skillOneUpgradeValue > 0
                2 -> skillUpgrade.skillTwoUpgradeValue > 0
                3 -> skillUpgrade.skillThreeUpgradeValue > 0
                else -> false
            }
            if (shouldUpgrade) {
                if (canUpgrade) {
                    setupSkillUpgradeLoop(skillNumber = skillNumber)
                } else {
                    setSkillUpgradeException(EnhancementExitReason.NoSkillUpgradeError, skillNumber)
                }
                ifRanOfQPEarlyException(
                    e = upgradeResultList[skillNumber - 1]?.reason,
                    skillNumber = skillNumber - 1
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
        val targetLevel = determineTargetSkillLevel(skillNumber)

        skillLocation.click(2)
        1.0.seconds.wait()

        val screens: Map<() -> Boolean, () -> Unit> = mapOf(
            { connectionRetry.needsToRetry() } to { connectionRetry.retry() },
            // case when all skills are level up returning to the main menu
            { isServantEmpty() } to {
                terminateEnhancementWhenSkillsMaxed(skillNumber = skillNumber)
            },
            { isTemporaryServant() } to { locations.tempServantEnhancementLocation.click() },
            { isConfirmationDialogVisible() } to { executeUpgradeSkill() },
            {
                isTheTargetLevelConditionMet(
                    region = skillRegion,
                    targetLevel = targetLevel,
                    skillNumber = skillNumber
                )
            } to { throw EnhancementException(EnhancementExitReason.TargetLevelMet) },
            { isOutOfMats() } to { throw EnhancementException(EnhancementExitReason.OutOfMatsException) },
            { isOutOfQP() } to { throw EnhancementException(EnhancementExitReason.OutOfQPException) },
            {
                verifySkillUpgradeEligibility(targetLevel = targetLevel, skillNumber = skillNumber)
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
    private fun terminateEnhancementWhenSkillsMaxed(skillNumber: Int) {
        updateCurrentSkillLevel(level = 10, skillNumber = skillNumber)
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
                setSkillUpgradeException(e.reason, skillNumber)
                break
            }
        }
    }


    /**
     * This function is used to check if the target level is met
     *
     * If the confirmation dialog is visible, you can't accurately read the skill level.
     * So, we need to check if the confirmation dialog is visible first.
     *
     * Afterwards, we can check if the target level is met using OCR.
     * If cannot read, then most likely we are in the middle of enhancing the skill.
     *
     * If the target level is met, we need to check if the script is in the skill enhancement menu.
     * to prevent stray detection
     *
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
        if (isConfirmationDialogVisible()) return false

        val currentLevel = region.findNumberInText(regexPattern = """([1-9]|10)(?:/1|/10|710|71)?""") ?: return false

        updateCurrentSkillLevel(level = currentLevel, skillNumber = skillNumber)
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
     * @param skillNumber the index of the skill
     */
    private fun setSkillUpgradeException(e: EnhancementExitReason, skillNumber: Int) {
        upgradeResultList[skillNumber - 1] = EnhancementException(e)
    }

    /**
     * This function is used to determine the current skill level
     * @param skillNumber the index of the skill
     * @return the current skill level
     */
    private fun determineTargetSkillLevel(skillNumber: Int) = when (skillNumber) {
        1 -> prefs.skill.minimumSkillOne + prefs.skill.skillOneUpgradeValue
        2 -> prefs.skill.minimumSkillTwo + prefs.skill.skillTwoUpgradeValue
        3 -> prefs.skill.minimumSkillThree + prefs.skill.skillThreeUpgradeValue
        else -> prefs.skill.minimumSkillOne + prefs.skill.skillOneUpgradeValue
    }

    /**
     * This function is used to update the skill upgrade result if the skill upgrade is ran out of QP early
     * @param e the exception that is thrown
     * @param skillNumber the index of the skill
     */
    private fun ifRanOfQPEarlyException(e: EnhancementExitReason?, skillNumber: Int) {
        if (e != EnhancementExitReason.OutOfQPException) return
        val skillUpgrade = prefs.skill
        val exitEarlyException = EnhancementException(EnhancementExitReason.ExitEarlyOutOfQPException)
        when (skillNumber) {
            1 -> {
                if (skillUpgrade.shouldUpgradeSkillTwo) upgradeResultList[1] = exitEarlyException
                if (skillUpgrade.shouldUpgradeSkillThree) upgradeResultList[2] = exitEarlyException
            }

            2 -> if (skillUpgrade.shouldUpgradeSkillThree) upgradeResultList[2] = exitEarlyException
        }
        throw SkillUpgradeException(ExitReason.RanOutOfQP)
    }

    private fun updateCurrentSkillLevel(level: Int, skillNumber: Int) {
        skillCountList[skillNumber - 1] = level
    }

    /**
     * This function is used to check if the skill will be upgraded.
     * This is in tandem with @see isTheTargetLevelConditionMet
     * where the first function would read the current skill level
     * and then this function would perform the actual enhancement
     * if the target level is not met yet.
     *
     * @param targetLevel the target level of the skill
     * @param skillNumber the index of the skill
     * @see isTheTargetLevelConditionMet
     * @return true if the skill will be upgraded
     */
    private fun verifySkillUpgradeEligibility(targetLevel: Int, skillNumber: Int): Boolean {
        if (isConfirmationDialogVisible() || isTemporaryServant()) return false
        val skillCount = skillCountList[skillNumber - 1]
        val skillCountConditionMet = skillCount?.let { it < targetLevel } ?: false

        return skillCountConditionMet && isInSkillEnhancementMenu()
    }

    /**
     * This function is used to check if the script is in the skill enhancement menu
     *      This is the "Skills" text.
     * @return true if the script is in the skill enhancement menu
     */
    private fun isInSkillEnhancementMenu() = images[Images.SkillMenuBanner] in
            locations.enhancementBannerRegion

    /**
     * This function is used to check if the script is out of materials
     * You can find the out of materials at the top of the skills text.
     *
     * @return true if the script is out of materials
     */
    private fun isOutOfMats(): Boolean = images[Images.SkillInsufficientMaterials] in
            locations.skill.insufficientMaterialsRegion

    /**
     * This function is used to check if the confirmation dialog is visible
     * This is after selecting the Enhance button
     *
     * @return true if the confirmation dialog is visible
     */
    private fun isConfirmationDialogVisible() = images[Images.Ok] in
            locations.skill.confirmationDialogRegion

    /**
     * This function is used to check if the script is out of QP
     * You can find the out of QP at the top of the skills text.
     *
     * @return true if the script is out of QP
     */
    private fun isOutOfQP(): Boolean = images[Images.SkillInsufficientQP] in
            locations.getInsufficientQPRegion

    /**
     * This function is used to check if the servant is empty
     * This is being used for checking at the start of the script.
     * And if the current servant got max level.
     *
     * @return true if the servant is empty
     */
    private fun isServantEmpty() = images[Images.EmptyEnhance] in locations.emptyEnhanceRegion

    /**
     * This function is used to check if the servant is temporary
     * There is additional click if the servant is temporary.
     *
     * @return true if the servant is temporary
     */
    private fun isTemporaryServant() = images[Images.Execute] in locations.tempServantEnhancementRegion

    private fun makeExitState(): ExitState {
        return ExitState(
            skillSummaryList = listOf(
                Summary(
                    isCheckToUpgrade = prefs.skill.shouldUpgradeSkillOne,
                    isAvailable = skill1Available,
                    enhancementExitReason = upgradeResultList[0],
                    startingLevel = prefs.skill.minimumSkillOne,
                    endLevel = skillCountList[0],
                    targetLevel = when (prefs.skill.skillOneUpgradeValue > 0) {
                        true -> prefs.skill.minimumSkillOne + prefs.skill.skillOneUpgradeValue
                        false -> null
                    }
                ),
                Summary(
                    isCheckToUpgrade = prefs.skill.shouldUpgradeSkillTwo,
                    isAvailable = prefs.skill.isSkillTwoAvailable,
                    enhancementExitReason = upgradeResultList[1],
                    startingLevel = if (prefs.skill.isSkillTwoAvailable)
                        prefs.skill.minimumSkillTwo else null,
                    endLevel = skillCountList[1],
                    targetLevel = when (prefs.skill.skillTwoUpgradeValue > 0) {
                        true -> prefs.skill.minimumSkillTwo + prefs.skill.skillTwoUpgradeValue
                        false -> null
                    },
                ),
                Summary(
                    isCheckToUpgrade = prefs.skill.shouldUpgradeSkillThree,
                    isAvailable = prefs.skill.isSkillThreeAvailable,
                    enhancementExitReason = upgradeResultList[2],
                    startingLevel = if (prefs.skill.isSkillThreeAvailable)
                        prefs.skill.minimumSkillThree else null,
                    endLevel = skillCountList[2],
                    targetLevel = when (prefs.skill.skillThreeUpgradeValue > 0) {
                        true -> prefs.skill.minimumSkillThree + prefs.skill.skillThreeUpgradeValue
                        false -> null
                    }
                )
            )
        )
    }

}