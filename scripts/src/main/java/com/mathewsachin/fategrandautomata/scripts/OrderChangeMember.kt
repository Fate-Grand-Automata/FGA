package com.mathewsachin.fategrandautomata.scripts

import com.mathewsachin.libautomata.Location

sealed class OrderChangeMember(
    val clickLocation: Location,
    val autoSkillCode: Char
) {
    class Starting private constructor(
        clickLocation: Location,
        autoSkillCode: Char
    ) : OrderChangeMember(clickLocation, autoSkillCode) {
        companion object {
            val list = listOf(
                Starting(Location(280, 700), '1'),
                Starting(Location(680, 700), '2'),
                Starting(Location(1080, 700), '3')
            )
        }
    }

    class Sub private constructor(
        clickLocation: Location,
        autoSkillCode: Char
    ) : OrderChangeMember(clickLocation, autoSkillCode) {
        companion object {
            val list = listOf(
                Sub(Location(1480, 700), '1'),
                Sub(Location(1880, 700), '2'),
                Sub(Location(2280, 700), '3')
            )
        }
    }
}