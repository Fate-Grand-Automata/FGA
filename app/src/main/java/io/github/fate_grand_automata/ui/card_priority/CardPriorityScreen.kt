package io.github.fate_grand_automata.ui.card_priority

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun CardPriorityScreen(
    vm: CardPriorityViewModel = viewModel(),
) {
    CardPriorityView(
        items = vm.cardPriorityItems,
        useServantPriority = vm.useServantPriority,
    )

    DisposableEffect(vm) {
        onDispose {
            vm.save()
        }
    }
}
