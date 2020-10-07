package com.mathewsachin.fategrandautomata.util

import android.content.Context
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.ContextThemeWrapper
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.accessibility.ScriptRunnerUserInterface
import com.mathewsachin.fategrandautomata.scripts.enums.RefillResourceEnum

fun Context.dayNightThemed() = ContextThemeWrapper(this, androidx.appcompat.R.style.Theme_AppCompat_DayNight_Dialog)

fun showOverlayDialog(context: Context, builder: AlertDialog.Builder.() -> Unit): AlertDialog {
    val alertDialog = AlertDialog.Builder(context.dayNightThemed())
        .apply(builder)
        .create()

    alertDialog.window?.setType(ScriptRunnerUserInterface.overlayType)
    alertDialog.show()

    return alertDialog
}

val RefillResourceEnum.stringRes
    get() = when (this) {
        RefillResourceEnum.SQ -> R.string.p_refill_type_sq
        RefillResourceEnum.Gold -> R.string.p_refill_type_gold
        RefillResourceEnum.Silver -> R.string.p_refill_type_silver
        RefillResourceEnum.Bronze -> R.string.p_refill_type_bronze
    }