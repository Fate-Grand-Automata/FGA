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
import com.mathewsachin.fategrandautomata.scripts.enums.MaterialEnum
import com.mathewsachin.fategrandautomata.scripts.models.CommandCard
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences
import dagger.hilt.android.scopes.ServiceScoped
import timber.log.Timber
import timber.log.debug
import javax.inject.Inject
import kotlin.time.Duration

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
            ScriptNotify.CEDropped -> notify(ceDropped)
            ScriptNotify.CEGet -> notify(ceGet)
            is ScriptNotify.FailedToDetermineCards -> {
                val msg = failedToDetermineCardType(action.cards)

                toast(msg)

                Timber.debug { msg }
            }
            is ScriptNotify.SupportListUpdatingIn -> {
                toast(supportListUpdatedIn(action.time))
            }
            is ScriptNotify.WaitForAPRegen -> {
                toast(waitAPToast(action.minutes))
            }
            is ScriptNotify.BetweenRuns -> {
                val msg = makeRefillAndRunsMessage(
                    timesRefilled = action.refills,
                    timesRan = action.runs
                )

                toast(msg)
            }
        }

    fun makeRefillAndRunsMessage(
        timesRan: Int,
        timesRefilled: Int
    ) = buildString {
        val refill = prefs.refill

        if (refill.shouldLimitRuns && refill.limitRuns > 0) {
            appendLine(timesRanOutOf(timesRan, refill.limitRuns))
        } else if (timesRan > 0) {
            appendLine(timesRan(timesRan))
        }

        if (refill.resources.isNotEmpty()) {
            val refillRepetitions = refill.repetitions
            if (refillRepetitions > 0) {
                appendLine(refillsUsedOutOf(timesRefilled, refillRepetitions))
            }
        }
    }.trimEnd()

    val apRanOut: String
        get() = context.getString(R.string.script_msg_ap_ran_out)

    val stoppedByUser: String
        get() = context.getString(R.string.stopped_by_user)

    val unexpectedError: String
        get() = context.getString(R.string.unexpected_error)

    val scriptExited: String
        get() = context.getString(R.string.script_exited)

    val lotteryBoxResetIsDisabled: String
        get() = context.getString(R.string.lottery_reset_disabled)

    val inventoryFull: String
        get() = context.getString(R.string.inventory_full)

    val lotteryPresentBoxFull: String
        get() = context.getString(R.string.present_box_full)

    val supportImageMakerNotFound: String
        get() = context.getString(R.string.support_img_maker_not_found)

    val supportSelectionManual: String
        get() = context.getString(R.string.support_selection_manual)

    val supportSelectionFriendNotSet: String
        get() = context.getString(R.string.support_selection_friend_not_set)

    val supportSelectionPreferredNotSet: String
        get() = context.getString(R.string.support_selection_preferred_not_set)

    val ceDropped: String
        get() = context.getString(R.string.ce_dropped)

    val ceGet: String
        get() = context.getString(R.string.ce_get)

    val withdrawDisabled: String
        get() = context.getString(R.string.withdraw_disabled)

    fun timesRan(times: Int) =
        context.getString(R.string.times_ran, times)

    fun timesRanOutOf(times: Int, outOf: Int) =
        context.getString(R.string.times_ran_out_of, times, outOf)

    fun farmedMaterials(count: Int) =
        context.getString(R.string.mats_farmed, count)

    fun timesRolled(times: Int) =
        context.getString(R.string.times_rolled, times)

    fun materials(mats: Map<MaterialEnum, Int>) =
        mats.entries.joinToString { (mat, count) ->
            "${context.getString(mat.stringRes)}: $count"
        }

    fun refillsUsedOutOf(used: Int, outOf: Int) =
        context.getString(R.string.refills_used_out_of, used, outOf)

    fun failedToDetermineCardType(cards: List<CommandCard.Face>) =
        context.getString(R.string.failed_to_determine_card_type, cards)

    fun supportListUpdatedIn(duration: Duration) =
        context.getString(R.string.support_list_updated_in, duration.toString())

    fun timesWithdrew(times: Int) =
        context.getString(R.string.times_withdrew, times)

    private val Duration.stringify: String
        get() =
            toComponents { hours, minutes, seconds, _ ->
                (if (hours > 0)
                    listOf(hours, minutes, seconds)
                else listOf(minutes, seconds))
                    .joinToString(":") { "%02d".format(it) }
            }

    fun time(duration: Duration) =
        context.getString(R.string.battle_time, duration.stringify)

    fun avgTimePerRun(duration: Duration) =
        context.getString(R.string.avg_time_per_run, duration.stringify)

    fun turns(min: Int, avg: Int, max: Int) =
        context.getString(R.string.turns_stats, min, avg, max)

    fun turns(turns: Int) =
        context.getString(R.string.turns_count, turns)

    fun pickedExpStack(stacks: Int) =
        context.getString(R.string.picked_exp_stacks, stacks)

    fun waitAPToast(minutes: Int) =
        context.getString(R.string.wait_ap_regen_toast_message, minutes)
}