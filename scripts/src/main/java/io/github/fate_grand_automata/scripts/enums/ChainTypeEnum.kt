package io.github.fate_grand_automata.scripts.enums

enum class ChainTypeEnum {
    Mighty,
    Buster,
    Arts,
    Quick,
    Avoid;

    companion object {
        /** The chains tried when Chain Priority is first turned on, in order. */
        val defaultOrder = listOf(Mighty, Buster, Arts, Quick)
    }
}
