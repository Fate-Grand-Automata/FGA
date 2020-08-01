package com.mathewsachin.fategrandautomata.scripts.models

import com.mathewsachin.libautomata.Location

sealed class BoostItem {
    // see docs/menu_boost_item_click_array.png

    object Disabled : BoostItem()
    sealed class Enabled(val clickLocation: Location) : BoostItem() {
        object Skip : Enabled(Location(1652, 1304))

        object BoostItem1 : Enabled(Location(1280, 418))
        object BoostItem2 : Enabled(Location(1280, 726))
        object BoostItem3 : Enabled(Location(1280, 1000))
    }

    companion object {
        fun of(value: Int): BoostItem =
            when (value) {
                0 -> Enabled.Skip
                1 -> Enabled.BoostItem1
                2 -> Enabled.BoostItem2
                3 -> Enabled.BoostItem3
                else -> Disabled
            }
    }
}