package com.mathewsachin.fategrandautomata.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment

class OpenDocTreePersistable : ActivityResultContracts.OpenDocumentTree() {
    override fun createIntent(context: Context, input: Uri?) =
        super.createIntent(context, input).apply {
            addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            addFlags(Intent.FLAG_GRANT_PREFIX_URI_PERMISSION)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        }
}

fun Fragment.registerPersistableDirPicker(callback: (Uri) -> Unit) =
    registerForActivityResult(OpenDocTreePersistable()) {
        if (it != null) {
            callback(it)
        }
    }