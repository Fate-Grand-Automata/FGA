package com.mathewsachin.fategrandautomata.scripts

import com.mathewsachin.fategrandautomata.scripts.enums.MaterialEnum
import com.mathewsachin.fategrandautomata.scripts.models.CommandCard
import kotlin.time.Duration

interface IScriptMessages {
    val apRanOut: String
    val inventoryFull: String
    val lotteryPresentBoxFull: String
    val supportImageMakerNotFound: String
    val supportSelectionManual: String
    val supportSelectionFriendNotSet: String
    val supportSelectionPreferredNotSet: String
    val ceDropped: String
    val ceGet: String
    val withdrawDisabled: String
    val cannotDetectScriptType: String
    val stoppedByUser: String
    val unexpectedError: String
    val scriptExited: String

    fun timesRan(times: Int): String
    fun timesRanOutOf(times: Int, outOf: Int): String
    fun farmedMaterials(count: Int): String

    fun materials(mats: Map<MaterialEnum, Int>): String

    fun refillsUsedOutOf(used: Int, outOf: Int): String

    fun failedToDetermineCardType(cards: List<CommandCard.Face>): String

    fun supportListUpdatedIn(duration: Duration): String

    fun timesWithdrew(times: Int): String

    fun time(duration: Duration): String

    fun avgTimePerRun(duration: Duration): String

    fun turns(min: Int, avg: Int, max: Int): String

    fun turns(turns: Int): String

    fun pickedExpStack(stacks: Int): String
}