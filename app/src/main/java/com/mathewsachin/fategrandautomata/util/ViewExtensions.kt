package com.mathewsachin.fategrandautomata.util

import android.view.View
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.scripts.enums.RefillResourceEnum

fun View.setThrottledClickListener(Listener: () -> Unit) {
    var isWorking = false

    setOnClickListener {
        if (isWorking) {
            return@setOnClickListener
        }

        isWorking = true

        try {
            Listener()
        } finally {
            isWorking = false
        }
    }
}

val RefillResourceEnum.stringRes
    get() = when (this) {
        RefillResourceEnum.SQ -> R.string.p_refill_type_sq
        RefillResourceEnum.Gold -> R.string.p_refill_type_gold
        RefillResourceEnum.Silver -> R.string.p_refill_type_silver
        RefillResourceEnum.Bronze -> R.string.p_refill_type_bronze
    }