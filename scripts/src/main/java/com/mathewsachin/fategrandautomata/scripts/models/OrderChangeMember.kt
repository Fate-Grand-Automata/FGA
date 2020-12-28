package com.mathewsachin.fategrandautomata.scripts.models

sealed class OrderChangeMember(val autoSkillCode: Char) {
    sealed class Starting(autoSkillCode: Char) : OrderChangeMember(autoSkillCode) {
        object A : Starting('1')
        object B : Starting('2')
        object C : Starting('3')

        companion object {
            val list = listOf(A, B, C)
        }
    }

    sealed class Sub(autoSkillCode: Char) : OrderChangeMember(autoSkillCode) {
        object A : Sub('1')
        object B : Sub('2')
        object C : Sub('3')

        companion object {
            val list = listOf(A, B, C)
        }
    }
}