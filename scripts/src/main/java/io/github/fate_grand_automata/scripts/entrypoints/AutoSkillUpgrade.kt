package io.github.fate_grand_automata.scripts.entrypoints

import io.github.fate_grand_automata.scripts.IFgoAutomataApi
import io.github.fate_grand_automata.scripts.Images
import io.github.fate_grand_automata.scripts.models.SkillUpgradeState
import io.github.fate_grand_automata.scripts.modules.ConnectionRetry
import io.github.fate_grand_automata.scripts.modules.EnhancementScreen
import io.github.fate_grand_automata.scripts.modules.SkillLevel
import io.github.fate_grand_automata.scripts.modules.SkillLevelReader
import io.github.fate_grand_automata.scripts.modules.maxSkillLevel
import io.github.lib_automata.EntryPoint
import io.github.lib_automata.ExitManager
import io.github.lib_automata.ScriptAbortException
import io.github.lib_automata.dagger.ScriptScope
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

/**
 * Loop iterations without a level increase before a skill is given up on. Each iteration waits at
 * least 0.5s, and a normal enhancement takes a handful of them.
 */
private const val maxIterationsWithoutProgress = 60

/**
 * Consecutive reads of the same unexpected level before it is trusted.
 */
private const val readsToTrustUnexpectedLevel = 5

/**
 * Enhances the three normal skills of the selected Servant on the Skill Enhancement screen up to
 * the target levels chosen in the launcher, one skill after another.
 */
