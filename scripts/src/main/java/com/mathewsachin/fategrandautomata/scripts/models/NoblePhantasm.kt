package com.mathewsachin.fategrandautomata.scripts.models

import com.mathewsachin.libautomata.Location
import com.mathewsachin.libautomata.Region

class NoblePhantasm private constructor(
    val clickLocation: Location,
    val autoSkillCode: Char,
    val servantMatchRegion: Region,
    val servantCropRegion: Region
) {
    companion object {
        val list = listOf(
            NoblePhantasm(
                clickLocation = Location(1000, 220),
                autoSkillCode = '4',
                servantMatchRegion = Region(678, 190, 300, 200),
                servantCropRegion = Region(762, 290, 115, 65)
            ),
            NoblePhantasm(
                clickLocation = Location(1300, 400),
                autoSkillCode = '5',
                servantMatchRegion = Region(1138, 190, 300, 200),
                servantCropRegion = Region(1230, 290, 115, 65)
            ),
            NoblePhantasm(
                clickLocation = Location(1740, 400),
                autoSkillCode = '6',
                servantMatchRegion = Region(1606, 190, 300, 200),
                servantCropRegion = Region(1694, 290, 115, 65)
            )
        )
    }

    override fun toString() = "[${autoSkillCode}]"
}