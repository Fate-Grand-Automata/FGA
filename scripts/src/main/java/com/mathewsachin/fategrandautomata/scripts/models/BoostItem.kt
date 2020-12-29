package com.mathewsachin.fategrandautomata.scripts.models

sealed class BoostItem {
    object Disabled : BoostItem()
    sealed class Enabled : BoostItem() {
        object Skip : Enabled()

        object BoostItem1 : Enabled()
        object BoostItem2 : Enabled()
        object BoostItem3 : Enabled()
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