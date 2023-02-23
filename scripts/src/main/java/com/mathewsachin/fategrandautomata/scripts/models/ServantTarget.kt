package com.mathewsachin.fategrandautomata.scripts.models

sealed class ServantTarget(val autoSkillCode: Char) {
    object A : ServantTarget('1')
    object B : ServantTarget('2')
    object C : ServantTarget('3')

    // Emiya
    object Left : ServantTarget('7')
    object Right : ServantTarget('8')

    // Kukulcan
    object Option1 : ServantTarget('K')
    object Option2 : ServantTarget('U')

    companion object {
        val list by lazy { listOf(A, B, C, Left, Right, Option1, Option2) }
    }
}