package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = ElectricBluePrimary,
    onPrimary = SurfaceWhite,
    primaryContainer = RoyalNavySurface,
    onPrimaryContainer = SurfaceWhite,
    secondary = LuxuryGold,
    onSecondary = RoyalNavyDark,
    secondaryContainer = LuxuryGoldContainer,
    onSecondaryContainer = TextCharcoal,
    tertiary = ElectricBlueSecondary,
    onTertiary = SurfaceWhite,
    background = RoyalNavyDark,
    onBackground = SurfaceWhite,
    surface = RoyalNavyPrimary,
    onSurface = SurfaceWhite,
    surfaceVariant = RoyalNavySurface,
    onSurfaceVariant = TextSubtle,
    outline = RoyalNavySurface,
    error = ErrorRed,
    onError = SurfaceWhite
)

private val LightColorScheme = lightColorScheme(
    primary = RoyalNavyPrimary,
    onPrimary = SurfaceWhite,
    primaryContainer = ElectricBlueLight,
    onPrimaryContainer = RoyalNavyPrimary,
    secondary = ElectricBluePrimary,
    onSecondary = SurfaceWhite,
    secondaryContainer = LuxuryGoldContainer,
    onSecondaryContainer = TextCharcoal,
    tertiary = LuxuryGold,
    onTertiary = TextCharcoal,
    background = BackgroundWarm,
    onBackground = TextCharcoal,
    surface = SurfaceWhite,
    onSurface = TextCharcoal,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = TextMedium,
    outline = SurfaceBorder,
    error = ErrorRed,
    onError = SurfaceWhite
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use our bespoke brand colors by default
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

