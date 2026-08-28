package io.github.fate_grand_automata.util

import android.content.Context
import androidx.annotation.StringRes
import io.github.fate_grand_automata.R
import io.github.fate_grand_automata.SupportImageKind

class KnownException(val reason: Reason) : Exception("${reason::class.simpleName}(${reason.args.joinToString()})") {
    sealed class Reason(@StringRes val resId: Int, vararg val args: Any) {
        class CouldNotCreateDirectory(name: String) : Reason(R.string.error_could_not_create_directory, name)
        class CouldNotCreateFile(name: String) : Reason(R.string.error_could_not_create_file, name)
        object CouldNotOpenFileForRecording : Reason(R.string.error_could_not_open_file_for_recording)
        class CouldNotOpenSupportFileForReading(
            kind: SupportImageKind,
            name: String
        ) : Reason(R.string.error_could_not_open_support_file_for_reading, kind, name)

        class CouldNotOpenSupportFileForWriting(
            kind: SupportImageKind,
            name: String
        ) : Reason(R.string.error_could_not_open_support_file_for_writing, kind, name)

        object CouldNotCreateDropScreenshotFile : Reason(R.string.error_could_not_create_drop_screenshot_file)
        class SupportFolderIsEmpty(
            kind: SupportImageKind,
            name: String
        ) : Reason(R.string.error_support_folder_is_empty, kind, name)

        class FailedRootPermission(e: Exception) : Reason(R.string.error_failed_root_permission, e.message.toString())

        object NoVirtualDisplay : Reason(R.string.error_no_virtual_display)
        object NoScreenshotReceived : Reason(R.string.error_no_screenshot_received)
        object ScreenCaptureStopped : Reason(R.string.error_screen_capture_stopped)

        fun msg(context: Context): String = context.getString(resId, *args)
    }
}
