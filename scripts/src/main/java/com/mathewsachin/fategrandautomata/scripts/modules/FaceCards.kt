package com.mathewsachin.fategrandautomata.scripts.modules

import com.mathewsachin.fategrandautomata.scripts.IFGAutomataApi
import com.mathewsachin.libautomata.Region

object BattleServantCards {
    val faceCardRegions = listOf(
        Region(106, 800, 300, 200),
        Region(620, 800, 300, 200),
        Region(1130, 800, 300, 200),
        Region(1644, 800, 300, 200),
        Region(2160, 800, 300, 200)
    )

    val npRegions = listOf(
        Region(678, 190, 300, 200),
        Region(1138, 190, 300, 200),
        Region(1606, 190, 300, 200)
    )

    val faceCardCropRegions = listOf(
        Region(200, 890, 115, 85),
        Region(714, 890, 115, 85),
        Region(1224, 890, 115, 85),
        Region(1738, 890, 115, 85),
        Region(2254, 890, 115, 85)
    )

    val npCropRegions = listOf(
        Region(762, 290, 115, 65),
        Region(1230, 290, 115, 65),
        Region(1694, 290, 115, 65)
    )
}

fun IFGAutomataApi.groupNpsWithFaceCards(groups: List<List<Int>>): List<List<Int>> {
    return BattleServantCards.npCropRegions
        .map { region ->
            region.getPattern().use { npCropped ->
                groups.maxBy {
                    BattleServantCards.faceCardRegions[it[0]]
                        .findAll(npCropped, 0.4)
                        .firstOrNull()?.score ?: 0.0
                } ?: emptyList()
            }
        }
}

fun IFGAutomataApi.groupByFaceCard(): List<List<Int>> {
    val remaining = BattleServantCards.faceCardRegions.indices.toMutableSet()
    val groups = mutableListOf<List<Int>>()

    while (remaining.isNotEmpty()) {
        val u = remaining.first()
        remaining.remove(u)

        val group = mutableListOf<Int>()
        group.add(u)

        if (remaining.isNotEmpty()) {
            val me = BattleServantCards.faceCardCropRegions[u].getPattern()

            me.use {
                val matched = remaining.filter {
                    val region = BattleServantCards.faceCardRegions[it]
                    region.exists(me)
                }

                remaining.removeAll(matched)
                group.addAll(matched)
            }
        }

        groups.add(group)
    }

    return groups
}