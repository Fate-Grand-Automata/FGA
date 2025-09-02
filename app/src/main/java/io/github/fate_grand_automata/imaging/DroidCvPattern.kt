package io.github.fate_grand_automata.imaging

import android.graphics.Bitmap
import io.github.lib_automata.Hsv
import io.github.lib_automata.Match
import io.github.lib_automata.Pattern
import io.github.lib_automata.Region
import io.github.lib_automata.Size
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.MatOfByte
import org.opencv.core.MatOfPoint
import org.opencv.core.Point
import org.opencv.core.Rect
import org.opencv.core.Scalar
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import timber.log.Timber
import java.io.InputStream
import java.io.OutputStream
import kotlin.math.roundToInt
import org.opencv.core.Size as CvSize

class DroidCvPattern(
    private var mat: Mat = Mat(),
    private val ownsMat: Boolean = true,
    override var tag: String = ""
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

    constructor(stream: InputStream, isColor: Boolean, tag: String = "") : this(makeMat(stream, isColor), tag = tag)

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

        return DroidCvPattern(resizedMat, tag = tag)
    }

    override fun resize(target: Pattern, size: Size) {
        if (target is DroidCvPattern) {
            resize(mat, target.mat, size)
        }

        target.tag = tag
    }

    private fun match(template: Pattern): Mat? {
        val result = Mat()
        if (template is DroidCvPattern) {
            if (template.width <= width && template.height <= height) {
                Imgproc.matchTemplate(
                    mat,
                    template.mat,
                    result,
                    Imgproc.TM_CCOEFF_NORMED
                )
                return result
            } else {
                Timber.v("Skipped matching $template: Region out of bounds")
            }
        }
        return null  
    }

    override fun findMatches(template: Pattern, similarity: Double) = sequence {
        val result = match(template)

        result?.use {
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

                    // Flood fill eliminates the problem of nearby points to a high similarity point also having high similarity
                    result.floodFill(loc, 0.3, 0.0)
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

        return DroidCvPattern(Mat(mat, rect), tag = tag)
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

    override fun copy() = DroidCvPattern(mat.clone(), tag = tag)

    override fun threshold(value: Double): Pattern {
        val result = Mat()

        Imgproc.threshold(mat, result, value * 255, 255.0, Imgproc.THRESH_BINARY)

        return DroidCvPattern(result, tag = "$tag[threshold=$value]")
    }

    override fun isWhite(): Boolean {
        val minMaxLocResult = Core.minMaxLoc(mat)
        // 0 = black, 255 = white
        return minMaxLocResult.minVal >= 200
    }

    override fun isBlack(): Boolean {
        val minMaxLocResult = Core.minMaxLoc(mat)
        // 0 = black, 255 = white
        return minMaxLocResult.maxVal <= 55
    }

    override fun floodFill(x: Double, y: Double, maxDiff: Double, newValue: Double): Pattern {
        val point = Point(x, y)
        this.mat.floodFill(point, maxDiff, newValue)
        return this
    }

    /**
     * Fills in outlined text. Only use this on binary images produced by [threshold].
     */
    override fun fillText(): Pattern {
        val mask = Mat.zeros(org.opencv.core.Size(width + 2.0, height + 2.0), mat.type())

        for (point in getPointInHoles(this.mat)) {
            mat.floodFillMask(point, 255.0, mask)
        }

        Core.bitwise_not(mask, mask)
        return DroidCvPattern(mask)
    }

    override fun getAverageBrightness(): Double {
        return if (mat.channels() == 3) {
            Mat().use { gray ->
                Imgproc.cvtColor(mat, gray, Imgproc.COLOR_BGR2GRAY)
                Core.mean(gray).`val`[0]
            }
        } else {
            Core.mean(mat).`val`[0]
        }
    }

    override fun getMinMaxBrightness(): Pair<Double, Double> {
        return if (mat.channels() == 3) {
            Mat().use { gray ->
                Imgproc.cvtColor(mat, gray, Imgproc.COLOR_BGR2GRAY)
                val minMax = Core.minMaxLoc(gray)
                Pair(minMax.minVal, minMax.maxVal)
            }
        } else {
            val minMax = Core.minMaxLoc(mat)
            Pair(minMax.minVal, minMax.maxVal)
        }
    }


    override fun isSaturationAndValueOver(sThresh: Double, vThresh: Double): Boolean {
        Mat().use { hsv ->
            Imgproc.cvtColor(mat, hsv, Imgproc.COLOR_BGR2HSV)
            val mean = Core.mean(hsv)
            val meanS = mean.`val`[1]
            val meanV = mean.`val`[2]
            return meanS >= sThresh && meanV >= vThresh
        }
    }
    private fun Hsv.scalar() = Scalar(h, s, v)

    override fun getHsvAverage(): Hsv =
        Mat().use { hsv ->
            Imgproc.cvtColor(mat, hsv, Imgproc.COLOR_BGR2HSV)
            Core.mean(hsv).`val`.let { (h, s, v, _) ->
                Hsv(h, s, v)
            }
        }

    override fun normalizeByHsv(lower: Hsv, upper: Hsv, invert: Boolean): Pattern {
        val normalized = Mat().also { resultMat ->
            Mat().use { maskMat ->
                Imgproc.cvtColor(mat, maskMat, Imgproc.COLOR_BGR2HSV)
                Core.inRange(maskMat, lower.scalar(), upper.scalar(), maskMat)
                Core.bitwise_and(mat, mat, resultMat, maskMat)
            }

            Imgproc.cvtColor(resultMat, resultMat, Imgproc.COLOR_BGR2GRAY)
            Imgproc.threshold(
                resultMat,
                resultMat,
                0.0,
                255.0,
                Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU)

            if (invert) {
                Core.bitwise_not(resultMat, resultMat)
            }
        }

        return DroidCvPattern(normalized, tag = tag)
    }

    override fun cropWhiteRegion(
        pad: Int
    ): Pattern = Mat().use { coords ->
        Core.bitwise_not(mat, coords)

        // If no non-zero pixels, return the full binary image
        if (coords.empty()) {
            return this
        }

        val rect = Imgproc.boundingRect(coords)

        val xStart = maxOf(rect.x - pad, 0)
        val yStart = maxOf(rect.y - pad, 0)
        val xEnd = minOf(rect.x + rect.width + pad, mat.cols())
        val yEnd = minOf(rect.y + rect.height + pad, mat.rows())

        // Crop to ROI and return as DroidCvPattern
        DroidCvPattern(
            mat.submat(Rect(xStart, yStart, xEnd - xStart, yEnd - yStart)).clone(),
            tag = tag
        )
    }

    /**
     * Flood fills the mat.
     */
    private fun Mat.floodFill(startingPoint: Point, maxDiff: Double, newValue: Double) {
        Mat().use { mask ->
            Imgproc.floodFill(
                this, mask, startingPoint, Scalar(newValue),
                Rect(),
                Scalar(maxDiff), Scalar(maxDiff),
                Imgproc.FLOODFILL_FIXED_RANGE
            )
        }
    }

    /**
     * Flood fills the mask instead of the mat.
     */
    private fun Mat.floodFillMask(startingPoint: Point, newValue: Double, mask: Mat) {
        Imgproc.floodFill(
            this, mask, startingPoint, Scalar(newValue),
            Rect(), Scalar(0.0), Scalar(0.0),
            4 + (newValue.toInt() shl 8) + Imgproc.FLOODFILL_MASK_ONLY
        )

    }

    /**
     * Identifies the point in the hole with the lowest y value.
     */
    private fun findTopHolePoint(holePoints: MatOfPoint): Point {
        var topPoint = Point(0.0, Double.MAX_VALUE)
        for (point in holePoints.toList()) {
            if (point.y < topPoint.y) {
                topPoint = point
            }
        }
        return Point(topPoint.x, topPoint.y + 1.0)
    }

    /**
     * Finds holes in outlined text and returns a list of [Point]s to be used as starting point for [floodFill].
     */
    private fun getPointInHoles(img: Mat): List<Point> {
        val work = Mat()
        Core.bitwise_not(img, work)
        val contours = mutableListOf<MatOfPoint>()
        val hierarchy = Mat()
        Imgproc.findContours(work, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE)
        val holePoints = mutableListOf<Point>()
        contours.forEachIndexed { index, matOfPoint ->
            val parent = hierarchy[0, index][3].toInt()
            // top level holes have a parent but no grandparent...
            if ((parent >= 0) && (hierarchy[0, parent][3] < 0)) {
                holePoints.add(findTopHolePoint(matOfPoint))
            }

        }
        return holePoints
    }
}