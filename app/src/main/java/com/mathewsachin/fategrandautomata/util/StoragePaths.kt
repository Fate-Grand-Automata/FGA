package com.mathewsachin.fategrandautomata.util

import android.os.Environment
import java.io.File

object StoragePaths {
    val pictures: File by lazy {
        File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            "FGA"
        ).apply { mkdirs() }
    }
}