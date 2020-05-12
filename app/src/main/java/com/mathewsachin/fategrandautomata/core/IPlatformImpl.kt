package com.mathewsachin.fategrandautomata.core

import java.io.InputStream

interface IPlatformImpl {
    val windowRegion: Region

    fun toast(Message: String)

    fun loadPattern(Stream: InputStream): IPattern

    fun getResizableBlankPattern(): IPattern

    fun messageBox(Title: String, Message: String)

    fun highlight(Region: Region, Seconds: Double)
}