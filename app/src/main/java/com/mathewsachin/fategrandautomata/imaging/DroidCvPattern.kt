package com.mathewsachin.fategrandautomata.imaging

import android.graphics.Bitmap
import com.mathewsachin.libautomata.Match
import com.mathewsachin.libautomata.Pattern
import com.mathewsachin.libautomata.Region
import com.mathewsachin.libautomata.Size
import org.opencv.core.*
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import timber.log.Timber
import java.io.InputStream
import java.io.OutputStream
import kotlin.math.roundToInt
import org.opencv.core.Size as CvSize

class DroidCvPattern(
    private var mat: Mat = Mat(),
    private val ownsMat: Boolean = true
) : Pattern {
    private companion object {
        fun makeMat(
            stream: InputStream,
            isColor: Boolean
        ): Mat {
            val byteArray = stream.readBytes()

            return MatOfByte(*byteArray).use {
                Imgcodecs.imdecode(
                    it,
                    if (isColor) Imgcodecs.IMREAD_COLOR else Imgcodecs.IMREAD_GRAYSCALE
                )
            }
        }
    }

    constructor(stream: InputStream, isColor: Boolean) : this(makeMat(stream, isColor))

    private var tag = ""

    override fun toString() =
        tag.ifBlank { super.toString() }

    override fun close() {
        if (ownsMat) {
            mat.release()
        }
    }

    private fun resize(source: Mat, target: Mat, size: Size) {
        Imgproc.resize(
            source, target,
            CvSize(size.width.toDouble(), size.height.toDouble()),
            0.0, 0.0, Imgproc.INTER_AREA
        )
    }

    override fun resize(size: Size): Pattern {
        val resizedMat = Mat().apply { resize(mat, this, size) }

        return DroidCvPattern(resizedMat).tag(tag)
    }

    override fun resize(target: Pattern, size: Size) {
        if (target is DroidCvPattern) {
            resize(mat, target.mat, size)
        }

        target.tag(tag)
    }

    private fun match(template: Pattern): Mat {
        val result = Mat()
        if (template is DroidCvPattern) {
            if (template.width <= width && template.height <= height) {
                Imgproc.matchTemplate(
                    mat,
                    template.mat,
                    result,
                    Imgproc.TM_CCOEFF_NORMED
                )
            } else {
                Timber.v("Skipped matching $template: Region out of bounds")
            }
        }

        return result
    }

    override fun findMatches(template: Pattern, similarity: Double) = sequence {
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

                    Timber.d("Matched $template with a score of ${match.score}")
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
                    Timber.v("Stopped matching $template at score ($score) < similarity ($similarity)")
                    break
                }
            }
        }
    }

    override val width get() = mat.width()
    override val height get() = mat.height()

    override fun crop(region: Region): Pattern {
        val clippedRegion = Region(0, 0, width, height)
            .clip(region)

        val rect = Rect(clippedRegion.x, clippedRegion.y, clippedRegion.width, clippedRegion.height)

        return DroidCvPattern(Mat(mat, rect)).tag(tag)
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

    override fun copy() = DroidCvPattern(mat.clone()).tag(tag)

    override fun tag(tag: String) = apply { this.tag = tag }

    override fun threshold(value: Double): Pattern {
        val result = Mat()

        Imgproc.threshold(mat, result, value * 255, 255.0, Imgproc.THRESH_BINARY)

        return DroidCvPattern(result)
            .tag("$tag[threshold=$value]")
    }

    override fun isWhite(): Boolean {
        val minMaxLocResult = Core.minMaxLoc(mat)
        // 0 = black, 255 = white
        return minMaxLocResult.minVal >= 200
    }
}