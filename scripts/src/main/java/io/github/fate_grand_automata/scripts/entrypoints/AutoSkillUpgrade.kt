package io.github.fate_grand_automata.scripts.entrypoints

import io.github.fate_grand_automata.scripts.IFgoAutomataApi
import io.github.fate_grand_automata.scripts.Images
import io.github.fate_grand_automata.scripts.modules.ConnectionRetry
import io.github.lib_automata.EntryPoint
import io.github.lib_automata.ExitManager
import io.github.lib_automata.Location
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

        data object SameLevelError : EnhancementExitReason()
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

    private var shouldUpgradeSkill1 = false
    private var shouldUpgradeSkill2 = false
    private var shouldUpgradeSkill3 = false


    private fun skillUpgrade(): Nothing {
        if (isServantEmpty()) {
            skill1Available = false
            throw SkillUpgradeException(ExitReason.NoServantSelected)
        }
        val skillUpgrade = prefs.skillUpgrade
        shouldUpgradeSkill1 = skillUpgrade.shouldUpgradeSkill1
        shouldUpgradeSkill2 = skillUpgrade.shouldUpgradeSkill2
        shouldUpgradeSkill3 = skillUpgrade.shouldUpgradeSkill3

        if (shouldUpgradeSkill1) {
            if (skillUpgrade.upgradeSkill1 > 0) {
                setupSkillUpgradeLoop(
                    skillLocation = locations.skillUpgrade.skill1Click,
                    skillRegion = locations.skillUpgrade.skill1TextRegion,
                    targetLevel = prefs.skillUpgrade.minSkill1 + prefs.skillUpgrade.upgradeSkill1,
                    skillNumber = 1,
                )
            } else {
                updateSkillUpgradeResult(EnhancementException(EnhancementExitReason.SameLevelError), 1)
            }

            ifRanOfQPEarlyException(
                e = skill1UpgradeResult?.reason,
                index = 1
            )
        }
        if (shouldUpgradeSkill2) {
            if (skillUpgrade.upgradeSkill2 > 0) {
                setupSkillUpgradeLoop(
                    skillLocation = locations.skillUpgrade.skill2Click,
                    skillRegion = locations.skillUpgrade.skill2TextRegion,
                    targetLevel = prefs.skillUpgrade.minSkill2 + prefs.skillUpgrade.upgradeSkill2,
                    skillNumber = 2,
                )
            } else {
                updateSkillUpgradeResult(EnhancementException(EnhancementExitReason.SameLevelError), 2)
            }
            ifRanOfQPEarlyException(
                e = skill2UpgradeResult?.reason,
                index = 1
            )
        }
        if (shouldUpgradeSkill3) {
            if (skillUpgrade.upgradeSkill3 > 0) {
                setupSkillUpgradeLoop(
                    skillLocation = locations.skillUpgrade.skill3Click,
                    skillRegion = locations.skillUpgrade.skill3TextRegion,
                    targetLevel = prefs.skillUpgrade.minSkill3 + prefs.skillUpgrade.upgradeSkill3,
                    skillNumber = 3,
                )
            } else {
                updateSkillUpgradeResult(EnhancementException(EnhancementExitReason.SameLevelError), 3)
            }
            ifRanOfQPEarlyException(
                e = skill3UpgradeResult?.reason,
                index = 3
            )
        }
        throw SkillUpgradeException(ExitReason.Done)
    }

    private fun setupSkillUpgradeLoop(
        skillLocation: Location,
        skillRegion: Region,
        targetLevel: Int,
        skillNumber: Int,
    ) {
        skillLocation.click(2)
        0.5.seconds.wait()
        val screens: Map<() -> Boolean, () -> Unit> = mapOf(
            { connectionRetry.needsToRetry() } to { connectionRetry.retry() },
            {
                isTheTargetUpgradeMet(
                    region = skillRegion,
                    targetLevel = targetLevel,
                    skillNumber = skillNumber
                )
            } to { throw EnhancementException(EnhancementExitReason.TargetLevelMet) },
            { isTemporaryServant() } to { locations.tempServantEnhancementLocation.click() },
            { isConfirmationDialog() } to { executeUpgradeSkill() },
            {
                checkIfWillUpgradeSkill(targetLevel = targetLevel, index = skillNumber)
            } to { locations.enhancementClick.click() },
            { isOutOfMats() } to { throw EnhancementException(EnhancementExitReason.OutOfMatsException) },
            { isOutOfQP() } to { throw EnhancementException(EnhancementExitReason.OutOfQPException) }
        )

        performSkillUpgradeLoop(
            currentSkillScreen = screens,
            skillNumber = skillNumber
        )
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
                updateSkillUpgradeResult(e, skillNumber)
                break
            }
        }
    }

    fun Region.detectNumberInText(): Int? {
        val text = this
            .detectText(false) // replace common OCR mistakes
            .replace("%", "x")
            .replace("S", "5")
            .replace("O", "0")
            .lowercase()
        val regex = Regex("""(\d+)""")
        return regex.find(text)?.groupValues?.getOrNull(1)?.toInt()
    }

    private fun isTheTargetUpgradeMet(
        region: Region,
        targetLevel: Int,
        skillNumber: Int,
    ): Boolean {

        return if (isConfirmationDialog()) {
            false
        } else {
            val currentLevel = region.detectNumberInText()

            currentLevel?.let {
                updateCurrentSkillLevel(level = it, index = skillNumber)
                val checkIfIsInSkillEnhancementMenu = isInSkillEnhancementMenu()

                targetLevel <= it && checkIfIsInSkillEnhancementMenu
            } ?: false
        }
    }

    private fun executeUpgradeSkill() {
        locations.skillUpgrade.confirmationDialogClick.click()
        1.0.seconds.wait()
    }

    private fun updateSkillUpgradeResult(e: EnhancementException, index: Int) {
        when (index) {
            1 -> skill1UpgradeResult = e
            2 -> skill2UpgradeResult = e
            3 -> skill3UpgradeResult = e
            else -> skill1UpgradeResult = e
        }
    }

    private fun ifRanOfQPEarlyException(e: EnhancementExitReason?, index: Int) {
        if (e != EnhancementExitReason.OutOfQPException) return
        when (index) {
            1 -> {
                if (shouldUpgradeSkill2) {
                    skill2UpgradeResult = EnhancementException(EnhancementExitReason.ExitEarlyOutOfQPException)
                }
                if (shouldUpgradeSkill3) {
                    skill3UpgradeResult = EnhancementException(EnhancementExitReason.ExitEarlyOutOfQPException)
                }
            }

            2 -> {
                if (shouldUpgradeSkill3) {
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
            locations.skillUpgrade.getInsufficientMatsRegion(prefs.gameServer)

    private fun isConfirmationDialog() = images[Images.Ok] in
            locations.skillUpgrade.getConfirmationDialog

    private fun isInSkillEnhancementMenu() = images[Images.SkillEnhancement] in
            locations.skillUpgrade.getSkillEnhanceRegion(prefs.gameServer)

    private fun isOutOfQP(): Boolean = images[Images.SkillInsufficientQP] in
            locations.getInsufficientQPRegion(prefs.gameServer)

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