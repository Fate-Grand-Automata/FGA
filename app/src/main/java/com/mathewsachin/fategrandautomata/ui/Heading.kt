package com.mathewsachin.fategrandautomata.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
        Text(
            text,
            style = MaterialTheme.typography.h4,
            modifier = Modifier
                .padding(start = 16.dp)
        )

        if (subheading != null) {
            LazyRow(
                contentPadding = PaddingValues(16.dp, 0.dp),
                modifier = Modifier.padding(top = 16.dp)
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
    color: Color = MaterialTheme.colors.primary
) {
    val shape = RoundedCornerShape(50)

    Surface(
        border = BorderStroke(1.dp, color),
        shape = shape,
        color = Color.Transparent,
        contentColor = color,
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