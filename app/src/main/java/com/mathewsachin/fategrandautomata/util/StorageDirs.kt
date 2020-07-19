package com.mathewsachin.fategrandautomata.util

import java.io.File

class StorageDirs(val storageRoot: File) {
    init {
        storageRoot.mkdirs()
    }

    private val supportImgFolderPath: File by lazy {
        File(
            storageRoot,
            "support"
        )
    }

    val shouldExtractSupportImages get() = !supportImgFolderPath.exists()

    val supportImgFolder: File by lazy {
        val dir = supportImgFolderPath

        if (!dir.exists()) {
            dir.mkdirs()

            // Create a .nomedia file so the images won't be added to gallery apps
            val noMediaFile = File(dir, ".nomedia")
            noMediaFile.createNewFile()
        }

        dir
    }

    val supportServantImgFolder: File by lazy {
        val dir = File(supportImgFolder, "servant")

        if (!dir.exists()) {
            dir.mkdirs()
        }

        dir
    }

    val supportCeFolder: File by lazy {
        val dir = File(supportImgFolder, "ce")

        if (!dir.exists()) {
            dir.mkdirs()
        }

        dir
    }

    val supportFriendFolder: File by lazy {
        val dir = File(supportImgFolder, "friend")

        if (!dir.exists()) {
            dir.mkdirs()
        }

        dir
    }
}