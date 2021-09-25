package com.mathewsachin.fategrandautomata.ui.card_priority

import android.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.scripts.enums.CardAffinityEnum
import com.mathewsachin.fategrandautomata.scripts.enums.CardTypeEnum
import com.mathewsachin.fategrandautomata.scripts.models.CardScore
import com.mathewsachin.fategrandautomata.ui.drag_sort.DragSort
import com.mathewsachin.fategrandautomata.ui.drag_sort.DragSortAdapter

@Composable
fun CardPriorityDragSort(
    scores: MutableList<CardScore>
) {
    val context = LocalContext.current

    DragSort(
        items = scores,
        viewConfigGrabber = {
            DragSortAdapter.ItemViewConfig(
                foregroundColor = Color.WHITE,
                backgroundColor = context.getColor(it.getColorRes()),
                text = it.toString()
            )
        }
    )
}

fun CardScore.getColorRes(): Int {
    return when (CardType) {
        CardTypeEnum.Buster -> when (CardAffinity) {
            CardAffinityEnum.Weak -> R.color.colorBusterWeak
            CardAffinityEnum.Normal -> R.color.colorBuster
            CardAffinityEnum.Resist -> R.color.colorBusterResist
        }
        CardTypeEnum.Arts -> when (CardAffinity) {
            CardAffinityEnum.Weak -> R.color.colorArtsWeak
            CardAffinityEnum.Normal -> R.color.colorArts
            CardAffinityEnum.Resist -> R.color.colorArtsResist
        }
        CardTypeEnum.Quick -> when (CardAffinity) {
            CardAffinityEnum.Weak -> R.color.colorQuickWeak
            CardAffinityEnum.Normal -> R.color.colorQuick
            CardAffinityEnum.Resist -> R.color.colorQuickResist
        }
        CardTypeEnum.Unknown -> R.color.colorPrimaryDark
    }
}