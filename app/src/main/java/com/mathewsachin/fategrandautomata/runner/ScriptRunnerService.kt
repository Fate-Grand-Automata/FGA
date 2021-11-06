package com.mathewsachin.fategrandautomata.runner

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.IBinder
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ScriptRunnerService: Service() {
    companion object {
        private val mServiceStarted = mutableStateOf(false)
        val serviceStarted: State<Boolean> = mServiceStarted

        private var instance: ScriptRunnerService? = null
            private set(value) {
                field = value
                mServiceStarted.value = value != null
            }

        fun startService(context: Context) {
            val intent = makeServiceIntent(context)

            ContextCompat.startForegroundService(context, intent)
        }

        private fun makeServiceIntent(context: Context) =
            Intent(context, ScriptRunnerService::class.java)

        fun stopService(context: Context): Boolean {
            val intent = makeServiceIntent(context)
            return context.stopService(intent)
        }

        var mediaProjectionToken: Intent? = null
            set(value) {
                field = value
                instance?.controller?.onNewMediaProjectionToken()
            }
    }

    @Inject
    lateinit var controller: ScriptRunnerServiceController

    override fun onDestroy() {
        controller.onDestroy()
        instance = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        instance = this
        controller.onCreate()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        controller.onScreenConfigChanged()
    }
}
