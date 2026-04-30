package io.github.fate_grand_automata.util

import android.content.Context
import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.ContextThemeWrapper

val overlayType: Int
    get() {
        @Suppress("DEPRECATION")
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }
    }

fun Context.dayNightThemed() = ContextThemeWrapper(this, androidx.appcompat.R.style.Theme_AppCompat_DayNight_Dialog)

fun showOverlayDialog(context: Context, builder: AlertDialog.Builder.() -> Unit): AlertDialog {
    val alertDialog = AlertDialog.Builder(context.dayNightThemed())
        .apply(builder)
        .create()

    alertDialog.window?.setType(overlayType)
    alertDialog.show()

    return alertDialog
}

/**
 * Used with MediaProjection so that we only get landscape images,
 * since the frame size can't be changed during a projection.
 */
fun DisplayMetrics.makeLandscape() = apply {
    // Retrieve images in Landscape
    if (heightPixels > widthPixels) {
        val temp = widthPixels
        widthPixels = heightPixels
        heightPixels = temp
    }
}
