package com.mathewsachin.fategrandautomata.ui.card_priority

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mathewsachin.fategrandautomata.ui.FgaScreen

@Composable
fun CardPriorityScreen(
    vm: CardPriorityViewModel = viewModel()
) {
    FgaScreen {
        CardPriorityView(items = vm.cardPriorityItems)
    }

    DisposableEffect(vm) {
        onDispose {
            vm.save()
        }
    }
}