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

private val SleekDarkColorScheme = darkColorScheme(
    primary = SleekDarkPrimary,
    onPrimary = SleekDarkOnPrimary,
    primaryContainer = SleekDarkPrimaryContainer,
    onPrimaryContainer = SleekDarkOnPrimaryContainer,
    secondary = SleekDarkSecondary,
    onSecondary = SleekDarkOnSecondary,
    secondaryContainer = SleekDarkSecondaryContainer,
    onSecondaryContainer = SleekDarkOnSecondaryContainer,
    tertiary = SleekDarkTertiary,
    onTertiary = SleekDarkOnTertiary,
    tertiaryContainer = SleekDarkTertiaryContainer,
    onTertiaryContainer = SleekDarkOnTertiaryContainer,
    background = SleekDarkBackground,
    onBackground = SleekDarkOnBackground,
    surface = SleekDarkSurface,
    onSurface = SleekDarkOnSurface,
    surfaceVariant = SleekDarkSurfaceVariant,
    onSurfaceVariant = SleekDarkOnSurfaceVariant,
    outline = SleekDarkOutline,
    outlineVariant = SleekDarkOutlineVariant
)

private val SleekLightColorScheme = lightColorScheme(
    primary = SleekLightPrimary,
    onPrimary = SleekLightOnPrimary,
    primaryContainer = SleekLightPrimaryContainer,
    onPrimaryContainer = SleekLightOnPrimaryContainer,
    secondary = SleekLightSecondary,
    onSecondary = SleekLightOnSecondary,
    secondaryContainer = SleekLightSecondaryContainer,
    onSecondaryContainer = SleekLightOnSecondaryContainer,
    tertiary = SleekLightTertiary,
    onTertiary = SleekLightOnTertiary,
    tertiaryContainer = SleekLightTertiaryContainer,
    onTertiaryContainer = SleekLightOnTertiaryContainer,
    background = SleekLightBackground,
    onBackground = SleekLightOnBackground,
    surface = SleekLightSurface,
    onSurface = SleekLightOnSurface,
    surfaceVariant = SleekLightSurfaceVariant,
    onSurfaceVariant = SleekLightOnSurfaceVariant,
    outline = SleekLightOutline,
    outlineVariant = SleekLightOutlineVariant
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> SleekDarkColorScheme
        else -> SleekLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

