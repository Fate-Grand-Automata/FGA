package io.github.fate_grand_automata.imaging

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.hardware.display.VirtualDisplay
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import io.github.fate_grand_automata.util.KnownException
import io.github.fate_grand_automata.util.StorageProvider
import io.github.lib_automata.ColorManager
import io.github.lib_automata.Pattern
import io.github.lib_automata.ScreenshotService
import io.github.lib_automata.Size
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/**
 * This class is responsible for creating screenshots using [mediaProjection].
 */
class MediaProjectionScreenshotService(
    private val context: Context,
    private val mediaProjection: MediaProjection,
    private val imageSize: Size,
    private val screenDensity: Int,
    private val storageProvider: StorageProvider,
    private val colorManager: ColorManager
) : ScreenshotService {
    private val bufferMat = Mat()
    private val grayscaleMat = Mat()
    private val grayscalePattern = DroidCvPattern(grayscaleMat, ownsMat = false)
    private val colorMat = Mat()
    private val colorPattern = DroidCvPattern(colorMat, ownsMat = false)

    @SuppressLint("WrongConstant")
    private val imageReader = ImageReader.newInstance(imageSize.width, imageSize.height, PixelFormat.RGBA_8888, 2)
    private val closed = AtomicBoolean(false)

    /**
     * Guards the [Mat]s and the [imageReader] against [close] running while a screenshot is
     * being taken on the script thread. Note that the returned [Pattern]s are read by the caller
     * after the lock is released, so this covers the capture and the colour conversion only.
     */
    private val lock = Any()

    /**
     * Counted down as soon as the [VirtualDisplay] hands out its first frame. Registered before
     * the display is created so no frame can slip past it.
     */
    private val firstFrame = CountDownLatch(1)

    /**
     * Whether a frame ever made it into [bufferMat]. Deliberately not derived from [firstFrame]:
     * the listener is dispatched on the main looper, so the latch can still be closed while a
     * frame has already been captured straight off the reader.
     */
    private var hasFrame = false

    init {
        imageReader.setOnImageAvailableListener(
            { reader ->
                firstFrame.countDown()

                // Only the first frame is waited on, so stop posting to the looper after it.
                reader.setOnImageAvailableListener(null, null)
            },
            null
        )
    }

    private val mediaProjectionCallback = object : MediaProjection.Callback() {
        override fun onStop() {
            close()
        }
    }

    private val virtualDisplay: VirtualDisplay? = mediaProjection.apply {
        this.registerCallback(mediaProjectionCallback, null)
    }.createVirtualDisplay(
        "ScreenCapture",
        imageSize.width, imageSize.height, screenDensity,
        0, imageReader.surface, null, null
    )

    override fun takeScreenshot(): Pattern = synchronized(lock) {
        /*
         * The projection can stop at any moment: the system revokes it, another app takes it
         * over, or the display changes underneath us. [close] releases the Mats when that
         * happens, and a released Mat is empty, which is what blows up in cvtColor below.
         * A closed ImageReader hands out null images instead of throwing, so this is the only
         * place the script gets to hear about it.
         */
        if (closed.get()) {
            throw KnownException(KnownException.Reason.ScreenCaptureStopped)
        }

        screenshotIntoBuffer()

        if (colorManager.isColor) {
            Imgproc.cvtColor(bufferMat, colorMat, Imgproc.COLOR_RGBA2BGR)

            colorPattern
        } else {
            Imgproc.cvtColor(bufferMat, grayscaleMat, Imgproc.COLOR_RGBA2GRAY)

            grayscalePattern
        }
    }

    private fun screenshotIntoBuffer() {
        val image = imageReader.acquireLatestImage()
            ?: awaitFirstImage()
            /*
             * Nothing new since the last call, which just means the screen didn't change.
             * [bufferMat] still holds the previous frame.
             */
            ?: return

        image.use {
            val plane = it.planes[0]
            val buffer = plane.buffer

            val rowStride = plane.rowStride.toLong()

            // Buffer memory isn't copied by OpenCV
            Mat(it.height, it.width, CvType.CV_8UC4, buffer, rowStride)
                .use { tempMat ->
                    tempMat.copyTo(bufferMat)
                }
        }

        hasFrame = true
    }

    /**
     * The [VirtualDisplay] needs a moment after being created before the system composes the
     * first frame into it. Until that happens, [ImageReader.acquireLatestImage] returns `null`
     * and [bufferMat] stays empty, which used to blow up in `cvtColor` further down.
     *
     * Only the very first frame is worth waiting for; afterwards a `null` image simply means
     * the screen contents didn't change.
     */
    private fun awaitFirstImage(): Image? {
        if (hasFrame) {
            return null
        }

        if (virtualDisplay == null) {
            throw KnownException(KnownException.Reason.NoVirtualDisplay)
        }

        if (!firstFrame.await(FIRST_FRAME_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
            throw KnownException(KnownException.Reason.NoScreenshotReceived)
        }

        /*
         * close() counts the latch down too, so revoking the projection mid-wait doesn't
         * leave the script sitting here for the full timeout.
         */
        if (closed.get()) {
            throw KnownException(KnownException.Reason.ScreenCaptureStopped)
        }

        return imageReader.acquireLatestImage()
            ?: throw KnownException(KnownException.Reason.NoScreenshotReceived)
    }

    override fun close() {
        /*
         * stop() below makes the projection call onStop(), which lands right back here. Unregistering
         * first isn't enough on its own, because close() is also reached *from* onStop() when the
         * user revokes the projection.
         */
        if (!closed.compareAndSet(false, true)) {
            return
        }

        /*
         * Happens before taking the lock: a script thread waiting for the first frame has to be
         * let go before it can hand the lock over.
         */
        firstFrame.countDown()

        synchronized(lock) {
            bufferMat.release()
            grayscaleMat.release()
            grayscalePattern.close()
            colorMat.release()
            colorPattern.close()

            virtualDisplay?.release()

            imageReader.close()

            mediaProjection.unregisterCallback(mediaProjectionCallback)
            mediaProjection.stop()
        }
    }

    override fun startRecording() =
        MediaProjectionRecording(context, mediaProjection, imageSize, screenDensity, storageProvider)

    companion object {
        private const val FIRST_FRAME_TIMEOUT_SECONDS = 3L
    }
}
