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
    private var Mat: Mat? = Mat(),
    private val OwnsMat: Boolean = true
) : IPattern {
    private companion object {
        fun makeMat(Stream: InputStream): Mat {
            val byteArray = Stream.readBytes()

            return MatOfByte(*byteArray).use {
                Imgcodecs.imdecode(it, Imgcodecs.IMREAD_GRAYSCALE)
            }
        }
    }

    constructor(Stream: InputStream) : this(makeMat(Stream))

    init {
        require(Mat != null) { "Mat should not be null" }
    }

    private var tag = ""

    override fun toString() =
        if (tag.isBlank()) super.toString() else tag

    override fun close() {
        if (OwnsMat) {
            Mat?.release()
        }

        Mat = null
    }

    private fun resize(Target: Mat, Size: Size) {
        Imgproc.resize(
            Mat, Target,
            CvSize(Size.Width.toDouble(), Size.Height.toDouble()),
            0.0, 0.0, Imgproc.INTER_AREA
        )
    }

    override fun resize(Size: Size): IPattern {
        val result = Mat()
        resize(result, Size)
        return DroidCvPattern(result).tag(tag)
    }

    override fun resize(Target: IPattern, Size: Size) {
        if (Target is DroidCvPattern) {
            resize(Target.Mat!!, Size)
        }

        Target.tag(tag)
    }

    private fun match(Template: IPattern): Mat {
        val result = Mat()

        if (Template is DroidCvPattern) {
            if (Template.width <= width && Template.height <= height) {
                Imgproc.matchTemplate(
                    Mat,
                    Template.Mat,
                    result,
                    Imgproc.TM_CCOEFF_NORMED
                )
            } else {
                Timber.verbose { "Skipped matching $Template: Region out of bounds" }
            }
        }

        return result
    }

    override fun findMatches(Template: IPattern, Similarity: Double) = sequence {
        val result = match(Template)

        result.use {
            while (true) {
                val minMaxLocResult = Core.minMaxLoc(it)
                val score = minMaxLocResult.maxVal

                if (score >= Similarity) {
                    val loc = minMaxLocResult.maxLoc
                    val region = Region(
                        loc.x.roundToInt(),
                        loc.y.roundToInt(),
                        Template.width,
                        Template.height
                    )

                    val match = Match(region, score)

                    Timber.debug { "Matched $Template with a score of ${match.score}" }
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
                    Timber.verbose { "Stopped matching $Template at score ($score) < similarity ($Similarity)" }
                    break
                }
            }
        }
    }

    override val width get() = Mat?.width() ?: 0
    override val height get() = Mat?.height() ?: 0

    override fun crop(Region: Region): IPattern {
        val region = Region(0, 0, width, height)
            .clip(Region)

        val rect = Rect(region.X, region.Y, region.Width, region.Height)

        val result = Mat(Mat, rect)

        return DroidCvPattern(result).tag(tag)
    }

    fun asBitmap(): Bitmap {
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        Mat().use {
            val conversion = if (Mat?.type() == CvType.CV_8UC1)
                Imgproc.COLOR_GRAY2BGRA
            else Imgproc.COLOR_BGR2RGBA

            Imgproc.cvtColor(Mat, it, conversion)
            org.opencv.android.Utils.matToBitmap(it, bmp)
        }

        return bmp
    }

    override fun save(stream: OutputStream) {
        asBitmap().use { bmp ->
            bmp.compress(Bitmap.CompressFormat.PNG, 90, stream)
        }
    }

    override fun copy() = DroidCvPattern(Mat?.clone()).tag(tag)

    override fun tag(tag: String) = apply { this.tag = tag }
}