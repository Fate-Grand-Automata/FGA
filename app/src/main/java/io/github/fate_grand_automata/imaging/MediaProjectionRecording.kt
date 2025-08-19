package io.github.fate_grand_automata.imaging

import android.content.Context
import android.hardware.display.VirtualDisplay
import android.media.CamcorderProfile
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.os.Build
import io.github.fate_grand_automata.util.StorageProvider
import io.github.lib_automata.Size
import timber.log.Timber

/**
 * This class is responsible for creating video recordings of the screen using [MediaProjection].
 */
class MediaProjectionRecording(
    context: Context,
    mediaProjection: MediaProjection,
    imageSize: Size,
    screenDensity: Int,
    storageProvider: StorageProvider
) : AutoCloseable {
    private val mediaRecorder by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }
    }

    private var virtualDisplay: VirtualDisplay? = null

    private val mediaProjectionCallback = object : MediaProjection.Callback() {
        override fun onStop() {
            Timber.d("Projection stopped by the user")
        }
    }

    init {
        mediaProjection.registerCallback(mediaProjectionCallback, null)

        initializeRecorder()
        mediaRecorder.start()
        virtualDisplay = createVirtualDisplay()
    }

    private fun initializeRecorder() {
        with(mediaRecorder) {
            setVideoSource(MediaRecorder.VideoSource.SURFACE)

            // Copy properties not related to audio
            val profile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH)
            setOutputFormat(profile.fileFormat)
            setVideoEncoder(profile.videoCodec)
            setVideoEncodingBitRate(profile.videoBitRate)
            setVideoFrameRate(profile.videoFrameRate)
            setVideoSize(imageSize.width, imageSize.height)

            setOutputFile(storageProvider.recordingFileDescriptor.fileDescriptor)
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
        mediaProjection.unregisterCallback(mediaProjectionCallback)
        mediaRecorder.stop()
        virtualDisplay?.release()
        virtualDisplay = null
    }
}