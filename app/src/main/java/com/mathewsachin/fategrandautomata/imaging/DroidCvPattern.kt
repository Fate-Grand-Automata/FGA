package com.mathewsachin.fategrandautomata.imaging

import android.graphics.Bitmap
import com.mathewsachin.libautomata.IPattern
import com.mathewsachin.libautomata.Match
import com.mathewsachin.libautomata.Region
import com.mathewsachin.libautomata.Size
import org.opencv.core.*
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import timber.log.Timber
import timber.log.debug
import timber.log.verbose
import java.io.InputStream
import java.io.OutputStream
import kotlin.math.roundToInt
import org.opencv.core.Size as CvSize

class DroidCvPattern(
    private var mat: Mat = Mat(),
    private var alpha: Mat? = null,
    private val ownsMat: Boolean = true
) : IPattern {
    private data class MatWithAlpha(val mat: Mat, val alpha: Mat?)

    private companion object {
        fun makeMat(stream: InputStream): MatWithAlpha {
            val byteArray = stream.readBytes()
            MatOfByte(*byteArray).use {
                val grayScale = Imgcodecs.imdecode(it, Imgcodecs.IMREAD_GRAYSCALE)
                var alphaChannel: Mat? = null

                Imgcodecs.imdecode(it, Imgcodecs.IMREAD_UNCHANGED).use { original ->
                    // RGBA, extract alpha
                    if (original.channels() == 4) {
                        alphaChannel =
                            Mat().apply { Core.extractChannel(original, this, 3) }
                        val minMax = Core.minMaxLoc(alphaChannel)
                        if (minMax.minVal.equals(255.0)) {
                            //every pixel has 0 transparency, alpha is useless
                            alphaChannel?.release()
                            alphaChannel = null
                        }
                    }
                }

                return MatWithAlpha(grayScale, alphaChannel)
            }
        }
    }

    private constructor(matWithAlpha: MatWithAlpha) : this(matWithAlpha.mat, matWithAlpha.alpha)

    constructor(stream: InputStream) : this(makeMat(stream))

    private var tag = ""

    override fun toString() =
        if (tag.isBlank()) super.toString() else tag

    override fun close() {
        if (ownsMat) {
            mat.release()
        }
        alpha?.release()
    }

    private fun resize(source: Mat, target: Mat, size: Size) {
        Imgproc.resize(
            source, target,
            CvSize(size.width.toDouble(), size.height.toDouble()),
            0.0, 0.0, Imgproc.INTER_AREA
        )
    }

    override fun resize(size: Size): IPattern {
        val resizedMat = Mat().apply { resize(mat, this, size) }
        val resizedAlpha = alpha?.let {
            Mat().apply { resize(it, this, size) }
        }

        return DroidCvPattern(resizedMat, resizedAlpha).tag(tag)
    }

    override fun resize(target: IPattern, size: Size) {
        if (target is DroidCvPattern) {
            resize(mat, target.mat, size)
            alpha?.let {
                if (target.alpha == null) {
                    target.alpha = Mat()
                }
                resize(it, target.alpha!!, size)
            }
        }

        target.tag(tag)
    }

    private fun match(template: IPattern): Mat {
        val result = Mat()
        if (template is DroidCvPattern) {
            if (template.width <= width && template.height <= height) {
                if (template.alpha != null) {
                    Imgproc.matchTemplate(
                        mat,
                        template.mat,
                        result,
                        Imgproc.TM_CCOEFF_NORMED,
                        template.alpha
                    )
                } else {
                    Imgproc.matchTemplate(
                        mat,
                        template.mat,
                        result,
                        Imgproc.TM_CCOEFF_NORMED
                    )
                }
            } else {
                Timber.verbose { "Skipped matching $template: Region out of bounds" }
            }
        }

        return result
    }

    override fun findMatches(template: IPattern, similarity: Double) = sequence {
        val result = match(template)

        result.use {
            while (true) {
                val minMaxLocResult = Core.minMaxLoc(it)
                val score = minMaxLocResult.maxVal

                if (score >= similarity) {
                    val loc = minMaxLocResult.maxLoc
                    val region = Region(
                        loc.x.roundToInt(),
                        loc.y.roundToInt(),
                        template.width,
                        template.height
                    )

                    val match = Match(region, score)

                    Timber.debug { "Matched $template with a score of ${match.score}" }
                    yield(match)

                    Mat().use { mask ->
                        // Flood fill eliminates the problem of nearby points to a high similarity point also having high similarity
                        val floodFillDiff = 0.3
                        Imgproc.floodFill(
                            result, mask, loc, Scalar(0.0),
                            Rect(),
                            Scalar(floodFillDiff), Scalar(floodFillDiff),
                            Imgproc.FLOODFILL_FIXED_RANGE
                        )
                    }
                } else {
                    Timber.verbose { "Stopped matching $template at score ($score) < similarity ($similarity)" }
                    break
                }
            }
        }
    }

    override val width get() = mat.width()
    override val height get() = mat.height()

    override fun crop(region: Region): IPattern {
        val clippedRegion = Region(0, 0, width, height)
            .clip(region)

        val rect = Rect(clippedRegion.x, clippedRegion.y, clippedRegion.width, clippedRegion.height)

        return DroidCvPattern(Mat(mat, rect), alpha?.let { Mat(alpha, rect) }).tag(tag)
    }

    fun asBitmap(): Bitmap {
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        Mat().use {
            val conversion = if (mat.type() == CvType.CV_8UC1)
                Imgproc.COLOR_GRAY2RGB
            else Imgproc.COLOR_BGR2RGBA

            Imgproc.cvtColor(mat, it, conversion)
            org.opencv.android.Utils.matToBitmap(it, bmp)
        }

        return bmp
    }

    override fun save(stream: OutputStream) {
        asBitmap().use { bmp ->
            bmp.compress(Bitmap.CompressFormat.PNG, 90, stream)
        }
    }

    override fun copy() = DroidCvPattern(mat.clone(), alpha?.clone()).tag(tag)

    override fun tag(tag: String) = apply { this.tag = tag }

    override fun threshold(value: Double): IPattern {
        val result = Mat()

        Imgproc.threshold(mat, result, value * 255, 255.0, Imgproc.THRESH_BINARY)

        return DroidCvPattern(result)
            .tag("$tag[threshold=$value]")
    }
}