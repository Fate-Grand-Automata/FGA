package com.mathewsachin.fategrandautomata.scripts.models

sealed class TeamSlot(val position: Int) {
    object A: TeamSlot(1)
    object B: TeamSlot(2)
    object C: TeamSlot(3)
    object D: TeamSlot(4)
    object E: TeamSlot(5)
    object F: TeamSlot(6)

    override fun toString() = "[$position]"

    companion object {
        val list by lazy {
            listOf(A, B, C, D, E, F)
        }
    }
}