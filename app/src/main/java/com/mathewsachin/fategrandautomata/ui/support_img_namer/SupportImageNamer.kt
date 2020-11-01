package com.mathewsachin.fategrandautomata.ui.support_img_namer

import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ScrollView
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.StorageDirs
import com.mathewsachin.fategrandautomata.accessibility.ScriptRunnerUserInterface
import com.mathewsachin.fategrandautomata.scripts.entrypoints.getCeImgPath
import com.mathewsachin.fategrandautomata.scripts.entrypoints.getFriendImgPath
import com.mathewsachin.fategrandautomata.scripts.entrypoints.getServantImgPath
import com.mathewsachin.fategrandautomata.util.dayNightThemed
import com.mathewsachin.fategrandautomata.util.showOverlayDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

// *, ?, \, |, / are special characters in Regex and need to be escaped using \
private const val InvalidChars = """<>"\|:\*\?\\\/"""
private const val FileNameRegex = """[^\.\s$InvalidChars][^$InvalidChars]*"""

val ServantRegex = Regex("""$FileNameRegex(/$FileNameRegex)?""")
val CeRegex = Regex(FileNameRegex)

private const val InvalidCharsMsg = "<, >, \", |, :, *, ?, \\, /"

private fun getSupportEntries(
    Frame: View,
    storageDirs: StorageDirs
): List<SupportImgEntry> {
    val tempDir = storageDirs.supportImgTempDir

    val context = Frame.context
    val servantInvalidMsg = context.getString(R.string.support_img_namer_servant_invalid_message, InvalidCharsMsg)
    val ceOrFriendInvalidMsg = context.getString(R.string.support_img_namer_ce_or_friend_invalid_message, InvalidCharsMsg)

    val servant0 = SupportImgEntry(
        getServantImgPath(
            tempDir,
            0
        ),
        storageDirs.supportServantImgFolder,
        Frame.findViewById(R.id.support_img_servant_0),
        ServantRegex, servantInvalidMsg
    )
    val servant1 = SupportImgEntry(
        getServantImgPath(
            tempDir,
            1
        ),
        storageDirs.supportServantImgFolder,
        Frame.findViewById(R.id.support_img_servant_1),
        ServantRegex, servantInvalidMsg
    )

    val ce0 = SupportImgEntry(
        getCeImgPath(
            tempDir,
            0
        ),
        storageDirs.supportCeFolder,
        Frame.findViewById(R.id.support_img_ce_0),
        CeRegex, ceOrFriendInvalidMsg
    )
    val ce1 = SupportImgEntry(
        getCeImgPath(
            tempDir,
            1
        ),
        storageDirs.supportCeFolder,
        Frame.findViewById(R.id.support_img_ce_1),
        CeRegex, ceOrFriendInvalidMsg
    )

    val friend0 = SupportImgEntry(
        getFriendImgPath(
            tempDir,
            0
        ),
        storageDirs.supportFriendFolder,
        Frame.findViewById(R.id.support_img_friend_0),
        CeRegex, ceOrFriendInvalidMsg
    )
    val friend1 = SupportImgEntry(
        getFriendImgPath(
            tempDir,
            1
        ),
        storageDirs.supportFriendFolder,
        Frame.findViewById(R.id.support_img_friend_1),
        CeRegex, ceOrFriendInvalidMsg
    )

    return listOf(servant0, servant1, ce0, ce1, friend0, friend1)
}

suspend fun showSupportImageNamer(UI: ScriptRunnerUserInterface, storageDirs: StorageDirs) = withContext(Dispatchers.Main) {
    val context = UI.Service.applicationContext
    val themedContext = context.dayNightThemed()
    val frame = FrameLayout(themedContext)

    val inflater = LayoutInflater.from(themedContext)
    inflater.inflate(R.layout.support_img_namer, frame)

    val content = ScrollView(themedContext).apply {
        addView(frame)
        setPadding(72, 20, 0, 0)
    }

    val entryList = getSupportEntries(frame, storageDirs)

    suspendCancellableCoroutine<Unit> { coroutine ->
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
                .setOnDismissListener {
                    coroutine.resume(Unit)
                }
        }
    }
}