package com.mathewsachin.libautomata

import java.io.OutputStream

/**
 * Interface for image objects.
 */
interface IPattern : AutoCloseable {
    val width: Int
    val height: Int

    /**
     * Creates a resized image with the specified size.
     *
     * @param size the size of the new image
     */
    fun resize(size: Size): IPattern

    /**
     * Creates a resized image with the specified size and writes it into the target.
     *
     * @param target the image to write the resized image data to
     * @param size the size of the new image
     */
    fun resize(target: IPattern, size: Size)

    /**
     * Finds all image matches with high enough similarity values.
     *
     * @param template the image to match with
     * @param similarity the minimum similarity
     *
     * @return a list of [Match] objects
     */
    fun findMatches(template: IPattern, similarity: Double): Sequence<Match>

    /**
     * Crops the image to be within the bounds of the given [region].
     *
     * Note that the resulting [IPattern] can have a smaller size than the [region] if the [region]
     * is not fully contained in the area of the image.
     *
     * @param region a [Region] in image coordinates, see [Region.transformToImage]
     */
    fun crop(region: Region): IPattern

    fun save(stream: OutputStream)

    /**
     * Makes a copy of the image.
     */
    fun copy(): IPattern

    fun tag(tag: String): IPattern

    fun threshold(value: Double): IPattern
}

/**
 * Gets the width and height in the form of a [Size] object.
 */
val IPattern.size get() = Size(width, height)