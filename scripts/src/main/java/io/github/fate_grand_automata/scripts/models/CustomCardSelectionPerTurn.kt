package io.github.fate_grand_automata.scripts.models

class CustomCardSelectionPerTurn private constructor(
    private val selectionPerTurn: List<CustomCardSelection>
) : List<CustomCardSelection> by selectionPerTurn {

    /**
     * Gets the selection for current turn
     * Returns empty selection when index is out of bounds.
     */
    fun atTurn(turn: Int): CustomCardSelection {
        val index = turn - 1
        return if (index in selectionPerTurn.indices) {
            selectionPerTurn[index]
        } else {
            CustomCardSelection.empty
        }
    }

    override fun toString() =
        selectionPerTurn.joinToString(separator)

    companion object {
        private const val separator = ","

        val empty = CustomCardSelectionPerTurn(listOf(CustomCardSelection.empty))

        fun from(selectionsPerTurn: List<CustomCardSelection>) =
            CustomCardSelectionPerTurn(selectionsPerTurn)

        fun of(selection: String): CustomCardSelectionPerTurn =
            if (selection.isBlank()) {
                empty
            } else {
                CustomCardSelectionPerTurn(
                    selection
                        .split(separator)
                        .map { CustomCardSelection.of(it) }
                )
            }
    }
}
