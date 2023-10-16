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

        data object NoEmbersLeft : ExitReason()

        data object Abort : ExitReason()

        class Unexpected(val e: Exception) : ExitReason()

        data object MaxLevelAchieved : ExitReason()

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
            { isMaxLevel() } to { throw ServantUpgradeException(ExitReason.MaxLevelAchieved) },
            { isOutOfQP() } to { throw ServantUpgradeException(ExitReason.RanOutOfQP) },
            { isAutoSelectNoQP() } to { throw ServantUpgradeException(ExitReason.RanOutOfQP) },
            { isEmberSelectionDialogOpen() } to { performEnhancement() },
            { isTemporaryServant() } to { locations.tempServantEnhancementLocation.click() },
            { isNoEmberDialogOpen() } to { throw ServantUpgradeException(ExitReason.NoEmbersLeft) },
            { isFinalConfirmVisible() } to { confirmEnhancement() },
            { isAutoSelectVisible() } to { performAutoSelect() },
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

    private fun isOutOfQP(): Boolean = images[Images.SkillInsufficientQP] in
            locations.getInsufficientQPRegion(prefs.gameServer)

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

    private fun isInServantEnhancementMenu() = images[Images.ServantEnhancement] in
            locations.servant.getServantEnhancementRegion(prefs.gameServer)

    private fun isLimitReached() = shouldLimit && limitCount <= 0 && isInServantEnhancementMenu()

    private fun isServantEmpty() = images[Images.EmptyEnhance] in locations.emptyEnhanceRegion

    private fun isAutoSelectVisible(): Boolean = images[Images.ServantAutoSelect] in
            locations.servant.getAutoSelectRegion

    private fun isEmberSelectionDialogOpen() = images[Images.Ok] in
            locations.servant.getEmberConfirmationDialogRegion

    private fun isNoEmberDialogOpen() = images[Images.Close] in
            locations.servant.getNoEmberDialogRegion(prefs.gameServer)

    private fun isFinalConfirmVisible() = images[Images.Ok] in locations.servant.getFinalConfirmRegion

    private fun isMaxLevel() = images[Images.ServantMaxLevel] in locations.servant.getServantMaxLevelRegion

    private fun isTemporaryServant() = images[Images.Execute] in locations.tempServantEnhancementRegion

    private fun isAutoSelectNoQP() = images[Images.Ok] in locations.servant.getAutoSelectNoQPRegion
}