package com.medipath.core.models

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class NavTab(
    val name: String,
    val label: String,
    val icon: ImageVector,
    val iconTint: Color
)