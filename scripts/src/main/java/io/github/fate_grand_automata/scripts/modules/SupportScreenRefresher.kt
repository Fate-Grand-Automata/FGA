package io.github.fate_grand_automata.scripts.modules

import io.github.fate_grand_automata.scripts.IScriptMessages
import io.github.fate_grand_automata.scripts.ScriptNotify
import io.github.lib_automata.dagger.ScriptScope
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeMark
import kotlin.time.TimeSource

@ScriptScope
class SupportScreenRefresher @Inject constructor(
    private val screen: SupportScreen,
    private val messages: IScriptMessages,
    private val connectionRetry: ConnectionRetry,
    private val supportClassPicker: SupportClassPicker
) {
    private var lastSupportRefreshTimestamp: TimeMark? = null
    private val supportRefreshThreshold = 10.seconds
    private val refreshPollInterval = 0.5.seconds

    fun refreshSupportList() {
        waitUntilRefreshAvailable()
        screen.refresh()

        waitForSupportScreenToLoad()
    }

    /**
     * Waits until the game shows the refresh icon on the update button.
     *
     * The local timer starts once the list is seen loaded, which is later than the game's own
     * cooldown start, so it only serves as an upper bound in case the icon is not detected.
     */
    private fun waitUntilRefreshAvailable() {
        val lastRefresh = lastSupportRefreshTimestamp ?: return
        val toWait = supportRefreshThreshold - lastRefresh.elapsedNow()

        if (!toWait.isPositive() || screen.refreshAvailable()) return

        messages.notify(ScriptNotify.SupportListUpdatingIn(toWait))

        val deadline = TimeSource.Monotonic.markNow() + toWait
        while (deadline.hasNotPassedNow() && !screen.refreshAvailable()) {
            screen.delay(refreshPollInterval)
        }
    }

    private fun updateLastSupportRefreshTimestamp() {
        lastSupportRefreshTimestamp = TimeSource.Monotonic.markNow()
    }

    private fun waitTillListLoads() {
        try {
            while (true) {
                when {
                    connectionRetry.needsToRetry() -> connectionRetry.retry()
                    // wait for dialogs to close
                    screen.isAnyDialogOpen() -> screen.delay(1.seconds)
                    screen.isListLoaded() -> return
                }

                screen.delay(100.milliseconds)
            }
        } finally {
            updateLastSupportRefreshTimestamp()
        }
    }

    fun waitForSupportScreenToLoad() {
        waitTillListLoads()
        supportClassPicker.selectSupportClass()
    }
}