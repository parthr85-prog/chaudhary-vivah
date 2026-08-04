package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = RoyalMaroon,
    onPrimary = Color.White,
    primaryContainer = LightRoseContainer,
    onPrimaryContainer = DarkMaroon,
    secondary = RoyalGold,
    onSecondary = Color.White,
    secondaryContainer = SoftGold,
    onSecondaryContainer = DeepGold,
    tertiary = WarmSaffron,
    onTertiary = Color.White,
    background = CreamBackground,
    onBackground = TextPrimaryDark,
    surface = SurfaceCream,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = TextSecondaryDark,
    outline = BorderLightGold
)

@Composable
fun ChaudharyVivahTheme(
    darkTheme: Boolean = false, // Explicitly ignore dark theme to keep app theme light and uniform
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    // Always force LightColorScheme across the app even if device night mode is enabled
    val colorScheme = LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    ChaudharyVivahTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
}
