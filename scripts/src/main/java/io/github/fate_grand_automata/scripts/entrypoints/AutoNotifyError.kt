package io.github.fate_grand_automata.scripts.entrypoints

import io.github.fate_grand_automata.scripts.IFgoAutomataApi
import io.github.lib_automata.EntryPoint
import io.github.lib_automata.ExitManager
import io.github.lib_automata.dagger.ScriptScope
import javax.inject.Inject

@ScriptScope
class AutoNotifyError @Inject constructor(
    exitManager: ExitManager,
    api: IFgoAutomataApi
) : EntryPoint(exitManager), IFgoAutomataApi by api {

    sealed class ExitReason {
        data object Abort : ExitReason()
    }

    data class ExitException(val reason: ExitReason) : Exception()
    override fun script(): Nothing {
        // do nothing
        throw ExitException(ExitReason.Abort)
    }
}