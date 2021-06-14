package com.mathewsachin.fategrandautomata.ui.card_priority

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun CardPriorityScreen(
    vm: CardPriorityViewModel = viewModel()
) {
    CardPriorityView(items = vm.cardPriorityItems)

    DisposableEffect(vm) {
        onDispose {
            vm.save()
        }
    }
}