package com.medipath.ui.theme

import androidx.compose.material.darkColors
import androidx.compose.ui.graphics.Color

sealed class ThemeColors(
    val primary: Color, // primary color for app bars, buttons
    val disabledBtn: Color, // color for disabled buttons
    val background: Color, // background color for screens
    val subtitle: Color, // color for subtitles and secondary text
    val inputBgPrimary: Color, // background color for input fields
    val placeholder: Color, // color for placeholder text in input fields
    val error: Color, // color for error messages
    val secondBackground: Color,
    val darkBlue: Color,
    val darkBlueIcon: Color,
    val lightBlue: Color,
    val lightBlueIcon: Color,
    val lightGray: Color,
    val lightGrayIcon: Color
) {
    object Light : ThemeColors(
        primary = Color(0xFF000000),
        disabledBtn =
            Color.Gray,
        background = Color(0xFFFFFFFF),
        subtitle = Color(0xFF284662),
        inputBgPrimary = Color(0xFFD9D9D9),
        placeholder = Color(0xFF5D5D5D),
        error = Color.Red,
        secondBackground = Color(0xFFE8E8E8),
        darkBlue = Color(0xFF2D4A69),
        darkBlueIcon = Color(0xFF3E6187),
        lightBlue = Color(0xFF88A8C9),
        lightBlueIcon = Color(0xFF9DB9D5),
        lightGray = Color(0xFFBABABA),
        lightGrayIcon = Color(0xFFD2D2D2),
    )

    object Dark : ThemeColors(
        primary = Color(0xFFFFFFFF),
        background = Color(0xFF000000),
        subtitle = Color(0xFF000000),
        disabledBtn = Color(0xFF000000),
        inputBgPrimary = Color(0xFF000000),
        placeholder = Color(0xFF000000),
        error = Color(0xFF000000),
        secondBackground = Color(0xFF000000),
        darkBlue = Color(0xFF000000),
        darkBlueIcon = Color(0xFF000000),
        lightBlue = Color(0xFF000000),
        lightBlueIcon = Color(0xFF000000),
        lightGray = Color(0xFF000000),
        lightGrayIcon = Color(0xFF000000),
    )
}