package com.mathewsachin.fategrandautomata.scripts.models

import com.mathewsachin.fategrandautomata.scripts.enums.RefillResourceEnum
import com.mathewsachin.libautomata.Location

sealed class RefillResource {
    sealed class Single(val clickLocation: Location) : RefillResource() {
        object SaintQuartz : Single(Location(1270, 345))

        sealed class Apple(clickLocation: Location) : Single(clickLocation) {
            object Gold : Apple(Location(1270, 634))
            object Silver : Apple(Location(1270, 922))
            object Bronze : Apple(Location(1270, 1140))
        }
    }

    sealed class Multiple(val items: List<Single>) : RefillResource() {
        object AllApples : Multiple(
            listOf(
                Single.Apple.Bronze,
                Single.Apple.Silver,
                Single.Apple.Gold
            )
        )
    }

    companion object {
        fun of(value: RefillResourceEnum): RefillResource =
            when (value) {
                RefillResourceEnum.AllApples -> Multiple.AllApples
                RefillResourceEnum.Gold -> Single.Apple.Gold
                RefillResourceEnum.Silver -> Single.Apple.Silver
                RefillResourceEnum.Bronze -> Single.Apple.Bronze
                RefillResourceEnum.SQ -> Single.SaintQuartz
            }
    }
}