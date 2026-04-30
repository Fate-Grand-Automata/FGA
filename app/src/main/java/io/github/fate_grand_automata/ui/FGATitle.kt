package io.github.fate_grand_automata.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign

@Composable
fun FGATitle(
    text: String,
    modifier: Modifier = Modifier,
) = Text(
    text,
    textAlign = TextAlign.Center,
    color = MaterialTheme.colorScheme.onSurface,
    modifier = modifier
        .fillMaxWidth(),
)
