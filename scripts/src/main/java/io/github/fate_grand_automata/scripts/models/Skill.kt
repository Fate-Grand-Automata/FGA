package io.github.fate_grand_automata.scripts.models

sealed class Skill(val autoSkillCode: Char) {
    sealed class Servant(autoSkillCode: Char) : Skill(autoSkillCode) {
        data object A1 : Servant('a')
        data object A2 : Servant('b')
        data object A3 : Servant('c')

        data object B1 : Servant('d')
        data object B2 : Servant('e')
        data object B3 : Servant('f')

        data object C1 : Servant('g')
        data object C2 : Servant('h')
        data object C3 : Servant('i')

        companion object {
            val list by lazy { listOf(A1, A2, A3, B1, B2, B3, C1, C2, C3) }

            val skill1 by lazy {
                listOf(A1, B1, C1)
            }
            val skill2 by lazy {
                listOf(A2, B2, C2)
            }
            val skill3 by lazy {
                listOf(A3, B3, C3)
            }
        }
    }

    sealed class Master(autoSkillCode: Char) : Skill(autoSkillCode) {
        data object A : Master('j')
        data object B : Master('k')
        data object C : Master('l')

        companion object {
            val list by lazy { listOf(A, B, C) }
        }
    }

    sealed class CommandSpell(autoSkillCode: Char) : Skill(autoSkillCode) {
        // full NP
        data object CS1 : CommandSpell('m')

        // full HP
        data object CS2 : CommandSpell('n')

        companion object {
            val list by lazy { listOf(CS1, CS2) }
        }
    }
}