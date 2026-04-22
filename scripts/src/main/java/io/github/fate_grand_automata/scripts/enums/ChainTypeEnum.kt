package io.github.fate_grand_automata.scripts.enums

enum class ChainTypeEnum {
    Mighty,
    Buster,
    Arts,
    Quick,
    None,
    Avoid;

    companion object {
        val Cutoff = None
        val defaultOrder = listOf(Mighty, Buster, Arts, Quick, None, Avoid)
        val allPermitted = listOf(Mighty, Buster, Arts, Quick, Avoid)
    }
}