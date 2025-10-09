package com.medipath.core.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class CustomColors(
    val blue900: Color,
    val blue800: Color,
    val blue400: Color,
    val blue300: Color,
    val purple800: Color,
    val purple300: Color,
    val orange800: Color,
    val orange300: Color,
    val green800: Color,
    val green300: Color,
    val red800: Color
)

val LocalCustomColors = staticCompositionLocalOf {
    CustomColors(
        blue900 = Color.Unspecified,
        blue800 = Color.Unspecified,
        blue400 = Color.Unspecified,
        blue300 = Color.Unspecified,
        purple800 = Color.Unspecified,
        purple300 = Color.Unspecified,
        orange800 = Color.Unspecified,
        orange300 = Color.Unspecified,
        green800 = Color.Unspecified,
        green300 = Color.Unspecified,
        red800 = Color.Unspecified
    )
}