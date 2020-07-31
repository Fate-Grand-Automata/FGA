package com.mathewsachin.fategrandautomata.scripts

import com.mathewsachin.libautomata.Location

class ServantTarget private constructor(
    val clickLocation: Location,
    val autoSkillCode: Char
) {
    companion object {
        val list = listOf(
            ServantTarget(
                Location(700, 880),
                '1'
            ),
            ServantTarget(
                Location(1280, 880),
                '2'
            ),
            ServantTarget(
                Location(1940, 880),
                '3'
            )
        )
    }
}