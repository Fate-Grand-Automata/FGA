package com.mathewsachin.fategrandautomata

import com.mathewsachin.libautomata.IPattern
import com.mathewsachin.libautomata.IPlatformImpl
import com.mathewsachin.libautomata.IPlatformPrefs
import com.mathewsachin.libautomata.Region
import kotlin.time.Duration

class FakePlatformImpl(override val windowRegion: Region) : IPlatformImpl {
    override val prefs: IPlatformPrefs
        get() = TODO("Not yet implemented")

    override fun toast(Message: String) {}
    override fun notify(message: String) {}

    override fun getResizableBlankPattern(): IPattern {
        TODO("Not yet implemented")
    }

    override fun messageBox(Title: String, Message: String, Error: Exception?) {}

    override fun highlight(Region: Region, Duration: Duration) {}
}