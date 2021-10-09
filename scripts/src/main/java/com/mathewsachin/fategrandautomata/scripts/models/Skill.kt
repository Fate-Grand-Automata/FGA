package com.mathewsachin.fategrandautomata.scripts.models

sealed class Skill(val autoSkillCode: Char) {
    sealed class Servant(
        val servantIndex: Int,
        val index: Int,
        autoSkillCode: Char
    ) : Skill(autoSkillCode) {
        object A1 : Servant(0, 0,'a')
        object A2 : Servant(0, 1, 'b')
        object A3 : Servant(0, 2, 'c')

        object B1 : Servant(1, 0, 'd')
        object B2 : Servant(1, 1, 'e')
        object B3 : Servant(1, 2, 'f')

        object C1 : Servant(2, 0, 'g')
        object C2 : Servant(2, 1, 'h')
        object C3 : Servant(2, 2, 'i')

        companion object {
            val list by lazy { listOf(A1, A2, A3, B1, B2, B3, C1, C2, C3) }
        }
    }

    sealed class Master(autoSkillCode: Char) : Skill(autoSkillCode) {
        object A : Master('j')
        object B : Master('k')
        object C : Master('l')

        companion object {
            val list by lazy { listOf(A, B, C) }
        }
    }
}