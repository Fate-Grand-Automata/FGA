package io.github.fate_grand_automata.scripts.models

sealed class ServantTarget(val autoSkillCode: Char) {
    object A : ServantTarget('1')
    object B : ServantTarget('2')
    object C : ServantTarget('3')

    // Emiya
    object Left : ServantTarget('7')
    object Right : ServantTarget('8')

    // Kukulkan
    object Option1 : ServantTarget('K')
    object Option2 : ServantTarget('U')

    companion object {
        val list by lazy { listOf(A, B, C, Left, Right, Option1, Option2) }
    }
}