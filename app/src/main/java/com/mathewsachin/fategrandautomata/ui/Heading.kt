package com.mathewsachin.fategrandautomata.ui

import android.content.res.Configuration
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import java.util.*

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
            if (landscape) text.toUpperCase(Locale.ROOT) else text,
            style = if (landscape) MaterialTheme.typography.subtitle2 else MaterialTheme.typography.h4,
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
    val shape = RoundedCornerShape(50)

    Surface(
        shape = shape,
        color = if (isDanger) MaterialTheme.colors.error else MaterialTheme.colors.primary,
        contentColor = if (isDanger) MaterialTheme.colors.onError else MaterialTheme.colors.onPrimary,
        modifier = modifier
            .padding(end = 5.dp)
            .animateContentSize()
            .clip(shape)
            .clickable(onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
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
                text.toUpperCase(Locale.ROOT),
                style = MaterialTheme.typography.subtitle2
            )
        }
    }
}

@Composable
fun FgaScaffold(
    heading: String,
    subheading: LazyListScope.() -> Unit,
    content: LazyListScope.() -> Unit,
    fab: @Composable BoxScope.() -> Unit = { },
    separator: Boolean = false
) {
    FgaTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Heading(
                    heading,
                    subheading = subheading
                )

                if (separator) {
                    Divider()
                }

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    content = content
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            fab()
        }
    }
}