@ScriptScope
class AutoSkillUpgrade @Inject constructor(
    private val connectionRetry: ConnectionRetry,
    private val enhancementScreen: EnhancementScreen,
    private val skillLevelReader: SkillLevelReader,
    state: SkillUpgradeState,
    exitManager: ExitManager,
    api: IFgoAutomataApi
) : EntryPoint(exitManager), IFgoAutomataApi by api {

    sealed class ExitReason(val cause: Exception? = null) {
        data object Done : ExitReason()
        data object RanOutOfQP : ExitReason()
        data object NoServantSelected : ExitReason()

        /** The Servant was deselected while skills were being enhanced. */
        data object ServantDeselected : ExitReason()

        /** The level of the skill being enhanced stopped going up. */
        data object NoProgress : ExitReason()
        data object Abort : ExitReason()
        class Unexpected(cause: Exception) : ExitReason(cause)
    }

    enum class SkillResult {
        Locked,

        /** The level couldn't be read during auto-detection. */
        Unreadable,
        AlreadyMaxed,

        /** The target level was not above the current level. */
        NotSelected,
        TargetReached,
        OutOfMaterials,
        OutOfQP,

        /** The run ended before this skill was finished, e.g. QP ran out on an earlier skill. */
        NotFinished
    }

    /**
     * @param startLevel null if the skill is locked or its level couldn't be read
     * @param endLevel the last level read from the screen, null if [startLevel] is
     */
    data class Summary(
        val startLevel: Int?,
        val endLevel: Int?,
        val targetLevel: Int,
        val result: SkillResult
    )

    /**
     * @param summaries one per skill, in skill order
     */
    class ExitException(val reason: ExitReason, val summaries: List<Summary>) : Exception(reason.cause)

    /**
     * Ends the run early from inside [upgradeSkill].
     */
    private class StopException(val reason: ExitReason) : Exception()

    private val summaries = state.currentLevels.zip(state.targetLevels) { current, target ->
        val startLevel = (current as? SkillLevel.Known)?.level

        Summary(
            startLevel = startLevel,
            endLevel = startLevel,
            targetLevel = target,
            result = initialResult(current, target)
        )
    }.toMutableList()

    override fun script(): Nothing {
        val reason = try {
            upgradeSkills()
        } catch (e: StopException) {
            e.reason
        } catch (e: ScriptAbortException) {
            ExitReason.Abort
        } catch (e: Exception) {
            ExitReason.Unexpected(e)
        }

        throw ExitException(reason, summaries)
    }

    /**
     * [SkillResult.NotFinished] marks a skill to enhance; [upgradeSkills] replaces it once the
     * skill is done.
     */
    private fun initialResult(current: SkillLevel, target: Int) = when (current) {
        SkillLevel.Locked -> SkillResult.Locked
        SkillLevel.Unreadable -> SkillResult.Unreadable
        is SkillLevel.Known -> when {
            current.level >= maxSkillLevel -> SkillResult.AlreadyMaxed
            target <= current.level -> SkillResult.NotSelected
            else -> SkillResult.NotFinished
        }
    }

    private fun upgradeSkills(): ExitReason {
        if (enhancementScreen.isNothingSelected()) {
            return ExitReason.NoServantSelected
        }

        for (index in summaries.indices) {
            val summary = summaries[index]
            if (summary.result != SkillResult.NotFinished || summary.startLevel == null) {
                continue
            }

            val result = upgradeSkill(
                skillNumber = index + 1,
                startLevel = summary.startLevel,
                targetLevel = summary.targetLevel
            )
            // upgradeSkill() updates endLevel, so this must not copy the Summary read before it
            summaries[index] = summaries[index].copy(result = result)

            if (result == SkillResult.OutOfQP) {
                return ExitReason.RanOutOfQP
            }
        }

        return ExitReason.Done
    }

    private sealed class Screen {
        data object ConnectionError : Screen()
        data object ServantEmpty : Screen()
        data object TemporaryServantDialog : Screen()
        data object ConfirmationDialog : Screen()

        /**
         * @param level null if OCR couldn't read it, e.g. during the level-up animation
         */
        data class SkillMenu(
            val level: Int?,
            val isOutOfMaterials: Boolean,
            val isOutOfQP: Boolean
        ) : Screen()

        data object Other : Screen()
    }

    private fun detectScreen(skillNumber: Int): Screen = when {
        connectionRetry.needsToRetry() -> Screen.ConnectionError
        enhancementScreen.isNothingSelected() -> Screen.ServantEmpty
        enhancementScreen.isTemporaryServantWarning() -> Screen.TemporaryServantDialog
        enhancementScreen.isOkButtonIn(locations.skill.confirmationDialogRegion) -> Screen.ConfirmationDialog
        enhancementScreen.isSkillEnhancementMenu() -> Screen.SkillMenu(
            level = (skillLevelReader.read(skillNumber) as? SkillLevel.Known)?.level,
            isOutOfMaterials = isOutOfMaterials(),
            isOutOfQP = isOutOfQP()
        )

        else -> Screen.Other
    }

    /**
     * Selects the skill and keeps enhancing it until [targetLevel] is reached or a resource runs out.
     */
    private fun upgradeSkill(skillNumber: Int, startLevel: Int, targetLevel: Int): SkillResult {
        val index = skillNumber - 1

        locations.skill.skillLocation(skillNumber).click(2)
        1.seconds.wait()

        /*
         * OCR can still show the old level while the level-up animation plays, so the script only
         * acts on a level once it matches the one expected after the last confirmed enhancement.
         * This prevents enhancing past the target.
         *
         * A different level is trusted after several identical reads in a row, in case the level
         * from auto-detection was wrong. Once an enhancement is confirmed, nothing below the new
         * level is trusted anymore, so a stale read of the old level can't cause another one.
         */
        var confirmedLevel = startLevel
        var expectedLevel = startLevel
        var minimumTrustedLevel = 1
        var unexpectedLevel: Int? = null
        var unexpectedLevelReads = 0
        var iterationsWithoutProgress = 0

        while (true) {
            if (++iterationsWithoutProgress > maxIterationsWithoutProgress) {
                throw StopException(ExitReason.NoProgress)
            }

            when (val screen = useSameSnapIn { detectScreen(skillNumber) }) {
                Screen.ConnectionError -> {
                    connectionRetry.retry()
                    iterationsWithoutProgress = 0
                }

                Screen.ServantEmpty -> throw StopException(ExitReason.ServantDeselected)
                Screen.TemporaryServantDialog -> locations.tempServantEnhancementLocation.click()

                Screen.ConfirmationDialog -> {
                    locations.skill.confirmationDialogRegion.click()
                    // Not a plain increment: the dialog can still be detected after this click
                    expectedLevel = confirmedLevel + 1
                    minimumTrustedLevel = expectedLevel
                    1.seconds.wait()
                }

                is Screen.SkillMenu -> {
                    val level = screen.level
                    val isTrusted = when {
                        level == null -> false
                        level == expectedLevel -> true
                        level < minimumTrustedLevel -> false
                        else -> {
                            if (level == unexpectedLevel) {
                                unexpectedLevelReads++
                            } else {
                                unexpectedLevel = level
                                unexpectedLevelReads = 1
                            }

                            unexpectedLevelReads >= readsToTrustUnexpectedLevel
                        }
                    }

                    // Otherwise wait for the level-up animation to finish
                    if (level != null && isTrusted) {
                        if (level > confirmedLevel) {
                            iterationsWithoutProgress = 0
                        }

                        confirmedLevel = level
                        expectedLevel = level
                        unexpectedLevel = null
                        unexpectedLevelReads = 0
                        summaries[index] = summaries[index].copy(endLevel = level)

                        when {
                            level >= targetLevel -> return SkillResult.TargetReached
                            screen.isOutOfMaterials -> return SkillResult.OutOfMaterials
                            screen.isOutOfQP -> return SkillResult.OutOfQP
                            else -> locations.enhancementClick.click()
                        }
                    }
                }

                Screen.Other -> skipAnimation()
            }

            0.5.seconds.wait()
        }
    }

    private fun skipAnimation() = locations.enhancementSkipRapidClick.click(5)

    /**
     * The warning shown above the skills when the selected skill lacks materials.
     */
    private fun isOutOfMaterials() =
        images[Images.SkillInsufficientMaterials] in locations.skill.insufficientMaterialsRegion

    /**
     * The warning shown above the skills when there isn't enough QP for the selected skill.
     */
    private fun isOutOfQP() =
        images[Images.SkillInsufficientQP] in locations.skill.insufficientQPRegion
}
