package com.mathewsachin.fategrandautomata.scripts

import com.mathewsachin.fategrandautomata.scripts.getRegionPattern as load

class ImageLocator {
    companion object {
        val Battle get() = load("battle.png")

        val TargetDanger get() = load("target_danger.png")

        val TargetServant get() = load("target_servant.png")

        val Buster get() = load("buster.png")

        val Art get() = load("art.png")

        val Quick get() = load("quick.png")

        val Weak get() = load("weak.png")

        val Resist get() = load("resist.png")

        val Friend get() = load("friend.png")

        val LimitBroken get() = load("limitbroken.png")

        val SupportScreen get() = load("support_screen.png")

        val SupportRegionTool get() = load("support_region_tool.png")

        val StorySkip get() = load("storyskip.png")

        val Menu get() = load("menu.png")

        val Stamina get() = load("stamina.png")

        val Result get() = load("result.png")

        val Bond get() = load("bond.png")

        val Bond10Reward get() = load("ce_reward.png")

        val FriendRequest get() = load("friendrequest.png")

        val Confirm get() = load("confirm.png")

        val QuestReward get() = load("questreward.png")

        val Retry get() = load("retry.png")

        val Withdraw get() = load("withdraw.png")

        val FinishedLotteryBox get() = load("lottery.png")

        val PresentBoxFull get() = load("StopGifts.png")

        val MasterExp get() = load("master_exp.png")

        val MasterLvlUp get() = load("master_lvl_up.png")

        val MatRewards get() = load("mat_rewards.png")

        val GudaFinalRewards get() = load("guda_final_rewards.png")
    }
}