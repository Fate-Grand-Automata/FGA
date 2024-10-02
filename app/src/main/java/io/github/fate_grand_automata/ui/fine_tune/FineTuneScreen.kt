package io.github.fate_grand_automata.ui.fine_tune

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.fate_grand_automata.R
import io.github.fate_grand_automata.ui.Heading
import io.github.fate_grand_automata.ui.Tabbed

@Composable
fun FineTuneScreen(
    vm: FineTuneSettingsViewModel = viewModel()
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Heading(stringResource(R.string.p_fine_tune))

        Box(
            modifier = Modifier
                .weight(1f)
        ) {
            Tabbed(
                items = vm.groups,
                heading = { Text(stringResource(it.name)) },
                content = { group ->
                    LazyColumn(
                        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp)
                    ) {
                        items(group.items) {
                            Card(
                                modifier = Modifier
                                    .padding(horizontal = 16.dp)
                                    .padding(bottom = 16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            ) {
                                it.FineTuneSetter()
                            }
                        }
                    }
                }
            )

            ExtendedFloatingActionButton(
                text = {
                    Text(
                        stringResource(R.string.fine_tune_menu_reset_to_defaults),
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                },
                onClick = { vm.resetAll() },
                icon = {
                    Icon(
                        painterResource(R.drawable.ic_refresh),
                        contentDescription = stringResource(R.string.fine_tune_menu_reset_to_defaults),
                        tint = MaterialTheme.colorScheme.onSecondary
                    )
                },
                containerColor = MaterialTheme.colorScheme.secondary,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            )
        }
    }
}