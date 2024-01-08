package io.github.fate_grand_automata.ui.release_notes

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import io.github.fate_grand_automata.R


@Composable
fun ReleaseNotesScreen(
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.release_notes),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack
                    ) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "navigation")
                    }
                },
                modifier = Modifier
            )
        }
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier.padding(16.dp, 8.dp),
            contentPadding = paddingValues
        ) {
            item {
                Text(
                    buildReleaseNotesSummary(summary = stringResource(id = R.string.release_notes_summary)),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
private fun buildReleaseNotesSummary(
    summary: String
) = buildAnnotatedString {
    append(summary)
}
