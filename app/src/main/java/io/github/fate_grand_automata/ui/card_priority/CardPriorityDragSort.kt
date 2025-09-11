package io.github.fate_grand_automata.ui.card_priority

import android.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import io.github.fate_grand_automata.R
import io.github.fate_grand_automata.scripts.enums.CardAffinityEnum
import io.github.fate_grand_automata.scripts.enums.CardTypeEnum
import io.github.fate_grand_automata.scripts.models.CardScore
import io.github.fate_grand_automata.ui.drag_sort.DragSort
import io.github.fate_grand_automata.ui.drag_sort.DragSortAdapter

@Composable
fun CardPriorityDragSort(
    scores: MutableList<CardScore>,
) {
    val context = LocalContext.current

    DragSort(
        items = scores,
        viewConfigGrabber = {
            DragSortAdapter.ItemViewConfig(
                foregroundColor = Color.WHITE,
                backgroundColor = context.getColor(it.getColorRes()),
                text = it.toString(),
            )
        },
    )
}

fun CardScore.getColorRes(): Int {
    return when (type) {
        CardTypeEnum.Buster -> when (affinity) {
            CardAffinityEnum.Weak -> R.color.colorBusterWeak
            CardAffinityEnum.Normal -> R.color.colorBuster
            CardAffinityEnum.Resist -> R.color.colorBusterResist
        }
        CardTypeEnum.Arts -> when (affinity) {
            CardAffinityEnum.Weak -> R.color.colorArtsWeak
            CardAffinityEnum.Normal -> R.color.colorArts
            CardAffinityEnum.Resist -> R.color.colorArtsResist
        }
        CardTypeEnum.Quick -> when (affinity) {
            CardAffinityEnum.Weak -> R.color.colorQuickWeak
            CardAffinityEnum.Normal -> R.color.colorQuick
            CardAffinityEnum.Resist -> R.color.colorQuickResist
        }
        CardTypeEnum.Unknown -> R.color.colorPrimaryDark
    }
}
