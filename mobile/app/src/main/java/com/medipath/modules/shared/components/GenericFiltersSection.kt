package com.medipath.modules.shared.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.medipath.R

data class FilterConfig(
    val statusOptions: List<FilterOption>,
    val sortByOptions: List<FilterOption>,
    val sortOrderOptions: List<FilterOption> = emptyList(),
    val showSortOrder: Boolean = true
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenericFiltersSection(
    statusFilter: String,
    dateFromFilter: String,
    dateToFilter: String,
    sortBy: String,
    sortOrder: String = "Ascending",
    onStatusFilterChange: (String) -> Unit,
    onDateFromChange: (String) -> Unit,
    onDateToChange: (String) -> Unit,
    onSortByChange: (String) -> Unit,
    onSortOrderChange: (String) -> Unit = {},
    filterConfig: FilterConfig,
    modifier: Modifier = Modifier
) {
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
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(
                text = stringResource(R.string.filters),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            var statusExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = statusExpanded,
                onExpandedChange = { statusExpanded = it }
            ) {
                val currentStatusLabel = filterConfig.statusOptions
                    .find { it.key == statusFilter }?.label ?: statusFilter

                OutlinedTextField(
                    value = currentStatusLabel,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.status), fontSize = 12.sp) },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(8.dp)
                )
                ExposedDropdownMenu(
                    expanded = statusExpanded,
                    onDismissRequest = { statusExpanded = false },
                    containerColor = MaterialTheme.colorScheme.background
                ) {
                    filterConfig.statusOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.label, color = MaterialTheme.colorScheme.onSurface) },
                            onClick = {
                                onStatusFilterChange(option.key)
                                statusExpanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = dateFromFilter,
                onValueChange = onDateFromChange,
                label = { Text(stringResource(R.string.date_from), fontSize = 12.sp) },
                placeholder = { Text(stringResource(R.string.yyyy_mm_dd), fontSize = 12.sp) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                singleLine = true
            )

            OutlinedTextField(
                value = dateToFilter,
                onValueChange = onDateToChange,
                label = { Text(stringResource(R.string.date_to), fontSize = 12.sp) },
                placeholder = { Text(stringResource(R.string.yyyy_mm_dd), fontSize = 12.sp) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                singleLine = true
            )

            var sortExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = sortExpanded,
                onExpandedChange = { sortExpanded = it }
            ) {
                val currentSortByLabel = filterConfig.sortByOptions
                    .find { it.key == sortBy }?.label ?: sortBy

                OutlinedTextField(
                    value = currentSortByLabel,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.sort_by), fontSize = 12.sp) },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = sortExpanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(8.dp)
                )
                ExposedDropdownMenu(
                    expanded = sortExpanded,
                    onDismissRequest = { sortExpanded = false },
                    containerColor = MaterialTheme.colorScheme.background
                ) {
                    filterConfig.sortByOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.label, color = MaterialTheme.colorScheme.onSurface) },
                            onClick = {
                                onSortByChange(option.key)
                                sortExpanded = false
                            }
                        )
                    }
                }
            }

            if (filterConfig.showSortOrder) {
                var orderExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = orderExpanded,
                    onExpandedChange = { orderExpanded = it }
                ) {
                    val currentOrderLabel = filterConfig.sortOrderOptions
                        .find { it.key == sortOrder }?.label ?: sortOrder

                    OutlinedTextField(
                        value = currentOrderLabel,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.order), fontSize = 12.sp) },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = orderExpanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(8.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = orderExpanded,
                        onDismissRequest = { orderExpanded = false },
                        containerColor = MaterialTheme.colorScheme.background
                    ) {
                        filterConfig.sortOrderOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.label, color = MaterialTheme.colorScheme.onSurface) },
                                onClick = {
                                    onSortOrderChange(option.key)
                                    orderExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
