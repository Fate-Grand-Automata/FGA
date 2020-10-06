package com.mathewsachin.libautomata

/**
 * Interface for image objects.
 */
interface IPattern : AutoCloseable {
    val width: Int
    val height: Int

    /**
     * Creates a resized image with the specified size.
     *
     * @param Size the size of the new image
     */
    fun resize(Size: Size): IPattern

    /**
     * Creates a resized image with the specified size and writes it into the target.
     *
     * @param Target the image to write the resized image data to
     * @param Size the size of the new image
     */
    fun resize(Target: IPattern, Size: Size)

    /**
     * Finds all image matches with high enough similarity values.
     *
     * @param Template the image to match with
     * @param Similarity the minimum similarity
     *
     * @return a list of [Match] objects
     */
    fun findMatches(Template: IPattern, Similarity: Double): Sequence<Match>

    /**
     * Crops the image to be within the bounds of the given [Region].
     *
     * Note that the resulting [IPattern] can have a smaller size than the [Region] if the [Region]
     * is not fully contained in the area of the image.
     *
     * @param Region a [Region] in image coordinates, see [Region.transformToImage]
     */
    fun crop(Region: Region): IPattern

    /**
     * Saves the image data to the given file path.
     *
     * @param FileName an absolute file path pointing to the save location
     */
    fun save(FileName: String)

    /**
     * Makes a copy of the image.
     */
    fun copy(): IPattern

    fun tag(tag: String): IPattern
}

/**
 * Gets the width and height in the form of a [Size] object.
 */
val IPattern.Size get() = Size(width, height)