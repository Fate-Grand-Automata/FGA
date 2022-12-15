package com.mathewsachin.fategrandautomata.ui

import android.content.res.Configuration
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
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
        val landscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

        Text(
            if (landscape) text.uppercase() else text,
            style = if (landscape) MaterialTheme.typography.titleSmall else MaterialTheme.typography.headlineMedium,
            modifier = Modifier
                .padding(start = 16.dp)
        )

        if (subheading != null) {
            LazyRow(
                contentPadding = PaddingValues(16.dp, 0.dp),
                modifier = Modifier.padding(top = if (landscape) 7.dp else 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                subheading()
            }
        }
    }
}

@Composable
fun HeadingButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: VectorIcon? = null,
    isDanger: Boolean = false
) {
    Card(
        shape = CircleShape,
        colors = CardDefaults.cardColors(
            containerColor = if (isDanger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            contentColor = if (isDanger) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onPrimary
        ),
        modifier = modifier
            .padding(end = 5.dp)
            .animateContentSize()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable(onClick = onClick)
                .padding(10.dp, 4.dp)
        ) {
            if (icon != null) {
                Icon(
                    icon.asPainter(),
                    contentDescription = "icon",
                    modifier = Modifier
                        .padding(end = 7.dp)
                        .size(20.dp)
                )
            }

            Text(
                text.uppercase(),
                style = MaterialTheme.typography.titleSmall
            )
        }
    }
}