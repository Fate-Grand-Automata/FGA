package com.mathewsachin.fategrandautomata.scripts.enums

import com.mathewsachin.libautomata.Location

enum class SupportClass constructor(val clickLocation: Location) {
    None(Location()),
    All(Location(184, 256)),
    Saber(Location(320, 256)),
    Archer(Location(454, 256)),
    Lancer(Location(568, 256)),
    Rider(Location(724, 256)),
    Caster(Location(858, 256)),
    Assassin(Location(994, 256)),
    Berserker(Location(1130, 256)),
    Extra(Location(1264, 256)),
    Mix(Location(1402, 256))
}