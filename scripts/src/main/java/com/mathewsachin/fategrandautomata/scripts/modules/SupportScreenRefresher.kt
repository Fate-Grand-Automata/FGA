package com.mathewsachin.fategrandautomata.scripts.modules

import com.mathewsachin.fategrandautomata.scripts.IScriptMessages
import com.mathewsachin.fategrandautomata.scripts.ScriptNotify
import com.mathewsachin.libautomata.dagger.ScriptScope
import javax.inject.Inject
import kotlin.time.Duration
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
    private val supportRefreshThreshold = Duration.seconds(10)

    fun refreshSupportList() {
        performRefresh()

        waitForSupportScreenToLoad()
    }

    private fun performRefresh() {
        lastSupportRefreshTimestamp?.elapsedNow()?.let { elapsed ->
            val toWait = supportRefreshThreshold - elapsed

            if (toWait.isPositive()) {
                messages.notify(ScriptNotify.SupportListUpdatingIn(toWait))

                screen.delay(toWait)
            }
        }

        screen.refresh()
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
                    screen.isAnyDialogOpen() -> screen.delay(Duration.seconds(1))
                    screen.isListLoaded() -> return
                }

                screen.delay(Duration.milliseconds(100))
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