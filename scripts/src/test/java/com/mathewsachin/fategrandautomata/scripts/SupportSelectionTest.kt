package com.mathewsachin.fategrandautomata.scripts

import com.mathewsachin.fategrandautomata.scripts.enums.SupportClass
import com.mathewsachin.fategrandautomata.scripts.modules.ConnectionRetry
import com.mathewsachin.fategrandautomata.scripts.modules.SupportClassPicker
import com.mathewsachin.fategrandautomata.scripts.modules.SupportScreenRefresher
import com.mathewsachin.fategrandautomata.scripts.modules.SupportSelectionLoop
import com.mathewsachin.fategrandautomata.scripts.prefs.ISupportPreferences
import com.mathewsachin.fategrandautomata.scripts.prefs.ISupportPreferencesCommon
import com.mathewsachin.fategrandautomata.scripts.supportSelection.SupportSelectionResult
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import org.junit.Assert
import org.junit.Test

class SupportSelectionTest {
    private val commonPrefs = object : ISupportPreferencesCommon {
        override val maxUpdates = 3
        override val mlbSimilarity = 0.85
        override val swipesPerUpdate = 5
    }

    private val messages = mockk<IScriptMessages>().apply {
        every { notify(any()) } just Runs
    }

    private fun testSupportSelection(
        setup: (FakeSupportScreen) -> Unit,
        verify: (FakeSupportScreen) -> Unit,
        supportProvider: (FakeSupportScreen) -> SupportSelectionResult,
        alsoCheckAll: Boolean = false,
        supportClass: SupportClass = SupportClass.None,
        needToRetry: Boolean = false,
    ): Boolean {
        val screen = FakeSupportScreen()
        val supportPrefs = mockk<ISupportPreferences>()
        every { supportPrefs.alsoCheckAll } returns alsoCheckAll
        every { supportPrefs.supportClass } returns supportClass

        val connectionRetry = mockk<ConnectionRetry>()
        every { connectionRetry.needsToRetry() } returns needToRetry

        val classPicker = SupportClassPicker(screen, supportPrefs)

        val loop = SupportSelectionLoop(
            screen,
            commonPrefs,
            SupportScreenRefresher(screen, messages, connectionRetry, classPicker),
            classPicker
        )

        val provider = FakeSupportSelectionProvider {
            supportProvider(screen)
        }

        setup(screen)
        return loop.select(provider).also {
            verify(screen)
        }
    }

    @Test
    fun `support not found after refreshing 3 times`() {
        Assert.assertFalse(
            testSupportSelection(
                setup = {
                    it.state = FakeSupportScreen.State.NoSupports
                    it.onRefresh = { s ->
                        s.state = FakeSupportScreen.State.NoSupports
                    }
                },
                verify = {
                    Assert.assertEquals(3, it.refreshCount)
                },
                supportProvider = { SupportSelectionResult.Refresh }
            )
        )
    }

    @Test
    fun `support found immediately`() {
        Assert.assertTrue(
            testSupportSelection(
                setup = { it.state = FakeSupportScreen.State.SomeSupports },
                verify = {
                    Assert.assertEquals(0, it.scrollOffset)
                    Assert.assertEquals(0, it.refreshCount)
                },
                supportProvider = { SupportSelectionResult.Done }
            )
        )
    }
}