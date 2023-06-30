package io.github.fate_grand_automata.scripts.models

data class NPUsage(val nps: Set<CommandCard.NP>, val cardsBeforeNP: Int) {
    companion object {
        val none = NPUsage(emptySet(), 0)
    }
}