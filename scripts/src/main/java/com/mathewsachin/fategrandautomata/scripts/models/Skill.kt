package com.mathewsachin.fategrandautomata.scripts.models

import com.mathewsachin.libautomata.Location

sealed class Skill(
    val clickLocation: Location,
    val autoSkillCode: Char
) {
    class Servant private constructor(
        clickLocation: Location,
        autoSkillCode: Char
    ) : Skill(clickLocation, autoSkillCode) {
        companion object {
            val list = listOf(
                Servant(Location(140, 1160), 'a'),
                Servant(Location(340, 1160), 'b'),
                Servant(Location(540, 1160), 'c'),

                Servant(Location(770, 1160), 'd'),
                Servant(Location(970, 1160), 'e'),
                Servant(Location(1140, 1160), 'f'),

                Servant(Location(1400, 1160), 'g'),
                Servant(Location(1600, 1160), 'h'),
                Servant(Location(1800, 1160), 'i')
            )
        }
    }

    class Master private constructor(
        clickLocation: Location,
        autoSkillCode: Char
    ) : Skill(clickLocation, autoSkillCode) {
        companion object {
            val list = listOf(
                Master(Location(1820, 620), 'j'),
                Master(Location(2000, 620), 'k'),
                Master(Location(2160, 620), 'l')
            )
        }
    }
}