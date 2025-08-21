package io.github.fate_grand_automata.scripts

import assertk.assertThat
import assertk.assertions.isEqualTo
import io.github.fate_grand_automata.scripts.enums.SupportClass
import io.github.fate_grand_automata.scripts.modules.ConnectionRetry
import io.github.fate_grand_automata.scripts.modules.SupportClassPicker
import io.github.fate_grand_automata.scripts.modules.SupportScreenRefresher
import io.github.fate_grand_automata.scripts.modules.SupportSelectionLoop
import io.github.fate_grand_automata.scripts.prefs.ISupportPreferences
import io.github.fate_grand_automata.scripts.prefs.ISupportPreferencesCommon
import io.github.fate_grand_automata.scripts.supportSelection.SupportSelectionResult
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

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
            classPicker,
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
        assertFalse(
            testSupportSelection(
                setup = {
                    it.state = FakeSupportScreen.State.NoSupports
                    it.onRefresh = { s ->
                        s.state = FakeSupportScreen.State.NoSupports
                    }
                },
                verify = {
                    assertThat(it.refreshCount).isEqualTo(3)
                },
                supportProvider = { SupportSelectionResult.Refresh },
            ),
        )
    }

    @Test
    fun `support found immediately`() {
        assertTrue(
            testSupportSelection(
                setup = { it.state = FakeSupportScreen.State.SomeSupports },
                verify = {
                    assertThat(it.scrollOffset).isEqualTo(0)
                    assertThat(it.refreshCount).isEqualTo(0)
                },
                supportProvider = { SupportSelectionResult.Done },
            ),
        )
    }
}
