package com.mathewsachin.fategrandautomata.core

class Stopwatch {
    private var timestamp = System.currentTimeMillis()

    fun start() {
        timestamp = System.currentTimeMillis()
    }

    val elapsedMs get() = System.currentTimeMillis() - timestamp

    val elapsedSec get() = elapsedMs / 1000.0
}