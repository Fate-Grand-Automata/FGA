package io.github.fate_grand_automata.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val md_theme_light_primary = Color(0xFF006495)
val md_theme_light_onPrimary = Color(0xFFFFFFFF)
val md_theme_light_primaryContainer = Color(0xFFCBE6FF)
val md_theme_light_onPrimaryContainer = Color(0xFF001E30)
val md_theme_light_secondary = Color(0xFF006B5E)
val md_theme_light_onSecondary = Color(0xFFFFFFFF)
val md_theme_light_secondaryContainer = Color(0xFF76F8E1)
val md_theme_light_onSecondaryContainer = Color(0xFF00201B)
val md_theme_light_tertiary = Color(0xFF3A4ED8)
val md_theme_light_onTertiary = Color(0xFFFFFFFF)
val md_theme_light_tertiaryContainer = Color(0xFFDFE0FF)
val md_theme_light_onTertiaryContainer = Color(0xFF000C61)
val md_theme_light_error = Color(0xFFBA1A1A)
val md_theme_light_errorContainer = Color(0xFFFFDAD6)
val md_theme_light_onError = Color(0xFFFFFFFF)
val md_theme_light_onErrorContainer = Color(0xFF410002)
val md_theme_light_background = Color(0xFFF8FDFF)
val md_theme_light_onBackground = Color(0xFF001F25)
val md_theme_light_surface = Color(0xFFF8FDFF)
val md_theme_light_onSurface = Color(0xFF001F25)
val md_theme_light_surfaceVariant = Color(0xFFDEE3EA)
val md_theme_light_onSurfaceVariant = Color(0xFF41474D)
val md_theme_light_outline = Color(0xFF72787E)
val md_theme_light_inverseOnSurface = Color(0xFFD6F6FF)
val md_theme_light_inverseSurface = Color(0xFF00363F)
val md_theme_light_inversePrimary = Color(0xFF8FCDFF)
val md_theme_light_shadow = Color(0xFF000000)
val md_theme_light_surfaceTint = Color(0xFF006495)
val md_theme_light_outlineVariant = Color(0xFFC1C7CE)
val md_theme_light_scrim = Color(0xFF000000)

val md_theme_dark_primary = Color(0xFF8FCDFF)
val md_theme_dark_onPrimary = Color(0xFF003450)
val md_theme_dark_primaryContainer = Color(0xFF004B71)
val md_theme_dark_onPrimaryContainer = Color(0xFFCBE6FF)
val md_theme_dark_secondary = Color(0xFF56DBC5)
val md_theme_dark_onSecondary = Color(0xFF003730)
val md_theme_dark_secondaryContainer = Color(0xFF005046)
val md_theme_dark_onSecondaryContainer = Color(0xFF76F8E1)
val md_theme_dark_tertiary = Color(0xFFBCC2FF)
val md_theme_dark_onTertiary = Color(0xFF001999)
val md_theme_dark_tertiaryContainer = Color(0xFF1B31C0)
val md_theme_dark_onTertiaryContainer = Color(0xFFDFE0FF)
val md_theme_dark_error = Color(0xFFFFB4AB)
val md_theme_dark_errorContainer = Color(0xFF93000A)
val md_theme_dark_onError = Color(0xFF690005)
val md_theme_dark_onErrorContainer = Color(0xFFFFDAD6)
val md_theme_dark_background = Color(0xFF001F25)
val md_theme_dark_onBackground = Color(0xFFA6EEFF)
val md_theme_dark_surface = Color(0xFF001F25)
val md_theme_dark_onSurface = Color(0xFFA6EEFF)
val md_theme_dark_surfaceVariant = Color(0xFF41474D)
val md_theme_dark_onSurfaceVariant = Color(0xFFC1C7CE)
val md_theme_dark_outline = Color(0xFF8B9198)
val md_theme_dark_inverseOnSurface = Color(0xFF001F25)
val md_theme_dark_inverseSurface = Color(0xFFA6EEFF)
val md_theme_dark_inversePrimary = Color(0xFF006495)
val md_theme_dark_shadow = Color(0xFF000000)
val md_theme_dark_surfaceTint = Color(0xFF8FCDFF)
val md_theme_dark_outlineVariant = Color(0xFF41474D)
val md_theme_dark_scrim = Color(0xFF000000)

// Set of Material typography styles to start with
val typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    )
)

private val LightColors = lightColorScheme(
    primary = md_theme_light_primary,
    onPrimary = md_theme_light_onPrimary,
    primaryContainer = md_theme_light_primaryContainer,
    onPrimaryContainer = md_theme_light_onPrimaryContainer,
    secondary = md_theme_light_secondary,
    onSecondary = md_theme_light_onSecondary,
    secondaryContainer = md_theme_light_secondaryContainer,
    onSecondaryContainer = md_theme_light_onSecondaryContainer,
    tertiary = md_theme_light_tertiary,
    onTertiary = md_theme_light_onTertiary,
    tertiaryContainer = md_theme_light_tertiaryContainer,
    onTertiaryContainer = md_theme_light_onTertiaryContainer,
    error = md_theme_light_error,
    errorContainer = md_theme_light_errorContainer,
    onError = md_theme_light_onError,
    onErrorContainer = md_theme_light_onErrorContainer,
    background = md_theme_light_background,
    onBackground = md_theme_light_onBackground,
    surface = md_theme_light_surface,
    onSurface = md_theme_light_onSurface,
    surfaceVariant = md_theme_light_surfaceVariant,
    onSurfaceVariant = md_theme_light_onSurfaceVariant,
    outline = md_theme_light_outline,
    inverseOnSurface = md_theme_light_inverseOnSurface,
    inverseSurface = md_theme_light_inverseSurface,
    inversePrimary = md_theme_light_inversePrimary,
    surfaceTint = md_theme_light_surfaceTint,
    outlineVariant = md_theme_light_outlineVariant,
    scrim = md_theme_light_scrim,
)


