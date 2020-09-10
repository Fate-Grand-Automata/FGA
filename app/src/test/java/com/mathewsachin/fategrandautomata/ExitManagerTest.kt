package com.mathewsachin.fategrandautomata

import com.mathewsachin.libautomata.ExitManager
import com.mathewsachin.libautomata.ScriptAbortException
import org.junit.Test

class ExitManagerTest {
    @Test(expected = ScriptAbortException::class)
    fun exit_manager_throws_after_exit() {
        val exitManager = ExitManager()
        exitManager.exit()
        exitManager.checkExitRequested()
    }

    @Test(expected = ScriptAbortException::class)
    fun exit_manager_exit_after_pause() {
        val exitManager = ExitManager()
        exitManager.pause()
        exitManager.exit()
        exitManager.checkExitRequested()
    }

    @Test
    fun exit_not_requested() {
        val exitManager = ExitManager()
        exitManager.checkExitRequested()
    }
}
