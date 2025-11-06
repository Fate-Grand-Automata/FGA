package io.github.fate_grand_automata.scripts.enums

enum class StarConditionEnum(val lower: Int, val upper: Int) {
    None(-1, -1),
    BelowTen(0, 9),
    BelowFifty(0, 49),
    AtLeastTen(10, 99),
    AtLeastFifty(50, 99)
}

fun StarConditionEnum.contains(value: Int): Boolean {
    return this != StarConditionEnum.None && value in lower..upper
}