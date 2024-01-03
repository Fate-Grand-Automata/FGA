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
class AutoServantEnhancement @Inject constructor(
    private val connectionRetry: ConnectionRetry,
    exitManager: ExitManager,
    api: IFgoAutomataApi
) : EntryPoint(exitManager), IFgoAutomataApi by api {

    sealed class ExitReason {
        data object RanOutOfQP : ExitReason()

        data object NoServantSelected : ExitReason()

        data object NoEmbersOrQPLeft : ExitReason()

        data object Abort : ExitReason()

        class Unexpected(val e: Exception) : ExitReason()

        data object MaxLevelAchieved : ExitReason()

        data object RedirectAscension : ExitReason()

        data object UnableToPerformAscension : ExitReason()

        data object RedirectGrail : ExitReason()

        data class Limit(val count: Int) : ExitReason()

    }

    class ServantUpgradeException(val reason: ExitReason) : Exception()

    class ExitException(val reason: ExitReason) : Exception()


    override fun script(): Nothing {
        try {
            loop()
        } catch (e: ServantUpgradeException) {
            throw ExitException(e.reason)
        } catch (e: ScriptAbortException) {
            throw ExitException(ExitReason.Abort)
        } catch (e: Exception) {
            val reason = ExitReason.Unexpected(e)
            throw ExitException(reason = reason)
        }
    }

    private var shouldLimit = false
    private var limitCount = 1

    private fun loop(): Nothing {
        if (isServantEmpty()) {
            throw ServantUpgradeException(ExitReason.NoServantSelected)
        }
        shouldLimit = prefs.servant.shouldLimit
        limitCount = prefs.servant.limitCount

        val screens: Map<() -> Boolean, () -> Unit> = mapOf(
            { connectionRetry.needsToRetry() } to { connectionRetry.retry() },
            { isLimitReached() } to
                    { throw ServantUpgradeException(ExitReason.Limit(prefs.servant.limitCount - limitCount)) },
            { isMaxLevel() } to { checkIfRedirectOrExitAfterMaxLevel() },
            { isOutOfQP() } to { throw ServantUpgradeException(ExitReason.RanOutOfQP) },
            { isAutoSelectMinEmberLowQP() } to { performMinEmberLowQPEnhancement() },
            { isEmberSelectionDialogOpen() } to { performEnhancement() },
            { isTemporaryServant() } to { locations.tempServantEnhancementLocation.click() },
            { isNoEmberOrQPDialogOpen() } to { throw ServantUpgradeException(ExitReason.NoEmbersOrQPLeft) },
            { isFinalConfirmVisible() } to { confirmEnhancement() },
            { isAutoSelectVisible() } to { performAutoSelect() },
            { isAutoSelectOff() } to { throw ServantUpgradeException(ExitReason.MaxLevelAchieved) },
            { isInAscensionMenu()} to { handleReturnToEnhancementMenu() },
        )

        while (true) {
            val actor = useSameSnapIn {
                screens
                    .asSequence()
                    .filter { (validator, _) -> validator() }
                    .map { (_, actor) -> actor }
                    .firstOrNull()
            } ?: { locations.enhancementSkipRapidClick.click(5) }
            actor.invoke()

            0.5.seconds.wait()
        }
    }

    private fun checkIfRedirectOrExitAfterMaxLevel() {
        when {
            prefs.servant.shouldRedirectAscension && isRedirectAscensionVisible() -> {
                locations.servant.getServantRedirectRegion.click()
                waitUntilAscensionVisible()
                handlePerformedAscension()
            }

            prefs.servant.shouldRedirectGrail && isRedirectGrailVisible() -> {
                locations.servant.getServantRedirectRegion.click()
                waitUntilGrailVisible()
                throw ServantUpgradeException(ExitReason.RedirectGrail)
            }

            else -> {
                throw ServantUpgradeException(ExitReason.MaxLevelAchieved)
            }
        }
    }

    private fun handlePerformedAscension() {
        if (!prefs.servant.shouldPerformAscension) {
            throw ServantUpgradeException(ExitReason.RedirectAscension)
        }
        var confirmationVisible = false
        for (i in 0..2) {
            locations.enhancementClick.click()
            confirmationVisible = locations.servant.getFinalConfirmRegion.exists(
                images[Images.Ok],
                timeout = 5.seconds
            )
            if (confirmationVisible) {
                break
            }
            if (connectionRetry.needsToRetry()) {
                connectionRetry.retry()
                0.5.seconds.wait()
            }
        }
        if (!confirmationVisible) {
            throw ServantUpgradeException(ExitReason.UnableToPerformAscension)
        }
        while (true){
            locations.servant.getFinalConfirmLocation.click()

            val isAscensionMenuVisible = locations.servant.getServantEnhancementRegion.waitVanish(
                images[Images.ServantAscensionBanner],
                timeout = 3.seconds
            )
            if (isAscensionMenuVisible) {
                break
            }
        }
    }

    private fun isInAscensionMenu() = images[Images.ServantAscensionBanner] in
            locations.servant.getServantEnhancementRegion

    private fun handleReturnToEnhancementMenu() {
        locations.servant.returnToServantMenuFromAscensionLocation.click()
        0.5.seconds.wait()
        waitUntilServantMenuVisible()
    }

    private fun isOutOfQP(): Boolean = images[Images.SkillInsufficientQP] in
            locations.getInsufficientQPRegion

    private fun performAutoSelect() {
        locations.servant.autoSelectLocation.click()
    }

    private fun performEnhancement() {
        locations.servant.emberConfirmationDialogLocation.click()
        1.seconds.wait()
        locations.enhancementClick.click()
        0.5.seconds.wait()
    }

    private fun confirmEnhancement() {
        locations.servant.getFinalConfirmLocation.click()
        if (limitCount > 0) {
            --limitCount
        }
        1.0.seconds.wait()

    }

    private fun performMinEmberLowQPEnhancement() {
        locations.servant.getAutoSelectMinEmberLowQPLocation.click()
        1.0.seconds.wait()
        locations.enhancementClick.click()
        0.5.seconds.wait()
    }

    private fun isInServantEnhancementMenu() = images[Images.ServantEnhancement] in
            locations.servant.getServantEnhancementRegion

    private fun isLimitReached() = shouldLimit && limitCount <= 0 && isInServantEnhancementMenu()

    private fun isServantEmpty() = images[Images.EmptyEnhance] in locations.emptyEnhanceRegion

    private fun isAutoSelectVisible(): Boolean = images[Images.ServantAutoSelect] in
            locations.servant.getAutoSelectRegion

    private fun isEmberSelectionDialogOpen() = images[Images.Ok] in
            locations.servant.getEmberConfirmationDialogRegion

    private fun isNoEmberOrQPDialogOpen() = images[Images.Close] in
            locations.servant.getNoEmberOrQPDialogRegion(prefs.gameServer)

    private fun isFinalConfirmVisible() = images[Images.Ok] in locations.servant.getFinalConfirmRegion

    private fun isMaxLevel() = images[Images.ServantMaxLevel] in locations.servant.getServantMaxLevelRegion

    private fun isTemporaryServant() = images[Images.Execute] in locations.tempServantEnhancementRegion

    private fun isAutoSelectMinEmberLowQP() = images[Images.Ok] in locations.servant.getAutoSelectMinEmberLowQPRegion

    // This is for the temporary servants as they cannot do palingenesis and
    // thus needed another way to check if they are max level at FA
    private fun isAutoSelectOff() = images[Images.ServantAutoSelectOff] in locations.servant.getAutoSelectRegion

    private fun isRedirectGrailVisible() = images[Images.ServantGrail] in locations.servant.getServantRedirectRegion

    private fun isRedirectAscensionVisible() = images[Images.ServantAscension] in
            locations.servant.getServantRedirectRegion

    private fun waitUntilGrailVisible() = locations.servant.getServantEnhancementRegion.exists(
        images[Images.ServantGrailBanner],
        similarity = 0.7,
        timeout = 15.seconds
    )

    private fun waitUntilAscensionVisible() = locations.servant.getServantEnhancementRegion.exists(
        images[Images.ServantAscensionBanner],
        similarity = 0.7,
        timeout = 15.seconds
    )

    private fun waitUntilServantMenuVisible() = locations.servant.getServantEnhancementRegion.exists(
        images[Images.ServantEnhancement],
        similarity = 0.7,
        timeout = 15.seconds
    )
}