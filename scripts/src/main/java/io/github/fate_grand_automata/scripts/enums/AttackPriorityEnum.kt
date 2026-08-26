package io.github.fate_grand_automata.scripts.enums

enum class AttackPriorityEnum {
    BraveChainPriority,
    CardChainPriority,
    ServantPriority,
    CardColorPriority;

    companion object {
        // It is defined separately here instead of using the order of the enum definition
        // because some parts might be removed or changed in future when the actual AttackPriority implementation is done
        // and this will prevent the app from breaking when serializing or deserializing content from this enum.
        val defaultOrder = listOf(BraveChainPriority, CardChainPriority, ServantPriority, CardColorPriority)
    }
}