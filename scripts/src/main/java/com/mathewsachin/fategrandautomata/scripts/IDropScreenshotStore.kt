package com.mathewsachin.fategrandautomata.scripts

import com.mathewsachin.libautomata.IPattern

interface IDropScreenshotStore {
    fun insert(images: List<IPattern>)
}