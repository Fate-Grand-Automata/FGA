package io.github.fate_grand_automata.scripts.entrypoints

import io.github.fate_grand_automata.scripts.IFgoAutomataApi
import io.github.fate_grand_automata.scripts.Images
import io.github.fate_grand_automata.scripts.modules.ConnectionRetry
import io.github.lib_automata.EntryPoint
import io.github.lib_automata.ExitManager
import io.github.lib_automata.Location
import io.github.lib_automata.Region
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
        data object RanOutOfQP : ExitReason()

        data object NoServantSelected: ExitReason()

        data object Success : ExitReason()
    }

    class ExitException(val reason: ExitReason) : Exception()

    sealed class EnhancementExitReason {
        data object OutOfMatsException : EnhancementExitReason()

        data object OutOfQPException: EnhancementExitReason()

        data object TargetLevelMet : EnhancementExitReason()

        data object SameLevelError : EnhancementExitReason()
    }

    class EnhancementException(val reason: EnhancementExitReason) : Exception()


    private var skill1count = 0
    private var skill2count = 0
    private var skill3count = 0

    private var isSkillUpgradeAnimationFinished = false

    val screens: Map<() -> Boolean, () -> Unit> = mapOf(
        { isOutOfMats } to { throw EnhancementException(EnhancementExitReason.OutOfMatsException) },
        { connectionRetry.needsToRetry() } to { connectionRetry.retry() }
    )

    var skill1UpgradeResult: Exception? = null
    var skill2UpgradeResult: Exception? = null
    var skill3UpgradeResult: Exception? = null

    override fun script(): Nothing {
        if (isServantEmpty){
            throw ExitException(ExitReason.NoServantSelected)
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

        }
        throw ExitException(ExitReason.Success)
    }

    private fun setupSkillUpgradeLoop(
        skillLocation: Location,
        skillRegion: Region,
        targetLevel: Int,
        operation: (Int) -> Unit,
        checkUpgradeSkill: (Int) -> Boolean,
        results: (Exception) -> Unit
    ) {
        skillLocation.click(2)
        0.5.seconds.wait()
        val skill1Screen: Map<() -> Boolean, () -> Unit> = mapOf(
            {
                isTheTargetUpgradeMet(
                    region = skillRegion,
                    targetLevel = targetLevel,
                    operation = operation
                )
            } to { throw EnhancementException(EnhancementExitReason.TargetLevelMet) },
            { checkUpgradeSkill(targetLevel) } to { executeUpgradeSkill() },
        )

        performSkillUpgradeLoop(
            currentSkillScreen = screens + skill1Screen,
            results = results
        )
    }

    private fun performSkillUpgradeLoop(
        currentSkillScreen: Map<() -> Boolean, () -> Unit>,
        results: (Exception) -> Unit
    ) {
        while (true) {
            try {
                val actor = useSameSnapIn {
                    currentSkillScreen
                        .asSequence()
                        .filter { (validator, _) -> validator() }
                        .map { (_, actor) -> actor }
                        .firstOrNull()
                } ?: { locations.skillUpgrade.skipRapidClick.click(5) }

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
        operation: (Int) -> Unit
    ): Boolean {

        return if (isConfirmationDialog) {
            false
        } else {

            val currentLevel = region.detectNumberInText()

            currentLevel?.let {
                isSkillUpgradeAnimationFinished = true
                if (isInSkillEnhancementMenu) {
                    operation(it)
                }
                targetLevel <= it && isInSkillEnhancementMenu
            } ?: isInSkillEnhancementMenu && isSkillUpgradeAnimationFinished
        }
    }

    private fun executeUpgradeSkill() {
        locations.skillUpgrade.enhancementClick.click()
        0.5.seconds.wait()
        locations.skillUpgrade.confirmationDialogClick.click()
        isSkillUpgradeAnimationFinished = false
    }

    private fun updateSkill1UpgradeResult(e: Exception) {
        skill1UpgradeResult = e
    }

    private fun updateSkill2UpgradeResult(e: Exception) {
        skill2UpgradeResult = e
    }

    private fun updateSkill3UpgradeResult(e: Exception) {
        skill3UpgradeResult = e
    }

    private fun updateSkill1(level: Int) {
        skill1count = level
    }

    private fun needToUpgradeSkill1(targetLevel: Int): Boolean {
        if (isConfirmationDialog) return false
        return skill1count < targetLevel
    }

    private fun updateSkill2(level: Int) {
        skill2count = level
    }

    private fun needToUpgradeSkill2(targetLevel: Int): Boolean {
        if (isConfirmationDialog) return false
        return skill2count < targetLevel
    }

    private fun updateSkill3(level: Int) {
        skill3count = level
    }

    private fun needToUpgradeSkill3(targetLevel: Int): Boolean {
        if (isConfirmationDialog) return false
        return skill3count < targetLevel
    }

    private val isOutOfMats = images[Images.SkillInsufficientMaterials] in
            locations.skillUpgrade.getInsufficientMatsRegion(prefs.gameServer)

    private val isConfirmationDialog = images[Images.Ok] in
            locations.skillUpgrade.getConfirmationDialog(prefs.gameServer)

    private val isInSkillEnhancementMenu = images[Images.SkillEnhancement] in
            locations.skillUpgrade.getSkillEnhanceRegion(prefs.gameServer)

    private val isServantEmpty = images[Images.EmptyEnhance] in locations.emptyEnhanceRegion

}