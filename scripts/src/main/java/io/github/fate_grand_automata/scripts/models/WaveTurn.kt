package io.github.fate_grand_automata.scripts.models

sealed class WaveTurn(
    val code: String,
) {
    /**
     * The ",#," string that represents the next wave.
     */
    data object Wave : WaveTurn(",#,")

    /**
     * The "," string that represents the end of the turn.
     */
    data object Turn : WaveTurn(",")
}
