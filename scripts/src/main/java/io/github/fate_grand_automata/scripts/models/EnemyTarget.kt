package io.github.fate_grand_automata.scripts.models

sealed class EnemyTarget(val autoSkillCode: Char) {
    data object A3 : EnemyTarget('1')
    data object B3 : EnemyTarget('2')
    data object C3 : EnemyTarget('3')

    // 6 enemy formation
    data object A6 : EnemyTarget('4')

    data object B6 : EnemyTarget('5')

    data object C6 : EnemyTarget('6')

    data object D6 : EnemyTarget('7')

    data object E6 : EnemyTarget('8')

    data object F6 : EnemyTarget('9')

    companion object {
        val list by lazy {
            listOf(
                A3, B3, C3,
                A6, B6, C6, D6, E6, F6
            )
        }
        val threeEnemyFormationList by lazy {
            listOf(
                A3, B3, C3
            )
        }

        val sixEnemyFormationCharList by lazy {
            listOf(
                A6.autoSkillCode, B6.autoSkillCode, C6.autoSkillCode,
                D6.autoSkillCode, E6.autoSkillCode, F6.autoSkillCode
            )
        }
    }
}

enum class EnemyFormation {
    THREE,
    SIX,
    // RAID
}