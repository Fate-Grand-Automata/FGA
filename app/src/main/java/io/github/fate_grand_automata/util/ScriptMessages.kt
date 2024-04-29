package io.github.fate_grand_automata.util

import android.app.Service
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import dagger.hilt.android.scopes.ServiceScoped
import io.github.fate_grand_automata.R
import io.github.fate_grand_automata.runner.ScriptRunnerNotification
import io.github.fate_grand_automata.scripts.IScriptMessages
import io.github.fate_grand_automata.scripts.ScriptLog
import io.github.fate_grand_automata.scripts.ScriptNotify
import io.github.fate_grand_automata.scripts.prefs.IPreferences
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
                    ceDropCount = action.ceDrops,
                    teapotsCount = action.teapotsCount
                )

                toast(msg)
            }
            is ScriptNotify.BondLevelUp -> {
                notify(
                    context.getString(R.string.bond_level_up)
                )
            }
        }

    private fun makeRefillAndRunsMessage(
        timesRan: Int,
        timesRefilled: Int,
        ceDropCount: Int,
        teapotsCount: Int
    ) = buildString {
        val perServerConfigPref = prefs.selectedServerConfigPref

        if (perServerConfigPref.shouldLimitRuns && perServerConfigPref.limitRuns > 0) {
            appendLine(
                context.getString(R.string.times_ran_out_of, timesRan, perServerConfigPref.limitRuns)
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
        if (perServerConfigPref.shouldUseTeapots && teapotsCount > 0) {
            appendLine(
                context.getString(R.string.teapots_used, teapotsCount)
            )
        }

        if (perServerConfigPref.resources.isNotEmpty()) {
            val refillRepetitions = perServerConfigPref.currentAppleCount
            if (refillRepetitions > 0) {
                appendLine(
                    context.getString(R.string.refills_used_out_of, timesRefilled, refillRepetitions)
                )
            }
        }
    }.trimEnd()
}