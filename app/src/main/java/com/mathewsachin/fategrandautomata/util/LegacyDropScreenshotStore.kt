package com.mathewsachin.fategrandautomata.util

import com.mathewsachin.fategrandautomata.StorageDirs
import com.mathewsachin.fategrandautomata.scripts.IDropScreenshotStore
import com.mathewsachin.libautomata.IPattern
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class LegacyDropScreenshotStore @Inject constructor(val storageDirs: StorageDirs) : IDropScreenshotStore {
    private val dropsFolder by lazy {
        val folder = File(
            storageDirs.storageRoot,
            "drops"
        )

        if (!folder.exists()) {
            folder.mkdirs()
        }

        folder
    }

    override fun insert(images: List<IPattern>) {
        val sdf = SimpleDateFormat("dd-M-yyyy-hh-mm-ss", Locale.US)
        val timeString = sdf.format(Date())

        for ((i, image) in images.withIndex()) {
            val dropFileName = "${timeString}.${i}.png"

            image.use {
                it.save(
                    File(dropsFolder, dropFileName).absolutePath
                )
            }
        }
    }
}