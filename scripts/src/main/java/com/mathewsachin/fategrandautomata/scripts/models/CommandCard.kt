package com.mathewsachin.fategrandautomata.scripts.models

import com.mathewsachin.libautomata.Location
import com.mathewsachin.libautomata.Region

sealed class CommandCard(
    val clickLocation: Location,
    val servantMatchRegion: Region,
    val servantCropRegion: Region
) {
    abstract val supportCheckRegion: Region

    class Face private constructor(
        val index: Int,
        clickLocation: Location,
        // see docs/card_affinity_regions.png
        val affinityRegion: Region,
        // see docs/card_type_regions.png
        val typeRegion: Region,
        servantMatchRegion: Region,
        servantCropRegion: Region
    ) : CommandCard(
        clickLocation,
        servantMatchRegion,
        servantCropRegion
    ) {
        companion object {
            val list = listOf(
                Face(
                    index = 1,
                    clickLocation = Location(300, 1000),
                    affinityRegion = Region(295, 650, 250, 200),
                    typeRegion = Region(0, 1060, 512, 200),
                    servantMatchRegion = Region(106, 800, 300, 200),
                    servantCropRegion = Region(200, 890, 115, 85)
                ),
                Face(
                    index = 2,
                    clickLocation = Location(750, 1000),
                    affinityRegion = Region(810, 650, 250, 200),
                    typeRegion = Region(512, 1060, 512, 200),
                    servantMatchRegion = Region(620, 800, 300, 200),
                    servantCropRegion = Region(714, 890, 115, 85)
                ),
                Face(
                    index = 3,
                    clickLocation = Location(1300, 1000),
                    affinityRegion = Region(1321, 650, 250, 200),
                    typeRegion = Region(1024, 1060, 512, 200),
                    servantMatchRegion = Region(1130, 800, 300, 200),
                    servantCropRegion = Region(1224, 890, 115, 85)
                ),
                Face(
                    index = 4,
                    clickLocation = Location(1800, 1000),
                    affinityRegion = Region(1834, 650, 250, 200),
                    typeRegion = Region(1536, 1060, 512, 200),
                    servantMatchRegion = Region(1644, 800, 300, 200),
                    servantCropRegion = Region(1738, 890, 115, 85)
                ),
                Face(
                    index = 5,
                    clickLocation = Location(2350, 1000),
                    affinityRegion = Region(2348, 650, 250, 200),
                    typeRegion = Region(2048, 1060, 512, 200),
                    servantMatchRegion = Region(2160, 800, 300, 200),
                    servantCropRegion = Region(2254, 890, 115, 85)
                )
            )
        }

        override val supportCheckRegion = affinityRegion + Location(-50, 100)

        override fun toString() = "$index"
    }

    class NP private constructor(
        clickLocation: Location,
        val autoSkillCode: Char,
        servantMatchRegion: Region,
        servantCropRegion: Region
    ) : CommandCard(
        clickLocation,
        servantMatchRegion,
        servantCropRegion
    ) {
        companion object {
            val list = listOf(
                NP(
                    clickLocation = Location(1000, 220),
                    autoSkillCode = '4',
                    servantMatchRegion = Region(678, 190, 300, 200),
                    servantCropRegion = Region(762, 290, 115, 65)
                ),
                NP(
                    clickLocation = Location(1300, 400),
                    autoSkillCode = '5',
                    servantMatchRegion = Region(1138, 190, 300, 200),
                    servantCropRegion = Region(1230, 290, 115, 65)
                ),
                NP(
                    clickLocation = Location(1740, 400),
                    autoSkillCode = '6',
                    servantMatchRegion = Region(1606, 190, 300, 200),
                    servantCropRegion = Region(1694, 290, 115, 65)
                )
            )
        }

        override val supportCheckRegion = (servantMatchRegion + Location(110, 0))
            .copy(Height = 110)

        override fun toString() = "$autoSkillCode"
    }
}