package com.mathewsachin.fategrandautomata.scripts.models

sealed class ServantTarget(val autoSkillCode: Char) {
    object A : ServantTarget('1')
    object B : ServantTarget('2')
    object C : ServantTarget('3')

    // Emiya
    object Left : ServantTarget('7')
    object Right : ServantTarget('8')

    companion object {
        val list = listOf(A, B, C, Left, Right)
    }
}