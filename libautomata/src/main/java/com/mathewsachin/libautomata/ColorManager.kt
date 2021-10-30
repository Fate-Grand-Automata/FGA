package com.mathewsachin.libautomata

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ColorManager @Inject constructor() {
    var isColor = false
        private set

    fun <T> useColor(block: () -> T): T {
        try {
            isColor = true

            return block()
        } finally {
            isColor = false
        }
    }
}