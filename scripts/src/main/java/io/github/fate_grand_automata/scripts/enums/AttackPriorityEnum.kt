package io.github.fate_grand_automata.scripts.enums

enum class AttackPriorityEnum {
    BraveChainPriority,
    CardChainPriority,
    ServantPriority,
    CardColorPriority;

    companion object {
        val defaultOrder = listOf(BraveChainPriority, CardChainPriority, ServantPriority, CardColorPriority)
    }
}