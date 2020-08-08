package com.mathewsachin.fategrandautomata.scripts.models

import com.mathewsachin.libautomata.Location
import com.mathewsachin.libautomata.Region

class CommandCard private constructor(
    val index: Int,
    val clickLocation: Location,
    // see docs/card_affinity_regions.png
    val affinityRegion: Region,
    // see docs/card_type_regions.png
    val typeRegion: Region,
    val servantMatchRegion: Region,
    val servantCropRegion: Region
) {
    companion object {
        val list = listOf(
            CommandCard(
                index = 1,
                clickLocation = Location(300, 1000),
                affinityRegion = Region(295, 650, 250, 200),
                typeRegion = Region(0, 1060, 512, 200),
                servantMatchRegion = Region(106, 800, 300, 200),
                servantCropRegion = Region(200, 890, 115, 85)
            ),
            CommandCard(
                index = 2,
                clickLocation = Location(750, 1000),
                affinityRegion = Region(810, 650, 250, 200),
                typeRegion = Region(512, 1060, 512, 200),
                servantMatchRegion = Region(620, 800, 300, 200),
                servantCropRegion = Region(714, 890, 115, 85)
            ),
            CommandCard(
                index = 3,
                clickLocation = Location(1300, 1000),
                affinityRegion = Region(1321, 650, 250, 200),
                typeRegion = Region(1024, 1060, 512, 200),
                servantMatchRegion = Region(1130, 800, 300, 200),
                servantCropRegion = Region(1224, 890, 115, 85)
            ),
            CommandCard(
                index = 4,
                clickLocation = Location(1800, 1000),
                affinityRegion = Region(1834, 650, 250, 200),
                typeRegion = Region(1536, 1060, 512, 200),
                servantMatchRegion = Region(1644, 800, 300, 200),
                servantCropRegion = Region(1738, 890, 115, 85)
            ),
            CommandCard(
                index = 5,
                clickLocation = Location(2350, 1000),
                affinityRegion = Region(2348, 650, 250, 200),
                typeRegion = Region(2048, 1060, 512, 200),
                servantMatchRegion = Region(2160, 800, 300, 200),
                servantCropRegion = Region(2254, 890, 115, 85)
            )
        )
    }

    val supportCheckRegion = affinityRegion + Location(-50, 100)

    override fun toString() = "[${index}]"
}