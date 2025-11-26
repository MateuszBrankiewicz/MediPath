package com.medipath.modules.shared.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.medipath.R
import com.medipath.core.theme.LocalCustomColors

@Composable
fun GenericFilterToggleRow(
    totalItems: Int,
    showingItems: Int,
    showFilters: Boolean,
    onToggleFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalCustomColors.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.total_showing, totalItems, showingItems),
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        IconButton(
            onClick = onToggleFilters
        ) {
            Icon(
                Icons.Outlined.FilterList,
                contentDescription = stringResource(R.string.toggle_filters),
                tint = if (showFilters) colors.blue900 else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
