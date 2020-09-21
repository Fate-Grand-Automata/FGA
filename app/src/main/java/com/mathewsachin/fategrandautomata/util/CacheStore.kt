package com.mathewsachin.fategrandautomata.util

import android.content.Context
import com.mathewsachin.fategrandautomata.scripts.ITemporaryStore
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject

class CacheStore @Inject constructor(
    @ApplicationContext val context: Context
) : ITemporaryStore {
    fun makePath(key: String) =
        File(context.cacheDir, key).apply {
            parentFile?.mkdirs()
        }

    override fun write(key: String) =
        makePath(key).outputStream()

    override fun read(key: String) =
        makePath(key).inputStream()

    override fun delete(key: String) {
        makePath(key).delete()
    }

    override fun exists(key: String) =
        makePath(key).exists()

    override fun clear() {
        context.cacheDir.listFiles()?.forEach {
            it.deleteRecursively()
        }
    }
}