package io.github.fate_grand_automata.scripts.entrypoints

import io.github.fate_grand_automata.scripts.IFgoAutomataApi
import io.github.fate_grand_automata.scripts.Images
import io.github.fate_grand_automata.scripts.modules.ConnectionRetry
import io.github.lib_automata.EntryPoint
import io.github.lib_automata.ExitManager
import io.github.lib_automata.Highlighter
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

    /**
     * This is the reason why the script exited.
     */
    sealed class ExitReason {
        /**
         * The script exited because it has no servants selected.
         */

        data object NoServantSelected : ExitReason()

        /**
         * The script exited because it ran out of embers or QP.
         * Just combine the two reasons into one to save logic.
         */

        data object NoEmbersOrQPLeft : ExitReason()

        /**
         * The script exited because the user aborted the script.
         */
        data object Abort : ExitReason()

        /**
         * The script exited because it encountered an unexpected exception.
         */

        class Unexpected(val exception: Exception) : ExitReason()

        /**
         * The script exited because it reached the max level.
         */

        data object MaxLevelAchieved : ExitReason()

        /**
         * The script exited because it was redirected to the ascension menu.
         * If the user didn't enabled the option to perform ascension, the script will exit.
         */
        data object RedirectAscension : ExitReason()

        /**
         * The script exited because it was redirected to the ascension menu.
         * While the user enabled the option to perform ascension, it was unable to do so.
         */

        data object UnableToPerformAscension : ExitReason()

        /**
         * The script exited because it was redirected to the grail menu.
         */
        data object RedirectGrail : ExitReason()
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

    private fun loop(): Nothing {
        if (isServantEmpty()) {
            throw ServantUpgradeException(ExitReason.NoServantSelected)
        }

        val screens: Map<() -> Boolean, () -> Unit> = mapOf(
            { connectionRetry.needsToRetry() } to { connectionRetry.retry() },
            { isMaxLevel() } to { checkMaxLevelRedirectOrExit() },
            { isAutoSelectMinimumEmberForLowQP() } to { performMinimumEmberForLowQPEnhancement() },
            { isEmberSelectionDialogVisible() } to { performEnhancement() },
            { isTemporaryServant() } to { locations.tempServantEnhancementLocation.click() },
            { isEmptyEmberOrQPDialogVisible() } to {
                throw ServantUpgradeException(ExitReason.NoEmbersOrQPLeft)
            },
            { isFinalConfirmDialogVisible() } to { confirmEnhancement() },
            { isAutoSelectVisible() } to { performAutoSelect() },
            { isAutoSelectOff() } to { throw ServantUpgradeException(ExitReason.MaxLevelAchieved) },
            { isInAscensionMenu() } to { handlePerformedAscension() },
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

    /**
     * This function will check if the max level is achieved.
     *
     * If it is, it will check if the user enabled the option to redirect to the ascension menu or grail menu.
     *
     * If the user enabled the option to redirect to the ascension menu,
     * it will wait until the ascension menu is visible.
     *
     * If the user enabled the option to redirect to the grail menu, it will wait until the grail menu is visible.
     *
     * If the user didn't enabled any of the options, it will exit the script.
     */
    private fun checkMaxLevelRedirectOrExit() {
        if (prefs.platformPrefs.debugMode) {
            // wait for debug rectangles to disappear
            (Highlighter.DEFAULT_DURATION + 0.1.seconds).wait()
        }
        var ascensionRedirect = false
        var grailRedirect = false
        useSameSnapIn {
            ascensionRedirect = prefs.servant.shouldRedirectAscension && isRedirectAscensionVisible()
            grailRedirect = prefs.servant.shouldRedirectGrail && isRedirectGrailVisible()
        }
        when {
            ascensionRedirect -> {
                while (true) {
                    locations.servant.servantRedirectCheckRegion(prefs.gameServer).click()
                    val isVisible = waitUntilAscensionVisible()
                    if (isVisible) {
                        break
                    }
                }
                if (!prefs.servant.shouldPerformAscension) {
                    throw ServantUpgradeException(ExitReason.RedirectAscension)
                }
            }

            grailRedirect -> {
                while (true) {
                    locations.servant.servantRedirectCheckRegion(prefs.gameServer).click()
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

    /**
     * This function will handle the ascension.
     * if isReturnToLevel() is true, it try to return to the enhancement menu.
     * otherwise it will perform the ascension.
     *
     * @see isReturnToLevel
     */
    private fun handlePerformedAscension() {
        if (isReturnToLevel()) {
            handleReturnToEnhancementMenu()
            return
        }

        var confirmationVisible = false
        val retry = 5

        run ascension@{
            repeat(retry) {
                locations.enhancementClick.click()
                confirmationVisible = mapOf(
                    images[Images.Ok] to locations.servant.finalConfirmRegion,
                    images[Images.Execute] to locations.tempServantEnhancementRegion
                ).exists(
                    timeout = 3.seconds
                )

                if (confirmationVisible) {
                    return@ascension
                }
                if (connectionRetry.needsToRetry()) {
                    connectionRetry.retry()
                    0.5.seconds.wait()
                }
            }
        }

        if (!confirmationVisible) {
            throw ServantUpgradeException(ExitReason.UnableToPerformAscension)
        }
    }

    /**
     * This function will check if the script is in the ascension menu.
     */
    private fun isInAscensionMenu() = images[Images.ServantAscensionBanner] in
            locations.enhancementBannerRegion

    /**
     * This function will handle the return to enhancement menu.
     * It will also wait until the servant menu is visible to ensure that the script is in the right menu.
     *
     * @see waitUntilServantMenuVisible
     */
    private fun handleReturnToEnhancementMenu() {
        locations.servant.returnToServantMenuFromAscensionLocation.click()
        0.5.seconds.wait()
        waitUntilServantMenuVisible()
    }

    /**
     * This function will perform the auto select.
     * This will start the script to get the ember to enhance the servant.
     *
     * @see isAutoSelectVisible
     */
    private fun performAutoSelect() {
        locations.servant.autoSelectLocation.click()
    }

    /**
     * This function will perform the enhancement.
     * This is the function that will be called when the ember selection dialog is visible.
     *
     * If the "isEmptyEmberOrQPDialogVisible" is true after the initial click.
     * It won't be able to detect it immediately as the enhancement click is still needed to be clicked.
     *
     * @see isEmberSelectionDialogVisible
     * @see isEmptyEmberOrQPDialogVisible
     */
    private fun performEnhancement() {
        locations.servant.emberConfirmationDialogLocation.click()
        1.seconds.wait()
        locations.enhancementClick.click()
        0.5.seconds.wait()
    }

    /**
     * This function will confirm the enhancement.
     * This is the function that will be called when the final confirmation dialog is visible.
     *
     * @see isFinalConfirmDialogVisible
     */
    private fun confirmEnhancement() {
        locations.servant.finalConfirmRegion.click()
        1.0.seconds.wait()

    }

    /**
     * This function will perform the minimum ember for low QP enhancement.
     * This happens if you got low qp and the script will try to use the minimum ember to enhance.
     */
    private fun performMinimumEmberForLowQPEnhancement() {
        locations.servant.autoSelectMinEmberLowQPLocation.click()
        1.0.seconds.wait()
        locations.enhancementClick.click()
        0.5.seconds.wait()
    }

    /**
     * This function will check if the servant is empty.
     */
    private fun isServantEmpty() = images[Images.EmptyEnhance] in locations.emptyEnhanceRegion

    /**
     * This function will check if the auto select is visible to start the selection of embers.
     */
    private fun isAutoSelectVisible(): Boolean = images[Images.ServantAutoSelect] in
            locations.servant.servantAutoSelectRegion

    /**
     * This function will check if the ember selection dialog is visible.
     */
    private fun isEmberSelectionDialogVisible() = images[Images.Ok] in
            locations.servant.emberConfirmationDialogRegion

    /**
     * This function will check if the empty ember or QP dialog is visible.
     */
    private fun isEmptyEmberOrQPDialogVisible() =
        images[Images.Close] in locations.servant.emptyEmberOrQPDialogRegion

    /**
     * This function will check if the final confirmation dialog is visible.
     * After clicking the button, it would then perform the enhancement.
     */
    private fun isFinalConfirmDialogVisible() = images[Images.Ok] in locations.servant.finalConfirmRegion

    /**
     * This function will check if the servant is max level.
     * This is added instead of having the script to check for grail menu and ascension menu
     * to save time and computation.
     *
     * @see isRedirectAscensionVisible
     * @see isRedirectGrailVisible
     */
    private fun isMaxLevel() = images[Images.ServantMaxLevel] in locations.servant.servantMaxLevelRegion

    /**
     * This function will check if the servant is temporary.
     * Temporary servants have additional check to ensure that they are going to be enhanced.
     */
    private fun isTemporaryServant() = images[Images.Execute] in locations.tempServantEnhancementRegion

    /**
     * This function will check if the servant can auto select the minimum ember for low QP.
     */
    private fun isAutoSelectMinimumEmberForLowQP() = images[Images.Ok] in
            locations.servant.autoSelectMinEmberLowQPRegion


    /**
     * This function is for the temporary servants as they cannot do palingenesis and
     * thus needed another way to check if they are max level at FA.
     */
    private fun isAutoSelectOff() = images[Images.ServantAutoSelectOff] in
            locations.servant.servantAutoSelectRegion

    /**
     * This function will check if the servant can redirect to the grail menu.
     */
    private fun isRedirectGrailVisible() = images[Images.ServantGrailRedirectFromMenu] in
            locations.servant.servantRedirectCheckRegion(prefs.gameServer)

    /**
     * This function will check if the servant can redirect to the ascension menu.
     */
    private fun isRedirectAscensionVisible() = images[Images.ServantAscensionRedirectFromMenu] in
            locations.servant.servantRedirectCheckRegion(prefs.gameServer)

    /**
     * This function will wait until the grail menu is visible.
     */
    private fun waitUntilGrailVisible() = locations.enhancementBannerRegion.exists(
        images[Images.ServantGrailBanner],
        similarity = 0.7,
        timeout = 5.seconds
    )

    /**
     * This function will wait until the ascension menu is visible.
     */
    private fun waitUntilAscensionVisible() = locations.enhancementBannerRegion.exists(
        images[Images.ServantAscensionBanner],
        similarity = 0.7,
        timeout = 5.seconds
    )

    /**
     * This function will check if the servant menu is visible.
     * This is used to ensure that the script is in the right menu.
     * and it delays the script to save computation for checking in the loop.
     */
    private fun waitUntilServantMenuVisible() = locations.servant.servantAutoSelectRegion.exists(
        images[Images.ServantAutoSelect],
        similarity = 0.7,
        timeout = 5.seconds
    )

    /**
     * This function will only run if the script is in the ascension menu.
     * This will check if the script would return to the enhancement menu.
     */
    private fun isReturnToLevel() = images[Images.ServantAscensionReturnToLevel] in
            locations.servant.ascensionReturnToLevelRegion
}