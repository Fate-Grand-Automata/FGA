package io.github.fate_grand_automata.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat

class ScreenOffReceiver : BroadcastReceiver() {

    private var registered = false

    fun register(Context: Context, listener: () -> Unit) {
        if (registered) return

        val filter = IntentFilter(Intent.ACTION_SCREEN_OFF)

        ContextCompat.registerReceiver(Context, this, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
        registered = true

        screenOffListener = listener
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_SCREEN_OFF -> screenOffListener()
        }
    }

    fun unregister(Context: Context) {
        // onDestroy can run without a matching register() if service startup bailed out
        if (!registered) return

        Context.unregisterReceiver(this)
        registered = false

        screenOffListener = { }
    }

    private var screenOffListener: () -> Unit = { }
}
