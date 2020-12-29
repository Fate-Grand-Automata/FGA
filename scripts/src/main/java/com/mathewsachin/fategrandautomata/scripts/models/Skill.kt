package com.mathewsachin.fategrandautomata.scripts.models

sealed class Skill(val autoSkillCode: Char) {
    sealed class Servant(autoSkillCode: Char) : Skill(autoSkillCode) {
        object A1 : Servant('a')
        object A2 : Servant('b')
        object A3 : Servant('c')

        object B1 : Servant('d')
        object B2 : Servant('e')
        object B3 : Servant('f')

        object C1 : Servant('g')
        object C2 : Servant('h')
        object C3 : Servant('i')

        companion object {
            val list = listOf(A1, A2, A3, B1, B2, B3, C1, C2, C3)
        }
    }

    sealed class Master(autoSkillCode: Char) : Skill(autoSkillCode) {
        object A : Master('j')
        object B : Master('k')
        object C : Master('l')

        companion object {
            val list = listOf(A, B, C)
        }
    }
}