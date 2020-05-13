package com.mathewsachin.fategrandautomata.imaging

import com.mathewsachin.fategrandautomata.core.IPattern
import com.mathewsachin.fategrandautomata.core.Match
import com.mathewsachin.fategrandautomata.core.Region
import com.mathewsachin.fategrandautomata.core.Size
import org.opencv.core.*
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import java.io.InputStream
import kotlin.math.roundToInt
import org.opencv.core.Size as CvSize

class DroidCvPattern(private var Mat: Mat?, private val OwnsMat: Boolean = true) : IPattern {
    constructor(): this(Mat())

    private companion object {
        fun makeMat(Stream: InputStream): Mat {
            val byteArray = Stream.readBytes()
            DisposableMat(MatOfByte(*byteArray)).use {
                return Imgcodecs.imdecode(it.Mat, Imgcodecs.IMREAD_GRAYSCALE)
            }
        }
    }

    constructor(Stream: InputStream): this(makeMat(Stream))

    init {
        require(Mat != null){ "Mat should not be null" }
    }

    override fun close() {
        if (OwnsMat) {
            Mat?.release()
        }

        Mat = null
    }

    private fun resize(Target: Mat, Size: Size) {
        Imgproc.resize(Mat, Target,
            CvSize(Size.Width.toDouble(), Size.Height.toDouble()),
            0.0, 0.0, Imgproc.INTER_AREA)
    }

    override fun resize(Size: Size): IPattern {
        val result = Mat()
        resize(result, Size)
        return DroidCvPattern(result)
    }

    override fun resize(Target: IPattern, Size: Size) {
        if (Target is DroidCvPattern) {
            resize(Target.Mat!!, Size)
        }
    }

    private fun match(Template: IPattern): DisposableMat {
        if (Template is DroidCvPattern) {
            val result = DisposableMat()

            if (Template.width <= width && Template.height <= height) {
                Imgproc.matchTemplate(Mat, Template.Mat, result.Mat, Imgproc.TM_CCOEFF_NORMED)
            }

            return result
        }

        return DisposableMat()
    }

    override fun isMatch(Template: IPattern, Similarity: Double): Boolean {
        match(Template).use {
            val minMaxLocResult = Core.minMaxLoc(it.Mat)

            return minMaxLocResult.maxVal >= Similarity
        }
    }

    override fun findMatches(Template: IPattern, Similarity: Double) = sequence {
        val result = match(Template)

        result.use {
            while (true) {
                val minMaxLocResult = Core.minMaxLoc(it.Mat)
                val score = minMaxLocResult.maxVal

                if (score >= Similarity) {
                    val loc = minMaxLocResult.maxLoc
                    val region = Region(loc.x.roundToInt(),
                        loc.y.roundToInt(),
                        Template.width,
                        Template.height)

                    yield(Match(region, score))

                    val mask = DisposableMat()
                    mask.use {
                        // Flood fill eliminates the problem of nearby points to a high similarity point also having high similarity
                        val floodFillDiff = 0.05
                        Imgproc.floodFill(result.Mat, mask.Mat, loc, Scalar(0.0),
                            Rect(),
                            Scalar(floodFillDiff), Scalar(floodFillDiff),
                            0
                        )
                    }
                }
                else break
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

        return DroidCvPattern(result)
    }

    override fun save(FileName: String) {
        Imgcodecs.imwrite(FileName, Mat)
    }

    override fun copy(): IPattern {
        return DroidCvPattern(Mat?.clone())
    }
}