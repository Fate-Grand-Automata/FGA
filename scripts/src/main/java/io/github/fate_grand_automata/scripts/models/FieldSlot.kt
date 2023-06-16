package io.github.fate_grand_automata.scripts.models

sealed class FieldSlot(val position: Int) {
    object A: FieldSlot(1)
    object B: FieldSlot(2)
    object C: FieldSlot(3)

    companion object {
        val list by lazy { listOf(A, B, C) }
    }

    override fun toString() = "[$position]"
}

fun FieldSlot.skill1() =
    when (this) {
        FieldSlot.A -> Skill.Servant.A1
        FieldSlot.B -> Skill.Servant.B1
        FieldSlot.C -> Skill.Servant.C1
    }

fun FieldSlot.skill2() =
    when (this) {
        FieldSlot.A -> Skill.Servant.A2
        FieldSlot.B -> Skill.Servant.B2
        FieldSlot.C -> Skill.Servant.C2
    }

fun FieldSlot.skill3() =
    when (this) {
        FieldSlot.A -> Skill.Servant.A3
        FieldSlot.B -> Skill.Servant.B3
        FieldSlot.C -> Skill.Servant.C3
    }

fun FieldSlot.skills() =
    listOf(skill1(), skill2(), skill3())