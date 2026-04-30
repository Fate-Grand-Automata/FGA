package io.github.fate_grand_automata.ui.support_img_namer

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ScrollView
import io.github.fate_grand_automata.R
import io.github.fate_grand_automata.SupportImageKind
import io.github.fate_grand_automata.scripts.entrypoints.SupportImageMaker
import io.github.fate_grand_automata.util.StorageProvider
import io.github.fate_grand_automata.util.dayNightThemed
import io.github.fate_grand_automata.util.showOverlayDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

private fun getSupportEntries(
    frame: View,
    storageProvider: StorageProvider,
): List<SupportImgEntry> {
    val tempDir = storageProvider.supportImageTempDir

    val servant0 = SupportImgEntry(
        SupportImageMaker.getServantImgPath(
            tempDir,
            0,
        ),
        SupportImageKind.Servant,
        frame.findViewById(R.id.support_img_servant_0),
    )
    val servant1 = SupportImgEntry(
        SupportImageMaker.getServantImgPath(
            tempDir,
            1,
        ),
        SupportImageKind.Servant,
        frame.findViewById(R.id.support_img_servant_1),
    )

    val ce0 = SupportImgEntry(
        SupportImageMaker.getCeImgPath(
            tempDir,
            0,
        ),
        SupportImageKind.CE,
        frame.findViewById(R.id.support_img_ce_0),
    )
    val ce1 = SupportImgEntry(
        SupportImageMaker.getCeImgPath(
            tempDir,
            1,
        ),
        SupportImageKind.CE,
        frame.findViewById(R.id.support_img_ce_1),
    )
    val ce2 = SupportImgEntry(
        SupportImageMaker.getCeImgPath(
            tempDir,
            2,
        ),
        SupportImageKind.CE,
        frame.findViewById(R.id.support_img_ce_2),
    )
    val ce3 = SupportImgEntry(
        SupportImageMaker.getCeImgPath(
            tempDir,
            3,
        ),
        SupportImageKind.CE,
        frame.findViewById(R.id.support_img_ce_3),
    )
    val ce4 = SupportImgEntry(
        SupportImageMaker.getCeImgPath(
            tempDir,
            4,
        ),
        SupportImageKind.CE,
        frame.findViewById(R.id.support_img_ce_4),
    )
    val ce5 = SupportImgEntry(
        SupportImageMaker.getCeImgPath(
            tempDir,
            5,
        ),
        SupportImageKind.CE,
        frame.findViewById(R.id.support_img_ce_5),
    )
    val friend0 = SupportImgEntry(
        SupportImageMaker.getFriendImgPath(
            tempDir,
            0,
        ),
        SupportImageKind.Friend,
        frame.findViewById(R.id.support_img_friend_0),
    )
    val friend1 = SupportImgEntry(
        SupportImageMaker.getFriendImgPath(
            tempDir,
            1,
        ),
        SupportImageKind.Friend,
        frame.findViewById(R.id.support_img_friend_1),
    )

    return listOf(servant0, servant1, ce0, ce1, ce2, ce3, ce4, ce5, friend0, friend1)
}

suspend fun showSupportImageNamer(context: Context, storageProvider: StorageProvider) = withContext(Dispatchers.Main) {
    val themedContext = context.dayNightThemed()
    val frame = FrameLayout(themedContext)

    val inflater = LayoutInflater.from(themedContext)
    inflater.inflate(R.layout.support_img_namer, frame)

    val content = ScrollView(themedContext).apply {
        addView(frame)
        setPadding(72, 20, 0, 0)
    }

    val entryList = getSupportEntries(frame, storageProvider)

    suspendCancellableCoroutine { coroutine ->
        showOverlayDialog(context) {
            setCancelable(false)
                .setTitle(context.getString(R.string.support_img_namer_title))
                .setView(content)
                .setPositiveButton(context.getString(R.string.support_img_namer_done)) { dialog, _ ->
                    if (entryList.all { it.isValid() }) {
                        if (entryList.all { it.rename(storageProvider) }) {
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
