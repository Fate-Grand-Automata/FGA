package io.github.fate_grand_automata.ui.custom_card_selection

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun CustomCardSelectionScreen(
    vm: CustomCardSelectionViewModel = viewModel()
) {
    CustomCardSelectionView(
        vm = vm
    )
}
