package com.mathewsachin.libautomata.extensions

import com.mathewsachin.libautomata.Location
import com.mathewsachin.libautomata.Region

interface ITransformationExtensions {
    /**
     * Transforms the current [Location] in script coordinates to a new one in screen coordinates.
     *
     * @return a new [Location] in screen coordinates
     */
    fun Location.transform(): Location

    /**
     * Transforms the current [Region] in script coordinates to a new one in screen coordinates.
     *
     * @return a new [Region] in screen coordinates
     */
    fun Region.transform(): Region

    /**
     * Transforms the [Region] from script coordinates to image coordinates.
     *
     * Normally, the script coordinates are in 1440p while the image coordinates are in 720p.
     *
     * @return a new [Region] with the same position in screen coordinates.
     */
    fun Region.transformToImage(): Region

    /**
     * Transforms the [Region] from image coordinates to script coordinates.
     *
     * Normally, the script coordinates are in 1440p while the image coordinates are in 720p.
     *
     * @return a new [Region] with the same position in script coordinates.
     */
    fun Region.transformFromImage(): Region
}