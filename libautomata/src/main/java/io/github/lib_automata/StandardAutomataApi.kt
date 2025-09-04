package io.github.lib_automata

import javax.inject.Inject
import kotlin.time.Duration

class StandardAutomataApi @Inject constructor(
    private val screenshotManager: ScreenshotManager,
    private val highlight: Highlighter,
    private val click: Clicker,
    private val imageMatcher: ImageMatcher,
    private val transform: Transformer,
    private val colorManager: ColorManager,
    private val wait: Waiter,
    private val ocrService: OcrService
) : AutomataApi {

    override fun Region.getPattern(tag: String): Pattern =
        screenshotManager.getScreenshot()
            .crop(transform.toImage(this))
            .also { highlight(this, HighlightColor.Info) }
            .copy() // It is important that the image gets cloned here.
            .apply {
                this.tag = tag
            }

    override fun <T> useSameSnapIn(block: () -> T) =
        screenshotManager.useSameSnapIn(block)

    override fun <T> useColor(block: () -> T): T =
        colorManager.useColor(block)

    override fun Duration.wait(applyMultiplier: Boolean) = wait(this, applyMultiplier)

    override fun Location.click(times: Int) = click(this, times)

    override fun Region.exists(
        image: Pattern,
        timeout: Duration,
        similarity: Double?
    ) = imageMatcher.exists(this, image, timeout, similarity)

    override fun Region.waitVanish(
        image: Pattern,
        timeout: Duration,
        similarity: Double?
    ) = imageMatcher.waitVanish(this, image, timeout, similarity)

    override fun Region.findAll(
        pattern: Pattern,
        similarity: Double?
    ) = imageMatcher.findAll(this, pattern, similarity)

    override fun Region.isWhite() = imageMatcher.isWhite(this)

    override fun Region.isBlack() = imageMatcher.isBlack(this)

    override fun Region.detectText(outlinedText: Boolean): String {
        screenshotManager.getScreenshot()
            .crop(transform.toImage(this))
            .threshold(0.5)
            .let {
                if (outlinedText) {
                    it.use {
                        it.fillText()
                    }
                } else it
            }
            .also { highlight(this, HighlightColor.Info) }
            .use {
                return ocrService.detectText(it)
            }
    }

    override fun Region.detectNumberInBrackets(lower: Hsv, upper: Hsv, invert: Boolean
    ): String = useColor {
        screenshotManager.getScreenshot()
            .crop(transform.toImage(this))
            .normalizeByHsv(lower, upper, invert)
            .cropWhiteRegion(2)
            .let { normalized ->
                ocrService.detectNumberInBrackets(normalized).also { result ->
                    highlight(this, HighlightColor.Info, text = result.takeIf { it.isNotBlank() })
                }
            }
    }

    override fun Map<Pattern, Region>.exists(
        timeout: Duration, similarity: Double?, requireAll: Boolean,
    ) = imageMatcher.exists(
        items = this,
        timeout = timeout,
        similarity = similarity,
        requireAll = requireAll
    )

    override fun Region.isBrightnessAbove(threshold: Double): Boolean =
        screenshotManager
            .getScreenshot()
            .crop(transform.toImage(this))
            .getAverageBrightness()
            .let { avg ->
                (avg >= threshold).also { result ->
                    highlight(
                        this.let {
                            Region(it.x - 6, it.y - 6, it.width + 12, it.height + 12)
                        },
                        if (result) HighlightColor.Success else HighlightColor.Error
                    )
                }
            }

    override fun Region.isSaturationAndValueOver(sThresh: Double, vThresh: Double
    ): Boolean = useColor {
        screenshotManager
            .getScreenshot()
            .crop(transform.toImage(this))
            .getHsvAverage()
            .let { hsv ->
                hsv.s >= sThresh && hsv.v >= vThresh
            }
            .also { result ->
                highlight(
                    this.let {
                        Region(it.x - 6, it.y - 6, it.width + 12, it.height + 12)
                    },
                    color = if (result) HighlightColor.Success else HighlightColor.Error
                )
            }
    }

    override fun Region.detectVisualBarLength(
        lower: Hsv,
        upper: Hsv
    ): Int = useColor {
        screenshotManager
            .getScreenshot()
            .crop(transform.toImage(this))
            .countPixelsInHsvRange(lower, upper)
            .let { count ->
                if (0 < count) transform.fromImage(Region(0, 0, count, 1)).width else 0
            }
            .also { count ->
                val np = (count * 100.0 / this.width).toInt()
                highlight(
                    this.let {
                        Region(it.x - 6, it.y - 6, it.width + 12, it.height + 12)
                    },
                    color = HighlightColor.Info,
                    text = np.toString()
                )
            }
    }

    override fun Region.isBelowBrightness(
        threshold: Double
    ): Boolean = screenshotManager
        .getScreenshot()
        .crop(transform.toImage(this))
        .getAverageBrightness()
        .let { brightness -> brightness < threshold }
        .also { result ->
            highlight(
                this,
                color = if (result) HighlightColor.Success else HighlightColor.Error
            )
        }
}

