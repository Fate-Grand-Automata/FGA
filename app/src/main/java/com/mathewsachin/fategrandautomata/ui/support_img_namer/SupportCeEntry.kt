package com.mathewsachin.fategrandautomata.ui.support_img_namer

import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import java.io.File

class SupportCeEntry(
    ImgPath: File,
    ImgView: ImageView,
    DeleteBtn: ImageButton,
    TextBox: EditText
) : SupportImgEntry(ImgPath, ImgView, DeleteBtn, TextBox) {
    override val regex get() = CeRegex
    override val invalidMsg get() = CeInvalidMsg
}