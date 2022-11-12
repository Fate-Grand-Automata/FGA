package com.mathewsachin.fategrandautomata.util

import com.mathewsachin.fategrandautomata.SupportImageKind

class KnownException(val reason: Reason) : Exception(reason.msg) {
    sealed class Reason(val msg: String) {
        class CouldNotCreateDirectory(name: String) : Reason("Couldn't create directory: '$name'")
        class CouldNotCreateFile(name: String) : Reason("Couldn't create file: '$name'")
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
    }
}