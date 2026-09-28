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
    primary = OceanPrimaryDark,
    onPrimary = OceanOnPrimaryDark,
    primaryContainer = OceanPrimaryContainerDark,
    onPrimaryContainer = OceanOnPrimaryContainerDark,
    secondary = CoralSecondaryDark,
    onSecondary = CoralOnSecondaryDark,
    secondaryContainer = CoralSecondaryContainerDark,
    onSecondaryContainer = CoralOnSecondaryContainerDark,
    tertiary = PalmTertiaryDark,
    onTertiary = PalmOnTertiaryDark,
    tertiaryContainer = PalmTertiaryContainerDark,
    onTertiaryContainer = PalmOnTertiaryContainerDark,
    background = SurfaceDark,
    surface = SurfaceContainerDark
)

private val LightColorScheme = lightColorScheme(
    primary = OceanPrimaryLight,
    onPrimary = OceanOnPrimaryLight,
    primaryContainer = OceanPrimaryContainerLight,
    onPrimaryContainer = OceanOnPrimaryContainerLight,
    secondary = CoralSecondaryLight,
    onSecondary = CoralOnSecondaryLight,
    secondaryContainer = CoralSecondaryContainerLight,
    onSecondaryContainer = CoralOnSecondaryContainerLight,
    tertiary = PalmTertiaryLight,
    onTertiary = PalmOnTertiaryLight,
    tertiaryContainer = PalmTertiaryContainerLight,
    onTertiaryContainer = PalmOnTertiaryContainerLight,
    background = SurfaceLight,
    surface = SurfaceContainerLight
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Set to false so custom holiday vibrant branding shines
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
