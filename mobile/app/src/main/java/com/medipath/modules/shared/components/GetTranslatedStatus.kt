package com.medipath.modules.shared.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.medipath.R

@Composable
fun getTranslatedStatus(status: String): String {
    return when (status.lowercase()) {
        "upcoming", "scheduled" -> stringResource(R.string.upcoming)
        "completed" -> stringResource(R.string.completed)
        "cancelled" -> stringResource(R.string.cancelled)
        else -> status
    }
}