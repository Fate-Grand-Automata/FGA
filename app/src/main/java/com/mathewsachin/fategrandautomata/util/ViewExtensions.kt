package com.mathewsachin.fategrandautomata.util

import android.view.View

fun View.setThrottledClickListener(Listener: () -> Unit) {
    var isWorking = false

    setOnClickListener {
        if (isWorking) {
            return@setOnClickListener
        }

        isWorking = true

        try {
            Listener()
        }
        finally {
            isWorking = false
        }
    }
}