package com.mathewsachin.fategrandautomata.scripts.models

import com.mathewsachin.fategrandautomata.scripts.enums.RefillResourceEnum
import com.mathewsachin.libautomata.Location

sealed class RefillResource(val clickLocation: Location) {
    object SaintQuartz : RefillResource(Location(750, 345))

    sealed class Apple(clickLocation: Location) : RefillResource(clickLocation) {
        object Gold : Apple(Location(750, 634))
        object Silver : Apple(Location(750, 922))
        object Bronze : Apple(Location(750, 1140))
    }

    companion object {
        fun of(value: RefillResourceEnum): RefillResource =
            when (value) {
                RefillResourceEnum.Gold -> Apple.Gold
                RefillResourceEnum.Silver -> Apple.Silver
                RefillResourceEnum.Bronze -> Apple.Bronze
                RefillResourceEnum.SQ -> SaintQuartz
            }
    }
}