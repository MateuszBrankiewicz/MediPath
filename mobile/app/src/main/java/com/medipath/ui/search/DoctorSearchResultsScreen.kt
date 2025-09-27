package com.medipath.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.medipath.data.api.SearchResult
import com.medipath.viewmodels.SearchViewModel
import android.content.Intent
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import com.medipath.ui.booking.AppointmentBookingActivity
import com.medipath.ui.theme.LocalCustomColors

enum class SortOption(val displayName: String) {
    DEFAULT("Default"),
    RATING_DESC("Rating (high to low)"),
    RATING_ASC("Rating (low to high)"),
    NUM_RATINGS_DESC("Reviews (most to least)"),
    NUM_RATINGS_ASC("Reviews (least to most)")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorSearchResultsScreen(
    searchQuery: String,
    city: String,
    specialisation: String,
    onBackClick: () -> Unit = {}
) {
    val viewModel: SearchViewModel = viewModel()
    val searchResults by viewModel.searchResults.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val context = LocalContext.current

    var selectedSortOption by remember { mutableStateOf(SortOption.DEFAULT) }
    var isDropdownExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.search(searchQuery, "doctor", city, specialisation)
    }

    val sortedResults = remember(searchResults, selectedSortOption) {
        when (selectedSortOption) {
            SortOption.DEFAULT -> searchResults
            SortOption.RATING_DESC -> searchResults.sortedByDescending { it.rating }
            SortOption.RATING_ASC -> searchResults.sortedBy { it.rating }
            SortOption.NUM_RATINGS_DESC -> searchResults.sortedByDescending { it.numOfRatings }
            SortOption.NUM_RATINGS_ASC -> searchResults.sortedBy { it.numOfRatings }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.secondary)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(LocalCustomColors.current.blue900),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.background
                )
            }
            Text(
                text = "Search Results",
                fontSize = 23.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.background,
                modifier = Modifier.padding(vertical = 30.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            val searchCriteria = buildList {
                if (searchQuery.isNotBlank()) add("\"$searchQuery\"")
                if (city.isNotBlank()) add("City: $city")
                if (specialisation.isNotBlank()) add("Specialisation: $specialisation")
            }.joinToString(" • ")

            if (searchCriteria.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.PersonSearch,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Searching for: $searchCriteria",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            if (!isLoading && searchResults.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Sort by:",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = " (${searchResults.size} results)",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }

                        ExposedDropdownMenuBox(
                            expanded = isDropdownExpanded,
                            onExpandedChange = { isDropdownExpanded = !isDropdownExpanded }
                        ) {
                            OutlinedTextField(
                                value = selectedSortOption.displayName,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = {
                                    Icon(
                                        Icons.Default.ArrowDropDown,
                                        contentDescription = "Dropdown",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                modifier = Modifier
                                    .width(180.dp)
                                    .menuAnchor(),
                                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )

                            ExposedDropdownMenu(
                                expanded = isDropdownExpanded,
                                onDismissRequest = { isDropdownExpanded = false }
                            ) {
                                SortOption.entries.forEach { option ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = option.displayName,
                                                fontSize = 12.sp
                                            )
                                        },
                                        onClick = {
                                            selectedSortOption = option
                                            isDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }

                searchResults.isEmpty() -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.PersonSearch,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No doctors found",
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Try adjusting your search criteria",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(sortedResults) { doctor ->
                            DoctorCard(
                                doctor = doctor,
                                onClick = {
                                    val intent = Intent(context, AppointmentBookingActivity::class.java)
                                    intent.putExtra("doctor_id", doctor.id)
                                    intent.putExtra("doctor_name", "${doctor.name} ${doctor.surname}")
                                    context.startActivity(intent)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DoctorCard(
    doctor: SearchResult,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${doctor.name} ${doctor.surname}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                    Text(
                        text = doctor.specialisations?.joinToString(", ") ?: "",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = "Rating",
                        tint = Color(0xFFFFB000),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "${doctor.rating} (${doctor.numOfRatings})",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            doctor.addresses?.forEach { address ->
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = "Location",
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "${address.first} • ${address.second}",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}