private val DarkColors = darkColorScheme(
    primary = md_theme_dark_primary,
    onPrimary = md_theme_dark_onPrimary,
    primaryContainer = md_theme_dark_primaryContainer,
    onPrimaryContainer = md_theme_dark_onPrimaryContainer,
    secondary = md_theme_dark_secondary,
    onSecondary = md_theme_dark_onSecondary,
    secondaryContainer = md_theme_dark_secondaryContainer,
    onSecondaryContainer = md_theme_dark_onSecondaryContainer,
    tertiary = md_theme_dark_tertiary,
    onTertiary = md_theme_dark_onTertiary,
    tertiaryContainer = md_theme_dark_tertiaryContainer,
    onTertiaryContainer = md_theme_dark_onTertiaryContainer,
    error = md_theme_dark_error,
    errorContainer = md_theme_dark_errorContainer,
    onError = md_theme_dark_onError,
    onErrorContainer = md_theme_dark_onErrorContainer,
    background = md_theme_dark_background,
    onBackground = md_theme_dark_onBackground,
    surface = md_theme_dark_surface,
    onSurface = md_theme_dark_onSurface,
    surfaceVariant = md_theme_dark_surfaceVariant,
    onSurfaceVariant = md_theme_dark_onSurfaceVariant,
    outline = md_theme_dark_outline,
    inverseOnSurface = md_theme_dark_inverseOnSurface,
    inverseSurface = md_theme_dark_inverseSurface,
    inversePrimary = md_theme_dark_inversePrimary,
    surfaceTint = md_theme_dark_surfaceTint,
    outlineVariant = md_theme_dark_outlineVariant,
    scrim = md_theme_dark_scrim,
)

@Immutable
data class CustomFGAColors(
    val colorEnemyTarget: Color,
    val colorMasterSkill: Color,
    val colorStageChange: Color,

    val colorServant1: Color,
    val colorServant2: Color,
    val colorServant3: Color,

    val colorBusterWeak: Color,
    val colorBuster: Color,
    val colorBusterResist: Color,

    val colorArtsWeak: Color,
    val colorArts: Color,
    val colorArtsResist: Color,

    val colorQuickWeak: Color,
    val colorQuick: Color,
    val colorQuickResist: Color,
)

val LocalCustomColorsPalette = staticCompositionLocalOf {
    CustomFGAColors(
        colorEnemyTarget = Color.Unspecified,
        colorMasterSkill = Color.Unspecified,
        colorStageChange = Color.Unspecified,

        colorServant1 = Color.Unspecified,
        colorServant2 = Color.Unspecified,
        colorServant3 = Color.Unspecified,

        colorBusterWeak = Color.Unspecified,
        colorBuster = Color.Unspecified,
        colorBusterResist = Color.Unspecified,

        colorArtsWeak = Color.Unspecified,
        colorArts = Color.Unspecified,
        colorArtsResist = Color.Unspecified,

        colorQuickWeak = Color.Unspecified,
        colorQuick = Color.Unspecified,
        colorQuickResist = Color.Unspecified,
    )
}

val MaterialTheme.customFGAColors: CustomFGAColors
    @Composable
    @ReadOnlyComposable
    get() = LocalCustomColorsPalette.current

@Composable
fun FGAListItemColors() = ListItemDefaults.colors(
    containerColor = MaterialTheme.colorScheme.surfaceVariant
)

@Composable
fun FGATheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    background: Color = Color.Unspecified,
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) {
        DarkColors
    } else {
        LightColors
    }

    val onCustomFGAColorsPalette = CustomFGAColors(
        colorEnemyTarget = Color(0xFF2e7d32),
        colorMasterSkill = Color(0xFF006064),
        colorStageChange = Color(0xFF616161),

        colorServant1 = Color(0xFFc62828),
        colorServant2 = Color(0xFF0277bd),
        colorServant3 = Color(0xFFf57f17),

        colorBusterWeak = Color(0xFFc81f1f),
        colorBuster = Color(0xFFe64a19),
        colorBusterResist = Color(0xFFf57c00),

        colorArtsWeak = Color(0xFF0e4fb3),
        colorArts = Color(0xFF0277bd),
        colorArtsResist = Color(0xFF3498db),

        colorQuickWeak = Color(0xFF006755),
        colorQuick = Color(0xFF2e7d32),
        colorQuickResist = Color(0xFF7cb342),
    )

    CompositionLocalProvider(
        LocalCustomColorsPalette provides onCustomFGAColorsPalette
    ) {
        MaterialTheme(
            colorScheme = colors,
            typography = typography
        ) {
            Surface(
                color = if (background == Color.Unspecified) MaterialTheme.colorScheme.background else background
            ) {
                PreventRtl {
                    content()
                }
            }
        }
    }

}

@Composable
fun FgaScreen(
    content: @Composable BoxScope.() -> Unit
) {
    FGATheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
        ) {
            content()
        }
    }
}