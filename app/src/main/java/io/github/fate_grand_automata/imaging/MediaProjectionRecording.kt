package io.github.fate_grand_automata.imaging

import android.hardware.display.VirtualDisplay
import android.media.CamcorderProfile
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import io.github.fate_grand_automata.util.StorageProvider
import io.github.lib_automata.Size

/**
 * This class is responsible for creating video recordings of the screen using [MediaProjection].
 */
class MediaProjectionRecording(
    mediaProjection: MediaProjection,
    imageSize: Size,
    screenDensity: Int,
    storageProvider: StorageProvider
) : AutoCloseable {
    private val mediaRecorder = MediaRecorder().apply {
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

    private val virtualDisplay: VirtualDisplay = mediaProjection.createVirtualDisplay(
        "ScreenRecord",
        imageSize.width, imageSize.height, screenDensity,
        0, mediaRecorder.surface, null, null
    )

    init {
        mediaRecorder.start()
    }

    override fun close() {
        mediaRecorder.stop()
        virtualDisplay.release()
    }
}