package com.mathewsachin.fategrandautomata.scripts

import com.mathewsachin.libautomata.Location
import com.mathewsachin.libautomata.Region

class EnemyTarget private constructor(
    val region: Region,
    val clickLocation: Location,
    val autoSkillCode: Char
) {
    companion object {
        // see docs/target_regions.png
        val list = listOf(
            EnemyTarget(
                Region(0, 0, 485, 220),
                Location(90, 80),
                '1'
            ),
            EnemyTarget(
                Region(485, 0, 482, 220),
                Location(570, 80),
                '2'
            ),
            EnemyTarget(
                Region(967, 0, 476, 220),
                Location(1050, 80),
                '3'
            )
        )
    }
}