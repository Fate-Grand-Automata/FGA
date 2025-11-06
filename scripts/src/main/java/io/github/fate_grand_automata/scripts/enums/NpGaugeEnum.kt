package io.github.fate_grand_automata.scripts.enums

enum class NpGaugeEnum(val lower: Int, val upper: Int) {
    None(-1, -1),   // skip NP check
    Low(0, 99),    // NP < 100%
    Ready(100, 300),   // 100% <= NP
    AtLeast10(10, 300),
    AtLeast50(50, 300)
}