package com.mathewsachin.fategrandautomata.util

import android.app.Service
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.accessibility.ScriptRunnerNotification
import com.mathewsachin.fategrandautomata.scripts.IScriptMessages
import com.mathewsachin.fategrandautomata.scripts.ScriptNotify
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences
import dagger.hilt.android.scopes.ServiceScoped
import timber.log.Timber
import timber.log.debug
import javax.inject.Inject

@ServiceScoped
class ScriptMessages @Inject constructor(
    service: Service,
    private val notification: ScriptRunnerNotification,
    private val prefs: IPreferences
) : IScriptMessages {
    private val context: Context = service

    private val handler by lazy {
        Handler(Looper.getMainLooper())
    }

    fun toast(message: String) {
        if (message.isNotBlank()) {
            handler.post {
                Toast
                    .makeText(context, message, Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    fun notify(message: String) = notification.message(message)

    override fun notify(action: ScriptNotify) =
        when (action) {
            ScriptNotify.CEDropped -> notify(context.getString(R.string.ce_dropped))
            ScriptNotify.CEGet -> notify(context.getString(R.string.ce_get))
            is ScriptNotify.FailedToDetermineCards -> {
                val msg = context.getString(R.string.failed_to_determine_card_type, action.cards)

                toast(msg)

                Timber.debug { msg }
            }
            is ScriptNotify.SupportListUpdatingIn -> {
                toast(
                    context.getString(R.string.support_list_updated_in, action.time.toString())
                )
            }
            is ScriptNotify.WaitForAPRegen -> {
                toast(
                    context.getString(R.string.wait_ap_regen_toast_message, action.minutes)
                )
            }
            is ScriptNotify.BetweenRuns -> {
                val msg = makeRefillAndRunsMessage(
                    timesRefilled = action.refills,
                    timesRan = action.runs
                )

                toast(msg)
            }
        }

    private fun makeRefillAndRunsMessage(
        timesRan: Int,
        timesRefilled: Int
    ) = buildString {
        val refill = prefs.refill

        if (refill.shouldLimitRuns && refill.limitRuns > 0) {
            appendLine(
                context.getString(R.string.times_ran_out_of, timesRan, refill.limitRuns)
            )
        } else if (timesRan > 0) {
            appendLine(
                context.getString(R.string.times_ran, timesRan)
            )
        }

        if (refill.resources.isNotEmpty()) {
            val refillRepetitions = refill.repetitions
            if (refillRepetitions > 0) {
                appendLine(
                    context.getString(R.string.refills_used_out_of, timesRefilled, refillRepetitions)
                )
            }
        }
    }.trimEnd()
}