package io.github.fate_grand_automata.scripts.enums

enum class CardTypeEnum {
    Buster,
    Arts,
    Quick,

    /**
     * Couldn't detect card type.
     * Can be because Attack screen didn't open up or because the servant is stunned/charmed.
     */
    Unknown
}