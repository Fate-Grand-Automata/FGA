package io.github.fate_grand_automata.scripts.models

sealed class CommandCard {
    sealed class Face(val index: Int) : CommandCard() {
        object A : Face(1)
        object B : Face(2)
        object C : Face(3)
        object D : Face(4)
        object E : Face(5)

        companion object {
            val list by lazy { listOf(A, B, C, D, E) }
        }

        override fun toString() = "$index"
    }

    sealed class NP(val autoSkillCode: Char) : CommandCard() {
        object A : NP('4')
        object B : NP('5')
        object C : NP('6')

        companion object {
            val list by lazy { listOf(A, B, C) }
        }

        override fun toString() = "$autoSkillCode"
    }
}

fun CommandCard.NP.toFieldSlot() =
    when (this) {
        CommandCard.NP.A -> FieldSlot.A
        CommandCard.NP.B -> FieldSlot.B
        CommandCard.NP.C -> FieldSlot.C
    }