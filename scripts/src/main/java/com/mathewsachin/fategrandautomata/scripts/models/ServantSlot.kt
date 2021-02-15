package com.mathewsachin.fategrandautomata.scripts.models

sealed class ServantSlot(val position: Int) {
    object A: ServantSlot(1)
    object B: ServantSlot(2)
    object C: ServantSlot(3)

    companion object {
        val list by lazy { listOf(A, B, C) }
    }

    override fun toString() = "[$position]"
}

fun ServantSlot.skill1() =
    when (this) {
        ServantSlot.A -> Skill.Servant.A1
        ServantSlot.B -> Skill.Servant.B1
        ServantSlot.C -> Skill.Servant.C1
    }

fun ServantSlot.skill2() =
    when (this) {
        ServantSlot.A -> Skill.Servant.A2
        ServantSlot.B -> Skill.Servant.B2
        ServantSlot.C -> Skill.Servant.C2
    }

fun ServantSlot.skill3() =
    when (this) {
        ServantSlot.A -> Skill.Servant.A3
        ServantSlot.B -> Skill.Servant.B3
        ServantSlot.C -> Skill.Servant.C3
    }

fun ServantSlot.skills() =
    listOf(skill1(), skill2(), skill3())