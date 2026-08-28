package io.github.fate_grand_automata.util

import io.github.fate_grand_automata.SupportImageKind

class KnownException(val reason: Reason) : Exception(reason.msg) {
    sealed class Reason(val msg: String) {
        class CouldNotCreateDirectory(name: String) : Reason("Couldn't create directory: '$name'")
        class CouldNotCreateFile(name: String) : Reason("Couldn't create file: '$name'")
        object CouldNotOpenFileForRecording : Reason("Couldn't open file for recording")
        class CouldNotOpenSupportFileForReading(
            kind: SupportImageKind,
            name: String
        ) : Reason("Couldn't open file for reading: [$kind] '$name'")

        class CouldNotOpenSupportFileForWriting(
            kind: SupportImageKind,
            name: String
        ) : Reason("Couldn't open file for writing: [$kind] '$name'")

        object CouldNotCreateDropScreenshotFile : Reason("Failed to create drop screenshot file")
        class SupportFolderIsEmpty(
            kind: SupportImageKind,
            name: String
        ) : Reason("[$kind] folder: '$name' is empty!")

        class FailedRootPermission(e: Exception) : Reason("Failed to get Root permission: ${e.message}")

        object NoVirtualDisplay : Reason("Screen capture couldn't be set up. Restart FGA and grant the screen capture permission again.")
        object NoScreenshotReceived : Reason("Screen capture didn't deliver any image. Restart FGA and grant the screen capture permission again.")
        object ScreenCaptureStopped : Reason("Screen capture was stopped. This happens when the system or another app takes it over, or when the display changes. Restart FGA and grant the screen capture permission again.")
    }
}