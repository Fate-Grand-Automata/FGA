package com.mathewsachin.fategrandautomata

import java.io.File

class StorageDirs(storageRoot: File) {
    val storageRoot by lazy {
        if (!storageRoot.exists()) {
            storageRoot.mkdirs()
        }

        storageRoot
    }
}