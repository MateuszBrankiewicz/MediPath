package com.medipath.modules.shared.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.medipath.R
import com.medipath.core.theme.LocalCustomColors

data class FilterOption(
    val key: String,
    val label: String
)

data class FilterChipsConfig(
    val sortByOptions: List<FilterOption>,
    val sortOrderOptions: List<FilterOption>,
    val showClearButton: Boolean = true,
    var sortOrderLabel: String? = null
)

@Composable
fun GenericFilterChipsSection(
    sortBy: String,
    sortOrder: String,
    onSortByChange: (String) -> Unit,
    onSortOrderChange: (String) -> Unit,
    onClearFilters: () -> Unit,
    config: FilterChipsConfig,
    modifier: Modifier = Modifier
) {
    val colors = LocalCustomColors.current

    if(config.sortOrderLabel.isNullOrEmpty()) {
        config.sortOrderLabel = stringResource(R.string.order)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.filters),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
            )

            Text(
                stringResource(R.string.sort_by),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(2.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                config.sortByOptions.forEach { option ->
                    FilterChip(
                        selected = sortBy == option.key,
                        onClick = { onSortByChange(option.key) },
                        label = { Text(option.label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = colors.blue900,
                            selectedLabelColor = MaterialTheme.colorScheme.background
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                config.sortOrderLabel!!,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(2.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                config.sortOrderOptions.forEach { order ->
                    FilterChip(
                        selected = sortOrder == order.key,
                        onClick = { onSortOrderChange(order.key) },
                        label = { Text(order.label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = colors.blue900,
                            selectedLabelColor = MaterialTheme.colorScheme.background
                        )
                    )
                }
            }

            if (config.showClearButton) {
                Button(
                    onClick = onClearFilters,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.blue900
                    ),
                    shape = RoundedCornerShape(30.dp)
                ) {
                    Text(stringResource(R.string.clear_filters))
                }
            }
        }
    }
}
