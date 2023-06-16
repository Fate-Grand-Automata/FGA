package io.github.fate_grand_automata.scripts

import io.github.fate_grand_automata.scripts.enums.SupportClass
import io.github.fate_grand_automata.scripts.modules.SupportScreen
import io.github.lib_automata.ExitManager
import kotlin.time.Duration

class FakeSupportScreen(
    var onRefresh: (FakeSupportScreen) -> Unit = { it.state = State.Loading }
) : SupportScreen {
    enum class State {
        Loading, ConnectionFailed, SomeSupports, NoSupports
    }

    val exitManager = ExitManager()

    var state = State.Loading

    var scrollOffset = 0
        private set

    var selectedClass = SupportClass.All
        private set

    var refreshCount = 0
        private set

    override fun scrollDown() {
        scrollOffset++
    }

    override fun scrollToTop() {
        scrollOffset = 0
    }

    override fun click(supportClass: SupportClass) {
        selectedClass = supportClass
    }

    override fun delay(duration: Duration) {
        exitManager.checkExitRequested()
    }

    override fun refresh() {
        scrollOffset = 0
        refreshCount++
        onRefresh(this)
    }

    override fun isAnyDialogOpen() =
        state == State.ConnectionFailed

    override fun noSupportsPresent() =
        state == State.NoSupports

    override fun someSupportsPresent() =
        state == State.SomeSupports

    override fun isListLoaded() =
        noSupportsPresent() || someSupportsPresent()
}