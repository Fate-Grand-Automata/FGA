package io.github.fate_grand_automata.ui.support_img_namer

import io.github.fate_grand_automata.SupportImageKind
import io.github.fate_grand_automata.scripts.entrypoints.SupportImageMaker
import io.github.fate_grand_automata.util.StorageProvider
import java.io.File

fun getSupportEntries(
    storageProvider: StorageProvider
): List<SupportImgEntry> {
    val tempDir = storageProvider.supportImageTempDir

    val servantOptions = getCurrentEntries(storageProvider, SupportImageKind.Servant)

    val servant0 = SupportImgEntry(
        imgPath = SupportImageMaker.getServantImgPath(
            dir = tempDir,
            Index = 0
        ),
        kind = SupportImageKind.Servant,
        index = 0,
        options = servantOptions,
    )
    val servant1 = SupportImgEntry(
        imgPath = SupportImageMaker.getServantImgPath(
            tempDir,
            1
        ),
        kind = SupportImageKind.Servant,
        index = 1,
        options = servantOptions,
    )

    val ceOptions = getCurrentEntries(storageProvider, SupportImageKind.CE)

    val ce0 = SupportImgEntry(
        imgPath = SupportImageMaker.getCeImgPath(
            tempDir,
            0
        ),
        kind = SupportImageKind.CE,
        index = 0,
        options = ceOptions
    )
    val ce1 = SupportImgEntry(
        imgPath = SupportImageMaker.getCeImgPath(
            tempDir,
            1
        ),
        kind = SupportImageKind.CE,
        index = 1,
        options = ceOptions
    )

    val friendOptions = getCurrentEntries(storageProvider, SupportImageKind.Friend)

    val friend0 = SupportImgEntry(
        imgPath = SupportImageMaker.getFriendImgPath(
            tempDir,
            0
        ),
        kind = SupportImageKind.Friend,
        index = 0,
        options = friendOptions
    )
    val friend1 = SupportImgEntry(
        imgPath = SupportImageMaker.getFriendImgPath(
            tempDir,
            1
        ),
        kind = SupportImageKind.Friend,
        index = 1,
        options = friendOptions
    )

    return listOf(servant0, servant1, ce0, ce1, friend0, friend1)
}

private fun getCurrentEntries(
    storageProvider: StorageProvider,
    kind: SupportImageKind
): List<String> = storageProvider.list(kind).map {
    if (kind == SupportImageKind.Servant) {
        it
    } else {
        File(it).nameWithoutExtension
    }
}