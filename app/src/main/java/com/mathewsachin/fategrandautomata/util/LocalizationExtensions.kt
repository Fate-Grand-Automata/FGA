package com.mathewsachin.fategrandautomata.util

import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.scripts.enums.BraveChainEnum
import com.mathewsachin.fategrandautomata.scripts.enums.RefillResourceEnum
import com.mathewsachin.fategrandautomata.scripts.enums.SpamEnum

val RefillResourceEnum.stringRes
    get() = when (this) {
        RefillResourceEnum.SQ -> R.string.p_refill_type_sq
        RefillResourceEnum.Gold -> R.string.p_refill_type_gold
        RefillResourceEnum.Silver -> R.string.p_refill_type_silver
        RefillResourceEnum.Bronze -> R.string.p_refill_type_bronze
    }

val BraveChainEnum.stringRes
    get() = when (this) {
        BraveChainEnum.None -> R.string.p_brave_chains_don_t_care
        BraveChainEnum.WithNP -> R.string.p_brave_chains_with_np
        BraveChainEnum.Avoid -> R.string.p_brave_chains_avoid
    }

val SpamEnum.stringRes
    get() = when (this) {
        SpamEnum.None -> R.string.p_spam_none
        SpamEnum.Spam -> R.string.p_spam_spam
        SpamEnum.Danger -> R.string.p_spam_danger
    }