package com.mathewsachin.fategrandautomata.imaging

import android.hardware.display.VirtualDisplay
import android.media.CamcorderProfile
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.util.DisplayMetrics
import com.mathewsachin.fategrandautomata.util.StorageProvider

/**
 * This class is responsible for creating video recordings of the screen using [MediaProjection].
 */
class MediaProjectionRecording(
    MediaProjection: MediaProjection,
    DisplayMetrics: DisplayMetrics,
    storageProvider: StorageProvider
) : AutoCloseable {

    private val virtualDisplay: VirtualDisplay
    private val mediaRecorder: MediaRecorder

    init {
        val screenDensity = DisplayMetrics.densityDpi
        var screenWidth = DisplayMetrics.widthPixels
        var screenHeight = DisplayMetrics.heightPixels

        // we only want landscape images, since the frame size can't be changed during a projection
        if (screenHeight > screenWidth) {
            val temp = screenHeight
            screenHeight = screenWidth
            screenWidth = temp
        }

        mediaRecorder = MediaRecorder()
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE)

        val profile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH)

        // Copy properties not related to audio
        mediaRecorder.setOutputFormat(profile.fileFormat)
        mediaRecorder.setVideoEncoder(profile.videoCodec)
        mediaRecorder.setVideoEncodingBitRate(profile.videoBitRate)
        mediaRecorder.setVideoFrameRate(profile.videoFrameRate)
        mediaRecorder.setVideoSize(screenWidth, screenHeight)

        mediaRecorder.setOutputFile(storageProvider.recordingFileDescriptor.fileDescriptor)

        mediaRecorder.prepare()

        virtualDisplay = MediaProjection.createVirtualDisplay(
            "ScreenRecord",
            screenWidth, screenHeight, screenDensity,
            0, mediaRecorder.surface, null, null
        )

        mediaRecorder.start()
    }

    override fun close() {
        mediaRecorder.stop()
        virtualDisplay.release()
    }
}