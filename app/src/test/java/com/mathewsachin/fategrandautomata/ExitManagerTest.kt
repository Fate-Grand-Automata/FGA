package com.mathewsachin.fategrandautomata

import com.mathewsachin.libautomata.ExitManager
import com.mathewsachin.libautomata.ScriptAbortException
import org.junit.Test

class ExitManagerTest {
    @Test(expected = ScriptAbortException::class)
    fun exit_manager_throws_after_exit() {
        val exitManager = ExitManager()
        exitManager.request()
        exitManager.checkExitRequested()
    }

    @Test
    fun exit_manager_can_cancel() {
        val exitManager = ExitManager()
        exitManager.request()
        exitManager.cancel()
    }
}
