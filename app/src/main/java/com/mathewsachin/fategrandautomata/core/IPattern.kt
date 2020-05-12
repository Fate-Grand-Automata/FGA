package com.mathewsachin.fategrandautomata.core

interface IPattern : AutoCloseable {
    fun resize(Size: Size): IPattern

    fun resize(Target: IPattern, Size: Size)

    fun isMatch(Template: IPattern, Similarity: Double): Boolean

    fun findMatches(Template: IPattern, Similarity: Double): Sequence<Match>

    val width: Int
    val height: Int

    fun crop(Region: Region): IPattern

    fun save(FileName: String)

    fun copy(): IPattern
}