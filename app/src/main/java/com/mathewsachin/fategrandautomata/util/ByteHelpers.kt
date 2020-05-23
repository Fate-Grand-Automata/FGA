package com.mathewsachin.fategrandautomata.util

import java.io.DataInputStream

/**
 * Reverses the order of the bytes in this Int.
 */
fun Int.reverseBytes(): Int {
    return Integer.reverseBytes(this)
}

/**
 * Reads a little-endian 32-bit integer value.
 */
fun DataInputStream.readIntLE(): Int {
    return readInt().reverseBytes()
}