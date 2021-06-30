package com.mathewsachin.fategrandautomata.util

import android.content.Context
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.scripts.IScriptMessages
import com.mathewsachin.fategrandautomata.scripts.enums.MaterialEnum
import com.mathewsachin.fategrandautomata.scripts.models.CommandCard
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlin.time.Duration

class ScriptMessages @Inject constructor(@ApplicationContext val context: Context) : IScriptMessages {
    override val apRanOut: String
        get() = context.getString(R.string.script_msg_ap_ran_out)

    override val stoppedByUser: String
        get() = context.getString(R.string.stopped_by_user)

    override val unexpectedError: String
        get() = context.getString(R.string.unexpected_error)

    override val scriptExited: String
        get() = context.getString(R.string.script_exited)

    override val inventoryFull: String
        get() = context.getString(R.string.inventory_full)

    override val lotteryPresentBoxFull: String
        get() = context.getString(R.string.present_box_full)

    override val supportImageMakerNotFound: String
        get() = context.getString(R.string.support_img_maker_not_found)

    override val supportSelectionManual: String
        get() = context.getString(R.string.support_selection_manual)

    override val supportSelectionFriendNotSet: String
        get() = context.getString(R.string.support_selection_friend_not_set)

    override val supportSelectionPreferredNotSet: String
        get() = context.getString(R.string.support_selection_preferred_not_set)

    override val ceDropped: String
        get() = context.getString(R.string.ce_dropped)

    override val ceGet: String
        get() = context.getString(R.string.ce_get)

    override val withdrawDisabled: String
        get() = context.getString(R.string.withdraw_disabled)

    override fun timesRan(times: Int) =
        context.getString(R.string.times_ran, times)

    override fun timesRanOutOf(times: Int, outOf: Int) =
        context.getString(R.string.times_ran_out_of, times, outOf)

    override fun farmedMaterials(count: Int) =
        context.getString(R.string.mats_farmed, count)

    override fun materials(mats: Map<MaterialEnum, Int>) =
        mats.entries.joinToString { (mat, count) ->
            "${context.getString(mat.stringRes)}: $count"
        }

    override fun refillsUsedOutOf(used: Int, outOf: Int) =
        context.getString(R.string.refills_used_out_of, used, outOf)

    override fun failedToDetermineCardType(cards: List<CommandCard.Face>) =
        context.getString(R.string.failed_to_determine_card_type, cards)

    override fun supportListUpdatedIn(duration: Duration) =
        context.getString(R.string.support_list_updated_in, duration.toString())

    override fun timesWithdrew(times: Int) =
        context.getString(R.string.times_withdrew, times)

    private val Duration.stringify: String
        get() =
            toComponents { hours, minutes, seconds, _ ->
                (if (hours > 0)
                    listOf(hours, minutes, seconds)
                else listOf(minutes, seconds))
                    .joinToString(":") { "%02d".format(it) }
            }

    override fun time(duration: Duration) =
        context.getString(R.string.battle_time, duration.stringify)

    override fun avgTimePerRun(duration: Duration) =
        context.getString(R.string.avg_time_per_run, duration.stringify)

    override fun turns(min: Int, avg: Int, max: Int) =
        context.getString(R.string.turns_stats, min, avg, max)

    override fun turns(turns: Int) =
        context.getString(R.string.turns_count, turns)

    override fun pickedExpStack(stacks: Int) =
        context.getString(R.string.picked_exp_stacks, stacks)

    override fun waitAPToast(minutes: Int) =
        context.getString(R.string.wait_ap_regen_toast_message, minutes)
}