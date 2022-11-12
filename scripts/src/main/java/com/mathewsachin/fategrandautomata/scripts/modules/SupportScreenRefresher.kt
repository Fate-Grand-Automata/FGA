package com.mathewsachin.fategrandautomata.scripts.modules

import com.mathewsachin.fategrandautomata.scripts.IScriptMessages
import com.mathewsachin.fategrandautomata.scripts.ScriptNotify
import com.mathewsachin.libautomata.dagger.ScriptScope
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