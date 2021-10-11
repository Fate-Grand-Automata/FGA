package com.mathewsachin.fategrandautomata.util

import android.app.Service
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.accessibility.ScriptRunnerNotification
import com.mathewsachin.fategrandautomata.scripts.IScriptMessages
import com.mathewsachin.fategrandautomata.scripts.ScriptLog
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

    override fun log(item: ScriptLog) =
        when (item) {
            ScriptLog.DefaultSupportBounds -> {
                Timber.debug { "Default Region being returned" }
            }
            is ScriptLog.CurrentParty -> {
                Timber.debug { "Current Party: ${item.party}" }
            }
            ScriptLog.RearrangingCards -> {
                Timber.debug { "Rearranging cards" }
            }
            is ScriptLog.MaxSkills -> {
                Timber.debug {
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
                }
            }
            ScriptLog.DefaultMasterOffset -> {
                Timber.debug { "Defaulting master offset" }
            }
            is ScriptLog.ClickingNPs -> {
                Timber.debug { "Clicking NP(s): ${item.nps}" }
            }
            is ScriptLog.ClickingCards -> {
                Timber.debug { "Clicking cards: ${item.cards}" }
            }
            is ScriptLog.NPsGroupedByFaceCards -> {
                Timber.debug { "NPs grouped with Face-cards: ${item.groups}" }
            }
            is ScriptLog.SupportFaceCardGroup -> {
                Timber.debug { "Support group: ${item.group}" }
            }
            is ScriptLog.FaceCardGroups -> {
                Timber.debug { "Face-card groups: ${item.groups}" }
            }
            is ScriptLog.ServantEnteredSlot -> {
                Timber.debug { "Servant: ${item.servant} in Slot: ${item.slot}" }
            }
            is ScriptLog.CardsBelongToServant -> {
                Timber.debug {
                    val supportText = if (item.isSupport) "Support " else ""

                    "${item.cards} belong to ${supportText}${item.servant}"
                }
            }
            is ScriptLog.CardsNotPickedByServantPriority -> {
                Timber.debug { "Cards not picked by servant priority ${item.cards}" }
            }
        }

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