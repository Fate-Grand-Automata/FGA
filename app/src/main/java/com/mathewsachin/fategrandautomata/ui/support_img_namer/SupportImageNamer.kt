package com.mathewsachin.fategrandautomata.ui.support_img_namer

import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ScrollView
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.StorageDirs
import com.mathewsachin.fategrandautomata.accessibility.ScriptRunnerUserInterface
import com.mathewsachin.fategrandautomata.accessibility.dayNightThemed
import com.mathewsachin.fategrandautomata.accessibility.showOverlayDialog
import com.mathewsachin.fategrandautomata.scripts.ITemporaryStore
import com.mathewsachin.fategrandautomata.scripts.entrypoints.SupportImageMaker

// *, ?, \, |, / are special characters in Regex and need to be escaped using \
private const val InvalidChars = """<>"\|:\*\?\\\/"""
private const val FileNameRegex = """[^\.\s$InvalidChars][^$InvalidChars]*"""

val ServantRegex = Regex("""$FileNameRegex(/$FileNameRegex)?""")
val CeRegex = Regex(FileNameRegex)

private const val InvalidCharsMsg = "<, >, \", |, :, *, ?, \\, /"

private fun getSupportEntries(
    Frame: View,
    tempStore: ITemporaryStore,
    storageDirs: StorageDirs
): List<SupportImgEntry> {
    val context = Frame.context
    val servantInvalidMsg = context.getString(R.string.support_img_namer_servant_invalid_message, InvalidCharsMsg)
    val ceOrFriendInvalidMsg = context.getString(R.string.support_img_namer_ce_or_friend_invalid_message, InvalidCharsMsg)

    val servant0 = SupportImgEntry(
        tempStore,
        SupportImageMaker.getServantImgKey(0),
        storageDirs.supportServantImgFolder,
        Frame.findViewById(R.id.support_img_servant_0),
        ServantRegex, servantInvalidMsg
    )
    val servant1 = SupportImgEntry(
        tempStore,
        SupportImageMaker.getServantImgKey(1),
        storageDirs.supportServantImgFolder,
        Frame.findViewById(R.id.support_img_servant_1),
        ServantRegex, servantInvalidMsg
    )

    val ce0 = SupportImgEntry(
        tempStore,
        SupportImageMaker.getCeImgKey(0),
        storageDirs.supportCeFolder,
        Frame.findViewById(R.id.support_img_ce_0),
        CeRegex, ceOrFriendInvalidMsg
    )
    val ce1 = SupportImgEntry(
        tempStore,
        SupportImageMaker.getCeImgKey(1),
        storageDirs.supportCeFolder,
        Frame.findViewById(R.id.support_img_ce_1),
        CeRegex, ceOrFriendInvalidMsg
    )

    val friend0 = SupportImgEntry(
        tempStore,
        SupportImageMaker.getFriendImgKey(0),
        storageDirs.supportFriendFolder,
        Frame.findViewById(R.id.support_img_friend_0),
        CeRegex, ceOrFriendInvalidMsg
    )
    val friend1 = SupportImgEntry(
        tempStore,
        SupportImageMaker.getFriendImgKey(1),
        storageDirs.supportFriendFolder,
        Frame.findViewById(R.id.support_img_friend_1),
        CeRegex, ceOrFriendInvalidMsg
    )

    return listOf(servant0, servant1, ce0, ce1, friend0, friend1)
}

fun showSupportImageNamer(UI: ScriptRunnerUserInterface, tempStore: ITemporaryStore, storageDirs: StorageDirs) {
    val context = UI.Service.applicationContext
    val themedContext = context.dayNightThemed()
    val frame = FrameLayout(themedContext)

    val inflater = LayoutInflater.from(themedContext)
    inflater.inflate(R.layout.support_img_namer, frame)

    val content = ScrollView(themedContext).apply {
        addView(frame)
        setPadding(72, 20, 0, 0)
    }

    val entryList = getSupportEntries(frame, tempStore, storageDirs)

    showOverlayDialog(context) {
        setCancelable(false)
            .setTitle(UI.Service.getString(R.string.support_img_namer_title))
            .setView(content)
            .setPositiveButton(UI.Service.getString(R.string.support_img_namer_done)) { dialog, _ ->
                if (entryList.all { it.isValid() }) {
                    if (entryList.all { it.rename() }) {
                        dialog.dismiss()
                    }
                }
            }
            .setNegativeButton(android.R.string.cancel) { dialog, _ -> dialog.dismiss() }
    }
}