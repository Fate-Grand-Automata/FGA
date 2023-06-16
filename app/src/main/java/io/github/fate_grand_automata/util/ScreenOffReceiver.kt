package io.github.fate_grand_automata.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter

class ScreenOffReceiver : BroadcastReceiver() {

    fun register(Context: Context, listener: () -> Unit) {
        val filter = IntentFilter(Intent.ACTION_SCREEN_OFF)

        Context.registerReceiver(this, filter)

        screenOffListener = listener
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_SCREEN_OFF -> screenOffListener()
        }
    }

    fun unregister(Context: Context) {
        Context.unregisterReceiver(this)

        screenOffListener = { }
    }

    private var screenOffListener: () -> Unit = { }
}
