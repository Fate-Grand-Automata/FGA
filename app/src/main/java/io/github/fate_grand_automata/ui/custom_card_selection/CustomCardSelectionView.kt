package io.github.fate_grand_automata.ui.custom_card_selection

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.fate_grand_automata.ui.Heading
import kotlinx.coroutines.launch

@Composable
fun CustomCardSelectionView(
    vm: CustomCardSelectionViewModel
) {
    val pagerState = rememberPagerState(pageCount = { vm.turnSelections.size })
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            Heading("Custom Card Selection")

            CustomCardSelectionTurnSelector(
                numberOfTurns = vm.turnSelections.size,
                selectedTurn = pagerState.currentPage,
                onSelectedTurnChange = { scope.launch { pagerState.animateScrollToPage(it) } },
                onAddTurn = { vm.addTurn() },
                onRemoveTurn = { vm.removeTurn(it) }
            )

            HorizontalDivider()

            HorizontalPager(
                state = pagerState,
                verticalAlignment = Alignment.Top,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) { pageIndex ->
                val selection = vm.turnSelections[pageIndex]
                
                CustomCardSelectionTurnItem(
                    selection = selection,
                    onSelectionChange = { vm.updateTurn(pageIndex, it) }
                )
            }
        }
    }
}
