package com.mathewsachin.fategrandautomata.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun Heading(
    text: String,
    subheading: (LazyListScope.() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .padding(vertical = 16.dp)
    ) {
        Text(
            text,
            style = MaterialTheme.typography.h4,
            modifier = Modifier
                .padding(start = 16.dp)
        )

        if (subheading != null) {
            LazyRow(
                contentPadding = PaddingValues(16.dp, 0.dp),
                modifier = Modifier.padding(top = 5.dp)
            ) {
                subheading()
            }
        }
    }
}