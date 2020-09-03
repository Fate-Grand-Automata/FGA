package com.mathewsachin.fategrandautomata.util

import android.content.Context
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.scripts.IScriptMessages
import com.mathewsachin.fategrandautomata.scripts.models.CommandCard
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlin.time.Duration

class ScriptMessages @Inject constructor(@ApplicationContext val context: Context) : IScriptMessages {
    override val apRanOut: String
        get() = context.getString(R.string.script_msg_ap_ran_out)

    override val stoppedByUser: String
        get() = "Script stopped by user or screen turned OFF"

    override val unexpectedError: String
        get() = "Unexpected Error"

    override val scriptExited: String
        get() = "Script Exited"

    override val inventoryFull: String
        get() = "Inventory Full"

    override val lotteryPresentBoxFull: String
        get() = "Present Box Full"

    override val supportImageMakerNotFound: String
        get() = "No support images were found on the current screen. Are you on Support selection or Friend list screen?"

    override val supportSelectionManual: String
        get() = "Support selection set to Manual"

    override val supportSelectionFriendNotSet: String
        get() = "When using 'friend' support selection mode, specify at least one friend name."

    override val supportSelectionPreferredNotSet: String
        get() = "When using 'preferred' support selection mode, specify at least one Servant or Craft Essence."

    override val ceDropped: String
        get() = "CE Dropped!"

    override val ceGet: String
        get() = "CE GET!"

    override val withdrawDisabled: String
        get() = "All servants have been defeated and auto-withdrawing is disabled."

    override fun timesRan(times: Int) = "Ran $times time(s)"

    override fun timesRanOutOf(times: Int, outOf: Int) =
        "Ran $times out of $outOf time(s)"

    override fun refillsUsedOutOf(used: Int, outOf: Int) =
        "$used refills used out of $outOf"

    override fun failedToDetermineCardType(card: CommandCard.Face) =
        "Failed to determine Card type: $card"

    override fun supportListUpdatedIn(duration: Duration) =
        "Support list will be updated in $duration"

    override fun timesWithdrew(times: Int) =
        "Withdrew $times time(s)"

    private val Duration.stringify: String
        get() =
            toComponents { hours, minutes, seconds, _ ->
                (if (hours > 0)
                    listOf(hours, minutes, seconds)
                else listOf(minutes, seconds))
                    .joinToString(":") { "%02d".format(it) }
            }

    override fun time(duration: Duration) =
        "Time: ${duration.stringify}"

    override fun avgTimePerRun(duration: Duration) =
        "Average time per run: ${duration.stringify}"

    override fun turns(min: Int, avg: Int, max: Int) =
        "Turns: $min (min), $avg (avg), $max (max)"

    override fun turns(turns: Int) = "Turns: $turns"
}