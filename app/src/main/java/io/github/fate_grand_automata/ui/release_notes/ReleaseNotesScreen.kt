package io.github.fate_grand_automata.ui.release_notes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.fate_grand_automata.R
import io.github.fate_grand_automata.ui.openLinkIntent

@Composable
fun ReleaseNotesScreen(
    windowSizeClass: WindowSizeClass,
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            val summaryList = stringResource(id = R.string.release_notes_summary).split("<ln>\n")
            if (summaryList[0].lowercase() == "pr") {
                PullRequestReleaseNotes(
                    summaryList=summaryList
                )
            } else if (summaryList[0].contains("latest", ignoreCase = true)) {
                PublicReleaseNotes(
                    windowSizeClass = windowSizeClass,
                    summaryList = summaryList
                )
            } else {
                Text(
                    text = stringResource(id = R.string.release_notes_summary).removeNoteTags
                )
            }
        }
    }
}

@Composable
private fun PullRequestReleaseNotes(
    summaryList: List<String>,
) {
    val context = LocalContext.current
    LazyColumn(
        modifier = Modifier.padding(16.dp, 8.dp),
    ) {
        item {
            Text(
                text = buildAnnotatedString {
                    append("PR: ${summaryList[1].removeNoteTags} ")
                    addStringAnnotation(
                        tag = "URL",
                        annotation = summaryList[2].removeNoteTags,
                        start = 0,
                        end = summaryList[1].length
                    )
                },
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.clickable(
                    enabled = summaryList[2].isNotBlank(),
                    onClick = {
                        context.openLinkIntent(summaryList[2].removeNoteTags)
                    }
                )
            )
        }
        item {
            Divider()
        }
        for (i in 3 until summaryList.size) {
            item {
                Text(
                    text = summaryList[i].removeNoteTags,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

    }
}


@Composable
private fun PublicReleaseNotes(
    windowSizeClass: WindowSizeClass,
    summaryList: List<String>
) {
    LazyVerticalGrid(
        columns = when (windowSizeClass.widthSizeClass) {
            WindowWidthSizeClass.Medium -> GridCells.Fixed(2)
            WindowWidthSizeClass.Expanded -> GridCells.Fixed(3)
            else -> GridCells.Fixed(1)
        },
        contentPadding = PaddingValues(16.dp, 8.dp),
    ) {
        val releaseNotesList = summaryList.chunked(2).map {
            Pair(
                it[0].removeNoteTags,
                it[1].removeNoteTags
            )
        }
        releaseNotesList.forEach { (tag, note) ->
            item {
                ListItem(
                    headlineContent = {
                        Column {
                            Text(
                                text = tag,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Divider()
                        }
                    },
                    supportingContent = {
                        Text(
                            text = note,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },

                    )
            }
        }
    }
}

val String.removeNoteTags: String
    get() = this.replace("<ln>\n", "").trim()