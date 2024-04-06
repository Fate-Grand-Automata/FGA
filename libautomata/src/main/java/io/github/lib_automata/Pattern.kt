package io.github.lib_automata

import java.io.OutputStream

/**
 * Interface for image objects.
 */
interface Pattern : AutoCloseable {
    val width: Int
    val height: Int
    var tag: String

    /**
     * Creates a resized image with the specified size.
     *
     * @param size the size of the new image
     */
    fun resize(size: Size): Pattern

    /**
     * Creates a resized image with the specified size and writes it into the target.
     *
     * @param target the image to write the resized image data to
     * @param size the size of the new image
     */
    fun resize(target: Pattern, size: Size)

    /**
     * Finds all image matches with high enough similarity values.
     *
     * @param template the image to match with
     * @param similarity the minimum similarity
     *
     * @return a list of [Match] objects
     */
    fun findMatches(template: Pattern, similarity: Double): Sequence<Match>

    /**
     * Crops the image to be within the bounds of the given [region].
     *
     * Note that the resulting [Pattern] can have a smaller size than the [region] if the [region]
     * is not fully contained in the area of the image.
     *
     * @param region a [Region] in image coordinates, see [Transformer.toImage]
     */
    fun crop(region: Region): Pattern

    fun save(stream: OutputStream)

    /**
     * Makes a copy of the image.
     */
    fun copy(): Pattern

    fun threshold(value: Double): Pattern

    fun dynamic(): Pattern

    fun isWhite(): Boolean

    fun isBlack(): Boolean

    fun floodFill(x: Double, y: Double, maxDiff: Double, newValue: Double): Pattern

    fun fillText(): Pattern
}

/**
 * Gets the width and height in the form of a [Size] object.
 */
val Pattern.size get() = Size(width, height)