package com.mathewsachin.fategrandautomata.util

import java.io.DataInputStream

fun Int.reverseBytes(): Int {
    return Integer.reverseBytes(this)
}

fun DataInputStream.readIntLE(): Int {
    return readInt().reverseBytes()
}