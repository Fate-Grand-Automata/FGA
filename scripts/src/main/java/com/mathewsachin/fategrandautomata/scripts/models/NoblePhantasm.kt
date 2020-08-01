package com.mathewsachin.fategrandautomata.scripts.models

import com.mathewsachin.libautomata.Location

class NoblePhantasm private constructor(
    val clickLocation: Location,
    val autoSkillCode: Char
) {
    companion object {
        val list = listOf(
            NoblePhantasm(
                Location(1000, 220),
                '4'
            ),
            NoblePhantasm(
                Location(1300, 400),
                '5'
            ),
            NoblePhantasm(
                Location(1740, 400),
                '6'
            )
        )
    }
}