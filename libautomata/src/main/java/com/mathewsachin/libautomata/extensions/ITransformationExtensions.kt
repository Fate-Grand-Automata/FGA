package com.mathewsachin.libautomata.extensions

import com.mathewsachin.libautomata.Location
import com.mathewsachin.libautomata.Region

interface ITransformationExtensions {
    /**
     * Calculates the ratio between the screen resolution and the image resolution.
     *
     * @return the ratio or `null` if the resolution is the same
     */
    fun screenToImageScale(): Double?

    /**
     * Calculates the ratio between the script resolution and the screen resolution.
     */
    fun scriptToScreenScale(): Double

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
     * Calculates the ratio between the script resolution and the image resolution.
     */
    fun scriptToImageScale(): Double

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