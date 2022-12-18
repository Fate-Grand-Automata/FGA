package com.mathewsachin.fategrandautomata.util

import android.app.Service
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.runner.ScriptRunnerNotification
import com.mathewsachin.fategrandautomata.scripts.IScriptMessages
import com.mathewsachin.fategrandautomata.scripts.ScriptLog
import com.mathewsachin.fategrandautomata.scripts.ScriptNotify
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences
import dagger.hilt.android.scopes.ServiceScoped
import timber.log.Timber
import javax.inject.Inject
import kotlin.time.DurationUnit

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

    override fun log(item: ScriptLog) =
        when (item) {
            ScriptLog.DefaultSupportBounds -> {
                Timber.d("Default Region being returned")
            }
            is ScriptLog.CurrentParty -> {
                Timber.d("Current Party: ${item.party}")
            }
            is ScriptLog.MaxSkills -> {
                Timber.d(
                    // Detected skill levels as string for debugging
                    item.isSkillMaxed
                        .zip(item.needMaxedSkills)
                        .joinToString("/") { (success, shouldBeMaxed) ->
                            when {
                                !shouldBeMaxed -> "x"
                                success -> "10"
                                else -> "f"
                            }
                        }
                )
            }
            ScriptLog.DefaultMasterOffset -> {
                Timber.d("Defaulting master offset")
            }
            is ScriptLog.ClickingNPs -> {
                Timber.d("Clicking NP(s): ${item.nps}")
            }
            is ScriptLog.ClickingCards -> {
                Timber.d("Clicking cards: ${item.cards}")
            }
            is ScriptLog.NPsGroupedByFaceCards -> {
                Timber.d("NPs grouped with Face-cards: ${item.groups}")
            }
            is ScriptLog.SupportFaceCardGroup -> {
                Timber.d("Support group: ${item.group}")
            }
            is ScriptLog.FaceCardGroups -> {
                Timber.d("Face-card groups: ${item.groups}")
            }
            is ScriptLog.ServantEnteredSlot -> {
                Timber.d("Servant: ${item.servant} in Slot: ${item.slot}")
            }
            is ScriptLog.CardsBelongToServant -> {
                val supportText = if (item.isSupport) "Support " else ""
                Timber.d("${item.cards} belong to ${supportText}${item.servant}")
            }
        }

    override fun notify(action: ScriptNotify) =
        when (action) {
            ScriptNotify.CEDropped -> notify(context.getString(R.string.ce_dropped))
            ScriptNotify.CEGet -> notify(context.getString(R.string.ce_get))
            is ScriptNotify.FailedToDetermineCards -> {
                var msg = context.getString(R.string.failed_to_determine_card, action.cards)
                if (action.unknownCardTypes) {
                    msg += "\n" + context.getString(R.string.unknown_card_type)
                }
                if (action.unknownServants) {
                    msg += "\n" + context.getString(R.string.unknown_servant_card)
                }

                toast(msg)

                Timber.d(msg)
            }
            is ScriptNotify.SupportListUpdatingIn -> {
                toast(
                    context.getString(R.string.support_list_updated_in, action.time.toString(DurationUnit.SECONDS, 2))
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
                    timesRan = action.runs,
                    ceDropCount = action.ceDrops
                )

                toast(msg)
            }
        }

    private fun makeRefillAndRunsMessage(
        timesRan: Int,
        timesRefilled: Int,
        ceDropCount: Int
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

        if (ceDropCount > 0) {
            appendLine(
                context.getString(R.string.ces_dropped, ceDropCount)
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