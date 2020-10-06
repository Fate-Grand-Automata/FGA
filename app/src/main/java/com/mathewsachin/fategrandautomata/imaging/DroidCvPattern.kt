package com.mathewsachin.fategrandautomata.imaging

import com.mathewsachin.fategrandautomata.scripts.WhitePixelsProvider
import com.mathewsachin.libautomata.IPattern
import com.mathewsachin.libautomata.Match
import com.mathewsachin.libautomata.Region
import com.mathewsachin.libautomata.Size
import mu.KotlinLogging
import org.opencv.core.*
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import java.io.InputStream
import kotlin.math.roundToInt
import org.opencv.core.Size as CvSize

private val logger = KotlinLogging.logger {}

class DroidCvPattern(
    private var Mat: Mat? = Mat(),
    private val OwnsMat: Boolean = true
) : IPattern, WhitePixelsProvider {
    private companion object {
        fun makeMat(Stream: InputStream): Mat {
            val byteArray = Stream.readBytes()
            return DisposableMat(MatOfByte(*byteArray)).use {
                Imgcodecs.imdecode(it.Mat, Imgcodecs.IMREAD_GRAYSCALE)
            }
        }
    }

    constructor(Stream: InputStream, tag: String) : this(makeMat(Stream)) {
        this.tag = tag
    }

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
        return DroidCvPattern(result)
    }

    override fun resize(Target: IPattern, Size: Size) {
        if (Target is DroidCvPattern) {
            resize(Target.Mat!!, Size)
            Target.tag = tag
        }
    }

    private fun match(Template: IPattern): DisposableMat {
        if (Template is DroidCvPattern) {
            val result = DisposableMat()

            if (Template.width <= width && Template.height <= height) {
                Imgproc.matchTemplate(
                    Mat,
                    Template.Mat,
                    result.Mat,
                    Imgproc.TM_CCOEFF_NORMED
                )
            } else {
                logger.debug { "Skipped matching $Template: Region out of bounds" }
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

                    logger.debug { "Matched $Template with a score of ${match.score}" }
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
                    logger.debug { "Stopped matching $Template at score ($score) < similarity ($Similarity)" }
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

        return DroidCvPattern(result).also { it.tag = tag }
    }

    override fun save(FileName: String) {
        Imgcodecs.imwrite(FileName, Mat)
    }

    override fun copy() = DroidCvPattern(Mat?.clone()).also {
        it.tag = tag
    }

    override fun getWhitePixelMask(threshold: Int): IPattern {
        val result = Mat()

        Imgproc.threshold(Mat, result, threshold.toDouble(), 255.0, Imgproc.THRESH_BINARY)

        val contours = mutableListOf<MatOfPoint>()
        Imgproc.findContours(result, contours, Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE)

        val rect = Imgproc.boundingRect(contours[0])

        return DroidCvPattern(result)
            .also { it.tag = tag }
            .crop(Region(rect.x, rect.y, rect.width, rect.height))
    }
}