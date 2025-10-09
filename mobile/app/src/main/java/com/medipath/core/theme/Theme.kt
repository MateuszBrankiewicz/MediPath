package com.medipath.core.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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

private val CustomLightColorScheme = CustomColors(
    blue900 = Color(0xFF2D4A69),
    blue800 = Color(0xFF0277BD),
    blue400 = Color(0xFF88A8C9),
    blue300 = Color(0xFF4FC3F7),
    purple800 = Color(0xFF6A1B9A),
    purple300 = Color(0xFF9C4DCC),
    orange800 = Color(0xFFE64A19),
    orange300 = Color(0xFFFF8A65),
    green800 = Color(0xFF2E7D32),
    green300 = Color(0xFF66BB6A),
    red800 = Color(0xFFDE2E2E)
)

private val CustomDarkColorScheme = CustomColors(
    blue900 = Color(0xFF2D4A69),
    blue800 = Color(0xFF0277BD),
    blue400 = Color(0xFF88A8C9),
    blue300 = Color(0xFF4FC3F7),
    purple800 = Color(0xFF6A1B9A),
    purple300 = Color(0xFF9C4DCC),
    orange800 = Color(0xFFE64A19),
    orange300 = Color(0xFFFF8A65),
    green800 = Color(0xFF2E7D32),
    green300 = Color(0xFF66BB6A),
    red800 = Color(0xFFDE2E2E)
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

    val customColors = if (darkTheme) CustomDarkColorScheme else CustomLightColorScheme

    CompositionLocalProvider(LocalCustomColors provides customColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}