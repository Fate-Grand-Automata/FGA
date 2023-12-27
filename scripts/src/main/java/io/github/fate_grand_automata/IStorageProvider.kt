package io.github.fate_grand_automata

import io.github.lib_automata.Pattern
import java.io.File
import java.io.InputStream
import java.io.OutputStream

interface IStorageProvider {
    val supportImageTempDir: File

    fun writeSupportImage(kind: SupportImageKind, name: String): OutputStream

    fun readSupportImage(kind: SupportImageKind, name: String): List<InputStream>

    fun list(kind: SupportImageKind): List<String>

    fun dropScreenshot(patterns: List<Pattern>)

    fun dropBondScreenShot(pattern: Pattern)

    /**
     * For debugging images
     *
     * ```
     * class Sample @Inject constructor(
     *     private val screenshotService: ScreenshotService,
     *     private val storageProvider: IStorageProvider,
     *     private val transform: Transformer){
     *
     * ....
     *
     * fun method(){
     *     val region = ...
     *     val dumpImage = screenshotService
     *         .takeScreenshot()
     *         .crop(transform.toImage(region))
     *     storageProvider.dump(name, dumpImage)
     * }
     * ```
     *
     * Can also add color by using this
     * ```
     * useColor {
     *     val region = ...
     *     val dumpImage = screenshotService
     *         .takeScreenshot()
     *         .crop(transform.toImage(region))
     *     storageProvider.dump(name, dumpImage)
     * }
     * ```
     *
     * @see io.github.lib_automata.ScreenshotService
     * @see io.github.lib_automata.Transformer
     * @see io.github.lib_automata.Pattern
     * @see io.github.lib_automata.AutomataApi.useColor
     */
    fun dump(name: String, image: Pattern)

    fun createNoMediaFile()
}