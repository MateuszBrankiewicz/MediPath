package com.medipath.core.theme

import androidx.compose.ui.graphics.Color

sealed class ThemeColors(
    val primary: Color,
    val disabledBtn: Color,
    val background: Color,
    val subtitle: Color,
    val inputBgPrimary: Color,
    val placeholder: Color,
    val error: Color,
    val secondBackground: Color,
) {
    object Light : ThemeColors(
        primary = Color(0xFF000000),
        disabledBtn = Color(0xFF9E9E9E),
        background = Color(0xFFFFFFFF),
        subtitle = Color(0xFF284662),
        inputBgPrimary = Color(0xFFD9D9D9),
        placeholder = Color(0xFF5D5D5D),
        error = Color(0xFFDE2E2E),
        secondBackground = Color(0xFFE8E8E8)
    )

    object Dark : ThemeColors(
        primary = Color(0xFFFFFFFF),
        background = Color(0xFF000000),
        subtitle = Color(0xFF000000),
        disabledBtn = Color(0xFF000000),
        inputBgPrimary = Color(0xFF000000),
        placeholder = Color(0xFF000000),
        error = Color(0xFF000000),
        secondBackground = Color(0xFF000000)
    )
}