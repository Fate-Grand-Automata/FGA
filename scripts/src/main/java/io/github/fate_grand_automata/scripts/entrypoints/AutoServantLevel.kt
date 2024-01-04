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
class AutoServantLevel @Inject constructor(
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
            { isLimitReached() } to {
                throw ServantUpgradeException(ExitReason.Limit(prefs.servant.limitCount - limitCount))
            },
            { isMaxLevel() } to { checkMaxLevelRedirectOrExit() },
            { isOutOfQP() } to { throw ServantUpgradeException(ExitReason.RanOutOfQP) },
            { isAutoSelectMinimumEmberForLowQP() } to { performMinimumEmberForLowQPEnhancement() },
            { isEmberSelectionDialogVisible() } to { performEnhancement() },
            { isTemporaryServant() } to { locations.tempServantEnhancementLocation.click() },
            { isEmptyEmberOrQPDialogVisible() } to {
                throw ServantUpgradeException(ExitReason.NoEmbersOrQPLeft)
            },
            { isFinalConfirmDialogVisible() } to { confirmEnhancement() },
            { isAutoSelectVisible() } to { performAutoSelect() },
            { isAutoSelectOff() } to { throw ServantUpgradeException(ExitReason.MaxLevelAchieved) },
            { isInAscensionMenu() } to { handleReturnToEnhancementMenu() },
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

    private fun checkMaxLevelRedirectOrExit() {
        val ascensionRedirect = isRedirectAscensionVisible()
        val grailRedirect = isRedirectGrailVisible()
        when {
            prefs.servant.shouldRedirectAscension && ascensionRedirect -> {
                while (true){
                    locations.servant.servantRedirectCheckRegion.click()
                    val isVisible = waitUntilAscensionVisible()
                    if (isVisible) {
                        break
                    }
                }
                handlePerformedAscension()
            }

            prefs.servant.shouldRedirectGrail && grailRedirect -> {
                while (true) {
                    locations.servant.servantRedirectCheckRegion.click()
                    val isVisible = waitUntilGrailVisible()
                    if (isVisible) {
                        break
                    }
                }
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
            confirmationVisible = locations.servant.finalConfirmRegion.exists(
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
        while (true) {
            locations.servant.finalConfirmRegion.click()

            val isAscensionMenuVisible = locations.enhancementBannerRegion.waitVanish(
                images[Images.ServantAscensionBanner],
                timeout = 3.seconds
            )
            if (isAscensionMenuVisible) {
                break
            }
        }
    }

    private fun isInAscensionMenu() = images[Images.ServantAscensionBanner] in
            locations.enhancementBannerRegion

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
        locations.servant.finalConfirmRegion.click()
        if (limitCount > 0) {
            --limitCount
        }
        1.0.seconds.wait()

    }

    private fun performMinimumEmberForLowQPEnhancement() {
        locations.servant.autoSelectMinEmberLowQPLocation.click()
        1.0.seconds.wait()
        locations.enhancementClick.click()
        0.5.seconds.wait()
    }

    private fun isLimitReached() = shouldLimit && limitCount <= 0 && isAutoSelectVisible()

    private fun isServantEmpty() = images[Images.EmptyEnhance] in locations.emptyEnhanceRegion

    private fun isAutoSelectVisible(): Boolean = images[Images.ServantAutoSelect] in
            locations.servant.servantAutoSelectRegion

    private fun isEmberSelectionDialogVisible() = images[Images.Ok] in
            locations.servant.emberConfirmationDialogRegion

    private fun isEmptyEmberOrQPDialogVisible() = images[Images.Close] in
            locations.servant.emptyEmberOrQPDialogRegion(prefs.gameServer)

    private fun isFinalConfirmDialogVisible() = images[Images.Ok] in locations.servant.finalConfirmRegion

    private fun isMaxLevel() = images[Images.ServantMaxLevel] in locations.servant.servantMaxLevelRegion

    private fun isTemporaryServant() = images[Images.Execute] in locations.tempServantEnhancementRegion

    private fun isAutoSelectMinimumEmberForLowQP() = images[Images.Ok] in
            locations.servant.autoSelectMinEmberLowQPRegion

    // This is for the temporary servants as they cannot do palingenesis and
    // thus needed another way to check if they are max level at FA
    private fun isAutoSelectOff() = images[Images.ServantAutoSelectOff] in
            locations.servant.servantAutoSelectRegion

    private fun isRedirectGrailVisible() = images[Images.ServantGrailRedirectFromMenu] in
            locations.servant.servantRedirectCheckRegion

    private fun isRedirectAscensionVisible() = images[Images.ServantAscensionRedirectFromMenu] in
            locations.servant.servantRedirectCheckRegion

    private fun waitUntilGrailVisible() = locations.enhancementBannerRegion.exists(
        images[Images.ServantGrailBanner],
        similarity = 0.7,
        timeout = 5.seconds
    )

    private fun waitUntilAscensionVisible() = locations.enhancementBannerRegion.exists(
        images[Images.ServantAscensionBanner],
        similarity = 0.7,
        timeout = 5.seconds
    )

    private fun waitUntilServantMenuVisible() = locations.servant.servantAutoSelectRegion.exists(
        images[Images.ServantAutoSelect],
        similarity = 0.7,
        timeout = 5.seconds
    )
}