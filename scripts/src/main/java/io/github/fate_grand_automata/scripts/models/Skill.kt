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
}

fun Skill.Servant.getFieldSlot(): FieldSlot = when (this) {
    Skill.Servant.A1, Skill.Servant.A2, Skill.Servant.A3 -> FieldSlot.A
    Skill.Servant.B1, Skill.Servant.B2, Skill.Servant.B3 -> FieldSlot.B
    Skill.Servant.C1, Skill.Servant.C2, Skill.Servant.C3 -> FieldSlot.C
}

fun Skill.Servant.getSkillIndex(): Int = when (this) {
    Skill.Servant.A1, Skill.Servant.B1, Skill.Servant.C1 -> 0
    Skill.Servant.A2, Skill.Servant.B2, Skill.Servant.C2 -> 1
    Skill.Servant.A3, Skill.Servant.B3, Skill.Servant.C3 -> 2
}