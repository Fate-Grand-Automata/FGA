package io.github.fate_grand_automata.ui.custom_card_selection

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.fate_grand_automata.R
import io.github.fate_grand_automata.ui.Heading
import io.github.fate_grand_automata.ui.DimmedIcon
import io.github.fate_grand_automata.ui.dialog.FgaDialog
import io.github.fate_grand_automata.ui.icon
import kotlinx.coroutines.launch

@Composable
fun CustomCardSelectionView(
    vm: CustomCardSelectionViewModel
) {
    val pagerState = rememberPagerState(pageCount = { vm.turnSelections.size })
    val scope = rememberCoroutineScope()
    val infoDialog = FgaDialog()

    infoDialog.build {
        title(stringResource(R.string.p_custom_card_selection))
        message(stringResource(R.string.p_custom_card_selection_dialog_message))
        /**
         * "Define specific cards to use for each turn of the battle.\n\n" +
         *                 "If the cards selected are present in available cards, the script will click them in the exact order as defined.\n\n" +
         *                 "If even one of your required cards is missing from the hand, the script will ignore the custom selection and fall back to Card Priority settings.\n\n" +
         *                 "If selected cards are less than 3, more cards would be picked from rest automatically."*/
        buttons(
            onSubmit = { },
            showCancel = false
        )
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Heading(
                    text = "Custom Card Selection"
                )

                IconButton(
                    onClick = { infoDialog.show() }
                ) {
                    DimmedIcon(
                        icon(Icons.Default.Info),
                        contentDescription = "Info"
                    )
                }
            }

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
