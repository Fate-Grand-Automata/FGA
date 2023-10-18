package io.github.fate_grand_automata.scripts.models

sealed class EnemyTarget(val autoSkillCode: Char) {
    data object A1 : EnemyTarget('1')
    data object B1 : EnemyTarget('2')
    data object C1 : EnemyTarget('3')

    // 6 enemy formation
    data object A2 : EnemyTarget('4')

    data object B2 : EnemyTarget('5')

    data object C2 : EnemyTarget('6')

    data object D2 : EnemyTarget('7')

    data object E2 : EnemyTarget('8')

    data object F2 : EnemyTarget('9')

    companion object {
        val list by lazy { listOf(A1, B1, C1, A2, B2, C2, D2, E2, F2) }
    }
}