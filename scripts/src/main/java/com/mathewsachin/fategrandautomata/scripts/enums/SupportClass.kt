package com.mathewsachin.fategrandautomata.scripts.enums

enum class SupportClass {
    None,
    All,
    Saber,
    Archer,
    Lancer,
    Rider,
    Caster,
    Assassin,
    Berserker,
    Extra,
    Mix
}

val SupportClass.canAlsoCheckAll get() =
    this !in listOf(SupportClass.None, SupportClass.All, SupportClass.Mix)