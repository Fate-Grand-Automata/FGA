package io.github.fate_grand_automata.scripts.enums

enum class ChainTypeEnum {
    Mighty,
    Buster,
    Arts,
    Quick,
    Avoid;

    companion object {
        val defaultOrder = listOf(Mighty, Buster, Arts, Quick, Avoid)
    }
}