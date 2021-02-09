package com.mathewsachin.fategrandautomata.ui.prefs.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val blueNeutral = Color(0xff61b4f4)
val blueLighter = Color(0xff98e6ff)
val blueDarker = Color(0xff1c85c1)

val tealNeutral = Color(0xff26a69a)
val tealLighter = Color(0xff64d8cb)
val tealDarker = Color(0xff00766c)

val grayLighter = Color(0xfffafafa)
val grayDarker = Color(0xFF303030)

val shapes = Shapes(
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(4.dp),
    large = RoundedCornerShape(0.dp)
)

// Set of Material typography styles to start with
val typography = Typography(
    body1 = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    )
)

private val DarkColorPalette = darkColors(
    primary = blueLighter,
    primaryVariant = blueDarker,
    secondary = tealLighter,
    background = grayDarker,
    surface = grayDarker
)

private val LightColorPalette = lightColors(
    primary = blueNeutral,
    primaryVariant = blueDarker,
    secondary = tealNeutral,
    secondaryVariant = tealDarker,
    surface = grayLighter
)

@Composable
fun FgaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    backgroundColor: Color = MaterialTheme.colors.background,
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
        colors = colors,
        typography = typography,
        shapes = shapes
    ) {
        Surface(
            modifier = Modifier.background(color = backgroundColor)
        ) {
            content()
        }
    }
}