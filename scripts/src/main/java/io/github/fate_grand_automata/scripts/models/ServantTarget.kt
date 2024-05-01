package io.github.fate_grand_automata.scripts.models

sealed class ServantTarget(val autoSkillCode: Char) {
    data object A : ServantTarget('1')
    data object B : ServantTarget('2')
    data object C : ServantTarget('3')

    // Emiya
    data object Left : ServantTarget('7')
    data object Right : ServantTarget('8')

    // Kukulkan
    data object Option1 : ServantTarget('K')
    data object Option2 : ServantTarget('U')

    // Mélusine
    data object Melusine : ServantTarget('M')

    // Soujuurou
    data object ChangeQuick : ServantTarget('Q')
    data object ChangeArts : ServantTarget('A')
    data object ChangeBuster : ServantTarget('B')

    companion object {
        val list by lazy {
            listOf(
                A, B, C,
                Left, Right,
                Option1, Option2,
                Melusine,
                ChangeQuick, ChangeBuster, ChangeArts
            )
        }
    }
}