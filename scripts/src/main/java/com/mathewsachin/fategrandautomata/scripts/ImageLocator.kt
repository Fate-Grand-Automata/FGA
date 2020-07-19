package com.mathewsachin.fategrandautomata.scripts

import com.mathewsachin.libautomata.IPattern

interface IImageLoader {
    fun loadRegionPattern(path: String): IPattern

    fun loadSupportPattern(path: String): IPattern
}

private lateinit var ImgLoader: IImageLoader

fun initImageLoader(imgLoader: IImageLoader) {
    ImgLoader = imgLoader
}

object ImageLocator {
    private fun load(path: String) = ImgLoader.loadRegionPattern(path)

    fun loadSupportPattern(path: String) = ImgLoader.loadSupportPattern(path)

    val battle get() = load("battle.png")

    val targetDanger get() = load("target_danger.png")

    val targetServant get() = load("target_servant.png")

    val buster get() = load("buster.png")

    val art get() = load("art.png")

    val quick get() = load("quick.png")

    val weak get() = load("weak.png")

    val resist get() = load("resist.png")

    val friend get() = load("friend.png")

    val limitBroken get() = load("limitbroken.png")

    val supportScreen get() = load("support_screen.png")

    val supportRegionTool
        get() = load(
            "support_region_tool.png"
        )

    val storySkip get() = load("storyskip.png")

    val menu get() = load("menu.png")

    val stamina get() = load("stamina.png")

    val result get() = load("result.png")

    val bond get() = load("bond.png")

    val bond10Reward get() = load("ce_reward.png")

    val friendRequest get() = load("friendrequest.png")

    val confirm get() = load("confirm.png")

    val questReward get() = load("questreward.png")

    val retry get() = load("retry.png")

    val withdraw get() = load("withdraw.png")

    val finishedLotteryBox
        get() = load(
            "lottery.png"
        )

    val presentBoxFull get() = load("StopGifts.png")

    val masterExp get() = load("master_exp.png")

    // TODO: Verify the CN image
    val masterLvlUp get() = load("master_lvl_up.png")

    val matRewards get() = load("mat_rewards.png")

    val gudaFinalRewards
        get() = load(
            "guda_final_rewards.png"
        )

    val inventoryFull get() = load("inven_full.png")

    val fpSummonContinue
        get() = load(
            "fp_continue.png"
        )
}