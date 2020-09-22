package com.mathewsachin.fategrandautomata.util

import com.mathewsachin.fategrandautomata.SupportStore
import java.io.File
import javax.inject.Inject

// On Android 11, we can access Media directories using File APIs
class LegacySupportStore @Inject constructor() : SupportStore {
    companion object {
        const val servant = "servant"
        const val ce = "ce"
        const val friend = "friend"
    }

    class LegacyDir(dir: java.io.File, kind: String) : SupportStore.SupportImage.Dir(dir.name, kind) {
        override val list = (dir.listFiles() ?: emptyArray())
            .map { LegacyFile(it, kind) }
    }

    class LegacyFile(val file: java.io.File, kind: String) : SupportStore.SupportImage.File(file.name, kind) {
        override fun read() = file.inputStream()

        override val nameWithoutExtension: String
            get() = file.nameWithoutExtension
    }

    val supportImgFolder: File by lazy {
        File(
            StoragePaths.pictures,
            "support"
        ).apply { mkdirs() }
    }

    val supportServantImgFolder: File by lazy {
        File(supportImgFolder, servant).apply { mkdirs() }
    }

    val supportCeFolder: File by lazy {
        File(supportImgFolder, ce).apply { mkdirs() }
    }

    val supportFriendFolder: File by lazy {
        File(supportImgFolder, friend).apply { mkdirs() }
    }

    override val servants: List<SupportStore.SupportImage>
        get() = (supportServantImgFolder.listFiles() ?: emptyArray())
            .map {
                if (it.isDirectory)
                    LegacyDir(it, servant)
                else LegacyFile(it, servant)
            }

    override val ces: List<SupportStore.SupportImage.File>
        get() = (supportCeFolder.listFiles() ?: emptyArray())
            .filter { it.isFile }
            .map { LegacyFile(it, ce) }

    override val friends: List<SupportStore.SupportImage.File>
        get() = (supportFriendFolder.listFiles() ?: emptyArray())
            .filter { it.isFile }
            .map { LegacyFile(it, friend) }

    override fun addServant(name: String) =
        File(supportServantImgFolder, name)
            .apply { parentFile?.mkdirs() }
            .outputStream()

    override fun addCE(name: String) =
        File(supportCeFolder, name).outputStream()

    override fun addFriend(name: String) =
        File(supportFriendFolder, name).outputStream()

    override fun getServants(name: String) =
        File(supportServantImgFolder, name).let {
            if (it.isFile) {
                listOf(LegacyFile(it, servant))
            } else LegacyDir(it, servant).list
        }

    override fun getCE(name: String) =
        LegacyFile(File(supportCeFolder, name), ce)

    override fun getFriend(name: String) =
        LegacyFile(File(supportFriendFolder, name), friend)
}