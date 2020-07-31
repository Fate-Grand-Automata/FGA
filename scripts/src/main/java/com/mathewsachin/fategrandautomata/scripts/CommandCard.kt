package com.mathewsachin.fategrandautomata.scripts

import com.mathewsachin.libautomata.Location
import com.mathewsachin.libautomata.Region

class CommandCard private constructor(
    val clickLocation: Location,
    // see docs/card_affinity_regions.png
    val affinityRegion: Region,
    // see docs/card_type_regions.png
    val typeRegion: Region
) {
    companion object {
        val list = listOf(
            CommandCard(
                clickLocation = Location(300, 1000),
                affinityRegion = Region(295, 650, 250, 200),
                typeRegion = Region(0, 1060, 512, 200)
            ),
            CommandCard(
                clickLocation = Location(750, 1000),
                affinityRegion = Region(810, 650, 250, 200),
                typeRegion = Region(512, 1060, 512, 200)
            ),
            CommandCard(
                clickLocation = Location(1300, 1000),
                affinityRegion = Region(1321, 650, 250, 200),
                typeRegion = Region(1024, 1060, 512, 200)
            ),
            CommandCard(
                clickLocation = Location(1800, 1000),
                affinityRegion = Region(1834, 650, 250, 200),
                typeRegion = Region(1536, 1060, 512, 200)
            ),
            CommandCard(
                clickLocation = Location(2350, 1000),
                affinityRegion = Region(2348, 650, 250, 200),
                typeRegion = Region(2048, 1060, 512, 200)
            )
        )
    }
}