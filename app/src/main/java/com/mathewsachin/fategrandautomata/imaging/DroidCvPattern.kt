package com.mathewsachin.fategrandautomata.imaging

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
import kotlin.math.roundToInt
import org.opencv.core.Size as CvSize

class DroidCvPattern(
    private var Mat: Mat? = Mat(),
    private val OwnsMat: Boolean = true
) : IPattern {
    private data class MatWithAlpha(val mat: Mat, val alpha: Mat?)

    var alpha: Mat? = null

    private companion object {
        fun makeMat(Stream: InputStream): MatWithAlpha {
            val byteArray = Stream.readBytes()
            DisposableMat(MatOfByte(*byteArray)).use {
                val decoded = Imgcodecs.imdecode(it.Mat, Imgcodecs.IMREAD_UNCHANGED)
                var alphaChannel: Mat? = null

                // Change color images to grayscale and extract alpha if present
                when (decoded.channels()) {
                    4 -> {
                        // RGBA
                        alphaChannel =
                            Mat().apply { Core.extractChannel(decoded, this, 3) }
                        Imgproc.cvtColor(decoded, decoded, Imgproc.COLOR_RGBA2GRAY)
                    }
                    3 -> Imgproc.cvtColor(decoded, decoded, Imgproc.COLOR_RGB2GRAY)
                }

                return MatWithAlpha(decoded, alphaChannel)
            }
        }
    }

    private constructor(matWithAlpha: MatWithAlpha) : this(matWithAlpha.mat) {
        alpha = matWithAlpha.alpha
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

    private fun match(Template: IPattern): DisposableMat {
        if (Template is DroidCvPattern) {
            val result = DisposableMat()

            if (Template.width <= width && Template.height <= height) {
                if (alpha != null) {
                    Imgproc.matchTemplate(
                        Mat,
                        Template.Mat,
                        result.Mat,
                        Imgproc.TM_CCOEFF_NORMED,
                        alpha
                    )
                } else {
                    Imgproc.matchTemplate(
                        Mat,
                        Template.Mat,
                        result.Mat,
                        Imgproc.TM_CCOEFF_NORMED
                    )
                }
            } else {
                Timber.verbose { "Skipped matching $Template: Region out of bounds" }
            }

            return result
        }

        return DisposableMat()
    }

    override fun findMatches(Template: IPattern, Similarity: Double) = sequence {
        val result = match(Template)

        result.use {
            while (true) {
                val minMaxLocResult = Core.minMaxLoc(it.Mat)
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

                    val mask = DisposableMat()
                    mask.use {
                        // Flood fill eliminates the problem of nearby points to a high similarity point also having high similarity
                        val floodFillDiff = 0.3
                        Imgproc.floodFill(
                            result.Mat, mask.Mat, loc, Scalar(0.0),
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

    override fun save(FileName: String) {
        Imgcodecs.imwrite(FileName, Mat)
    }

    override fun copy() = DroidCvPattern(Mat?.clone()).tag(tag)

    override fun tag(tag: String) = apply { this.tag = tag }
}