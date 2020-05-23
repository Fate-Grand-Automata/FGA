package com.mathewsachin.fategrandautomata.ui.support_img_namer

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.scripts.entrypoints.getCeImgPath
import com.mathewsachin.fategrandautomata.scripts.entrypoints.getServantImgPath
import kotlinx.android.synthetic.main.support_img_namer.*

const val SupportImageIdKey = "SupportImageIdKey"

// *, ?, \, |, / are special characters in Regex and need to be escaped using \
private const val InvalidChars = """<>"\|:\*\?\\\/"""
private const val FileNameRegex = """[^\.\s$InvalidChars][^$InvalidChars]*"""

val ServantRegex = Regex("""$FileNameRegex(/$FileNameRegex)?""")
val CeRegex = Regex(FileNameRegex)

private const val InvalidCharsMsg = "<, >, \", |, :, *, ?, \\, /"
const val ServantInvalidMsg = "Please check your Servant names again. \n\nYou're not allowed to specify more than 1 folder, files cannot start with a period or space, and these symbols cannot be used: $InvalidCharsMsg"
const val CeInvalidMsg = "Please check your CE names again. \n\nYou're not allowed to specify folders, files cannot start with a period or space, and these symbols cannot be used: $InvalidCharsMsg"

class SupportImageNamerActivity : AppCompatActivity() {
    private lateinit var servant0: SupportImgEntry
    private lateinit var servant1: SupportImgEntry
    private lateinit var ce0: SupportImgEntry
    private lateinit var ce1: SupportImgEntry
    private lateinit var entryList: List<SupportImgEntry>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.support_img_namer)

        setSupportActionBar(support_img_namer_toolbar)

        val extras = intent.extras
        val supportImgId = extras?.getString(SupportImageIdKey)
            ?: return

        servant0 = SupportImgEntry(
            getServantImgPath(supportImgId, 0),
            image_servant_0, del_servant_0, text_servant_0,
            this, ServantRegex, ServantInvalidMsg
        )
        servant1 = SupportImgEntry(
            getServantImgPath(supportImgId, 1),
            image_servant_1, del_servant_1, text_servant_1,
            this, ServantRegex, ServantInvalidMsg
        )
        ce0 = SupportImgEntry(
            getCeImgPath(supportImgId, 0),
            image_ce_0, del_ce_0, text_ce_0,
            this, CeRegex, CeInvalidMsg
        )
        ce1 = SupportImgEntry(
            getCeImgPath(supportImgId, 1),
            image_ce_1, del_ce_1, text_ce_1,
            this, CeRegex, CeInvalidMsg
        )

        entryList = listOf(servant0, servant1, ce0, ce1)
    }

    override fun onDestroy() {
        // these objects contain a reference to activity context
        for (entry in entryList) {
            entry.close()
        }

        super.onDestroy()
    }

    private fun renameSupportImages() {
        if (!entryList.all { it.isValid() })
            return

        if (!entryList.all { it.rename() })
            return

        finish()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.support_img_namer_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.action_rename_support_imgs -> {
                renameSupportImages()
                true
            }
            R.id.action_rename_support_imgs_discard -> {
                discard()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun discard() {
        AlertDialog.Builder(this)
            .setMessage("Do you want to delete all images and exit?")
            .setTitle("Confirm Deletion")
            .setPositiveButton(android.R.string.yes) { _, _ ->
                for (file in entryList.map { it.ImgPath })
                {
                    if (file.exists()) {
                        file.delete()
                    }
                }

                finish()
            }
            .setNegativeButton(android.R.string.no, null)
            .show()
    }

    override fun onBackPressed() {
        AlertDialog.Builder(this)
            .setMessage("If you exit now, the images will not be renamed. Do you still want to exit?")
            .setTitle("Confirm Exit")
            .setPositiveButton(android.R.string.yes) { _, _ -> super.onBackPressed() }
            .setNegativeButton(android.R.string.no, null)
            .show()
    }
}
