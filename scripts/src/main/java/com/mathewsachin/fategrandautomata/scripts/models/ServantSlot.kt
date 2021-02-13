package com.mathewsachin.fategrandautomata.scripts.models

sealed class ServantSlot(val position: Int) {
    object A: ServantSlot(1)
    object B: ServantSlot(2)
    object C: ServantSlot(3)

    override fun toString() = "[$position]"
}