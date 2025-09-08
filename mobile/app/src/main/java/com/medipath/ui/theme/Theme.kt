package com.medipath.ui.theme

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

private val DarkColorScheme = darkColorScheme(
    primary = ThemeColors.Dark.primary,
    onSurfaceVariant = ThemeColors.Dark.disabledBtn,
    background = ThemeColors.Dark.background,
    onBackground = ThemeColors.Dark.subtitle,
    surface = ThemeColors.Dark.inputBgPrimary,
    onSurface = ThemeColors.Dark.placeholder,
    error = ThemeColors.Dark.error,
    secondary = ThemeColors.Dark.secondBackground,
//    darkBlue = Color(0xFF2D4A69),
//    darkBlueIcon = Color(0xFF3E6187),
//    lightBlue = Color(0xFF88A8C9),
//    lightBlueIcon = Color(0xFF9DB9D5),
//    lightGray = Color(0xFFBABABA),
//    lightGrayIcon = Color(0xFFD2D2D2),

)

private val LightColorScheme = lightColorScheme(
    primary = ThemeColors.Light.primary,
    onSurfaceVariant = ThemeColors.Light.disabledBtn,
    background = ThemeColors.Light.background,
    onBackground = ThemeColors.Light.subtitle,
    surface = ThemeColors.Light.inputBgPrimary,
    onSurface = ThemeColors.Light.placeholder,
    error = ThemeColors.Light.error,
    secondary = ThemeColors.Light.secondBackground
)

@Composable
fun MediPathTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
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