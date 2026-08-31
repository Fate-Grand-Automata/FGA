package io.github.fate_grand_automata.imaging

import android.content.Context
import android.hardware.display.VirtualDisplay
import android.media.CamcorderProfile
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.os.Build
import android.os.ParcelFileDescriptor
import io.github.fate_grand_automata.util.StorageProvider
import io.github.lib_automata.Size
import timber.log.Timber

/**
 * This class is responsible for creating video recordings of the screen using [MediaProjection].
 */
class MediaProjectionRecording(
    context: Context,
    private val mediaProjection: MediaProjection,
    private val imageSize: Size,
    private val screenDensity: Int,
    private val storageProvider: StorageProvider
) : AutoCloseable {
    private val mediaRecorder =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }

    private var outputFile: ParcelFileDescriptor? = null

    private var virtualDisplay: VirtualDisplay? = null

    private var started = false

    private var closed = false

    private val mediaProjectionCallback = object : MediaProjection.Callback() {
        override fun onStop() {
            // The display is gone, so nothing more will be encoded. Wrap the file up now instead of
            // leaving the recorder running until the script happens to end.
            Timber.d("Projection stopped by the user")

            close()
        }
    }

    init {
        // Anything that has already been acquired has to be given back if a later step throws,
        // because the caller only gets an exception and never an object to close.
        try {
            mediaProjection.registerCallback(mediaProjectionCallback, null)

            initializeRecorder()
            mediaRecorder.start()
            started = true
            virtualDisplay = createVirtualDisplay()
        } catch (e: Throwable) {
            close()
            throw e
        }
    }

    private fun initializeRecorder() {
        // Keep the descriptor around: MediaRecorder writes into it until stop() and it's on us to
        // close it afterwards.
        val fileDescriptor = storageProvider.recordingFileDescriptor
        outputFile = fileDescriptor

        with(mediaRecorder) {
            setVideoSource(MediaRecorder.VideoSource.SURFACE)

            // Copy properties not related to audio
            val profile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH)
            setOutputFormat(profile.fileFormat)
            setVideoEncoder(profile.videoCodec)
            setVideoEncodingBitRate(profile.videoBitRate)
            setVideoFrameRate(profile.videoFrameRate)
            setVideoSize(imageSize.width, imageSize.height)

            setOutputFile(fileDescriptor.fileDescriptor)
            prepare()
        }
    }

    private fun createVirtualDisplay(): VirtualDisplay? {
        return mediaProjection.createVirtualDisplay(
            "ScreenRecord",
            imageSize.width, imageSize.height, screenDensity,
            0, mediaRecorder.surface, null, null
        )
    }

    override fun close() {
        if (closed) {
            return
        }
        closed = true

        if (started) {
            // stop() throws when the encoder never received a frame, which happens when a script
            // dies right after starting. The display and the recorder still have to be freed.
            runCatching { mediaRecorder.stop() }
                .onFailure { Timber.w(it, "Failed to stop the recorder") }
        }

        runCatching { mediaProjection.unregisterCallback(mediaProjectionCallback) }
            .onFailure { Timber.w(it, "Failed to unregister the projection callback") }

        runCatching { virtualDisplay?.release() }
            .onFailure { Timber.w(it, "Failed to release the recording display") }
        virtualDisplay = null

        runCatching { mediaRecorder.release() }
            .onFailure { Timber.w(it, "Failed to release the recorder") }

        runCatching { outputFile?.close() }
            .onFailure { Timber.w(it, "Failed to close the recording file") }
        outputFile = null
    }
}
