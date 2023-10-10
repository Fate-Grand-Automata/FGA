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

    private var lastSkill1count: Int? = null
    private var lastSkill2count: Int? = null
    private var lastSkill3count: Int? = null

    private var isSkillUpgradeAnimationFinished = false

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
        if (isServantEmpty) {
            skill1Available = false
            throw SkillUpgradeException(ExitReason.NoServantSelected)
        }
        val skillUpgrade = prefs.skillUpgrade

        if (skillUpgrade.shouldUpgradeSkill1) {
            if (skillUpgrade.upgradeSkill1 > 0) {
                setupSkillUpgradeLoop(
                    skillLocation = locations.skillUpgrade.skill1Click,
                    skillRegion = locations.skillUpgrade.skill1TextRegion,
                    targetLevel = prefs.skillUpgrade.minSkill1 + prefs.skillUpgrade.upgradeSkill1,
                    operation = ::updateSkill1,
                    checkUpgradeSkill = ::needToUpgradeSkill1,
                    results = ::updateSkill1UpgradeResult
                )
            } else {
                updateSkill1UpgradeResult(EnhancementException(EnhancementExitReason.SameLevelError))
            }
            if (skill1UpgradeResult?.reason == EnhancementExitReason.OutOfQPException){

                if (skillUpgrade.shouldUpgradeSkill2){
                    skill2UpgradeResult = EnhancementException(EnhancementExitReason.ExitEarlyOutOfQPException)
                }
                if (skillUpgrade.shouldUpgradeSkill3){
                    skill3UpgradeResult = EnhancementException(EnhancementExitReason.ExitEarlyOutOfQPException)
                }
                throw SkillUpgradeException(ExitReason.RanOutOfQP)
            }
        }
        if (skillUpgrade.shouldUpgradeSkill2) {
            if (skillUpgrade.upgradeSkill2 > 0) {
                setupSkillUpgradeLoop(
                    skillLocation = locations.skillUpgrade.skill2Click,
                    skillRegion = locations.skillUpgrade.skill2TextRegion,
                    targetLevel = prefs.skillUpgrade.minSkill2 + prefs.skillUpgrade.upgradeSkill2,
                    operation = ::updateSkill2,
                    checkUpgradeSkill = ::needToUpgradeSkill2,
                    results = ::updateSkill2UpgradeResult
                )
            } else {
                updateSkill2UpgradeResult(EnhancementException(EnhancementExitReason.SameLevelError))
            }
            if (skill2UpgradeResult?.reason == EnhancementExitReason.OutOfQPException){
                if (skillUpgrade.shouldUpgradeSkill3){
                    skill3UpgradeResult = EnhancementException(EnhancementExitReason.ExitEarlyOutOfQPException)
                }
                throw SkillUpgradeException(ExitReason.RanOutOfQP)
            }
        }
        if (skillUpgrade.shouldUpgradeSkill3) {
            if (skillUpgrade.upgradeSkill3 > 0) {
                setupSkillUpgradeLoop(
                    skillLocation = locations.skillUpgrade.skill3Click,
                    skillRegion = locations.skillUpgrade.skill3TextRegion,
                    targetLevel = prefs.skillUpgrade.minSkill3 + prefs.skillUpgrade.upgradeSkill3,
                    operation = ::updateSkill3,
                    checkUpgradeSkill = ::needToUpgradeSkill3,
                    results = ::updateSkill3UpgradeResult
                )
            } else {
                updateSkill3UpgradeResult(EnhancementException(EnhancementExitReason.SameLevelError))
            }
            if (skill3UpgradeResult?.reason == EnhancementExitReason.OutOfQPException){
                throw SkillUpgradeException(ExitReason.RanOutOfQP)
            }
        }
        throw SkillUpgradeException(ExitReason.Done)
    }

    private fun setupSkillUpgradeLoop(
        skillLocation: Location,
        skillRegion: Region,
        targetLevel: Int,
        operation: (Int) -> Boolean,
        checkUpgradeSkill: (Int) -> Boolean,
        results: (EnhancementException) -> Unit
    ) {
        skillLocation.click(2)
        0.5.seconds.wait()
        val screens: Map<() -> Boolean, () -> Unit> = mapOf(
            { connectionRetry.needsToRetry() } to { connectionRetry.retry() },
            {
                isTheTargetUpgradeMet(
                    region = skillRegion,
                    targetLevel = targetLevel,
                    operation = operation
                )
            } to { throw EnhancementException(EnhancementExitReason.TargetLevelMet) },
            { checkUpgradeSkill(targetLevel) } to { executeUpgradeSkill() },
            { isOutOfMats() } to { throw EnhancementException(EnhancementExitReason.OutOfMatsException) },
            { isOutOfQP() } to { throw EnhancementException(EnhancementExitReason.OutOfQPException) }
        )

        performSkillUpgradeLoop(
            currentSkillScreen = screens,
            results = results
        )
    }

    private fun performSkillUpgradeLoop(
        currentSkillScreen: Map<() -> Boolean, () -> Unit>,
        results: (EnhancementException) -> Unit
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
                results(e)
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
        operation: (Int) -> Boolean
    ): Boolean {

        return if (isConfirmationDialog) {
            false
        } else {
            val currentLevel = region.detectNumberInText()

            currentLevel?.let {
                val notSameLastValue = if (isInSkillEnhancementMenu) {
                    isSkillUpgradeAnimationFinished = true
                    operation(it)
                } else false
                targetLevel <= it && isInSkillEnhancementMenu &&
                        notSameLastValue && isSkillUpgradeAnimationFinished
            } ?: false
        }
    }

    private fun executeUpgradeSkill() {
        locations.enhancementClick.click()
        0.5.seconds.wait()
        locations.skillUpgrade.confirmationDialogClick.click()
        isSkillUpgradeAnimationFinished = false
        0.5.seconds.wait()
    }

    private fun updateSkill1UpgradeResult(e: EnhancementException) {
        skill1UpgradeResult = e
    }

    private fun updateSkill2UpgradeResult(e: EnhancementException) {
        skill2UpgradeResult = e
    }

    private fun updateSkill3UpgradeResult(e: EnhancementException) {
        skill3UpgradeResult = e
    }

    private fun updateSkill1(level: Int): Boolean {
        skill1count = level
        return when (lastSkill1count) {
            level -> false
            null -> false
            else -> true
        }
    }

    private fun needToUpgradeSkill1(targetLevel: Int): Boolean {
        if (isConfirmationDialog) return false
        return skill1count?.let {
            val checkIfFirst = when (lastSkill1count) {
                it -> false
                else -> {
                    lastSkill1count = it
                    true
                }
            }
            it < targetLevel && isSkillUpgradeAnimationFinished && checkIfFirst
        } ?: false
    }

    private fun updateSkill2(level: Int): Boolean {
        skill2count = level
        return when (lastSkill2count) {
            level -> false
            null -> false
            else -> true
        }
    }

    private fun needToUpgradeSkill2(targetLevel: Int): Boolean {
        if (isConfirmationDialog) return false
        return skill2count?.let {
            val checkIfFirst = when (lastSkill2count) {
                it -> false
                else -> {
                    lastSkill2count = it
                    true
                }
            }
            it < targetLevel && isSkillUpgradeAnimationFinished && checkIfFirst
        } ?: false
    }

    private fun updateSkill3(level: Int): Boolean {
        skill3count = level
        return when (lastSkill3count) {
            level -> false
            null -> false
            else -> true
        }
    }

    private fun needToUpgradeSkill3(targetLevel: Int): Boolean {
        if (isConfirmationDialog) return false
        return skill3count?.let {
            val checkIfFirst = when (lastSkill3count) {
                it -> false
                else -> {
                    lastSkill3count = it
                    true
                }
            }
            it < targetLevel && isSkillUpgradeAnimationFinished && checkIfFirst
        } ?: false
    }

    private fun isOutOfMats(): Boolean = images[Images.SkillInsufficientMaterials] in
            locations.skillUpgrade.getInsufficientMatsRegion(prefs.gameServer)

    private val isConfirmationDialog = images[Images.Ok] in
            locations.skillUpgrade.getConfirmationDialog

    private val isInSkillEnhancementMenu = images[Images.SkillEnhancement] in
            locations.skillUpgrade.getSkillEnhanceRegion(prefs.gameServer)

    private fun isOutOfQP(): Boolean = images[Images.SkillInsufficientQP] in
            locations.getInsufficientQPRegion(prefs.gameServer)

    private val isServantEmpty = images[Images.EmptyEnhance] in locations.emptyEnhanceRegion

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