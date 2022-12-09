package com.mathewsachin.fategrandautomata.ui.fine_tune

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.ui.Heading
import com.mathewsachin.fategrandautomata.ui.Tabbed

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
                                    .padding(bottom = 16.dp)
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