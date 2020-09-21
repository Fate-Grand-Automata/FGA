package com.mathewsachin.fategrandautomata.scripts

import java.io.InputStream
import java.io.OutputStream

interface ITemporaryStore {
    fun read(key: String): InputStream

    fun write(key: String): OutputStream

    fun delete(key: String)

    fun exists(key: String): Boolean

    fun clear()
}