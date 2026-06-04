package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryAmber,
    onPrimary = ObsidianBlack,
    secondary = SecondaryAmber,
    onSecondary = ObsidianBlack,
    tertiary = VintageGold,
    onTertiary = ObsidianBlack,
    background = ObsidianBlack,
    onBackground = TextWarmWhite,
    surface = DarkCharcoal,
    onSurface = TextWarmWhite,
    surfaceVariant = SlateMuted,
    onSurfaceVariant = TextSoftGray,
    error = AccentError,
    onError = TextWarmWhite
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force true dark mode by default
    dynamicColor: Boolean = false, // Set to false to preserve the premium amber aesthetic
    content: @Composable () -> Unit,
) {
    // We enforce our signature True Dark Theme palette
    val colorScheme = DarkColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
