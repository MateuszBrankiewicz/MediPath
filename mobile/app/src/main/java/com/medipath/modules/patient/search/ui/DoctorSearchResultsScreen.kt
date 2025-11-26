package com.medipath.modules.patient.search.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.res.stringResource
import com.medipath.core.models.SearchResult
import com.medipath.core.theme.LocalCustomColors
import com.medipath.modules.patient.booking.ui.AppointmentBookingActivity
import com.medipath.modules.patient.search.SearchViewModel
import com.medipath.modules.shared.components.rememberBase64Image
import com.medipath.R

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

    val error by viewModel.error.collectAsState()

    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.doctor_results),
                        color = MaterialTheme.colorScheme.background,
                        fontSize = 23.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            tint = MaterialTheme.colorScheme.background
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = LocalCustomColors.current.blue900
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.secondary)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
            Spacer(modifier = Modifier.height(16.dp))

            val searchCriteria = buildList {
                if (searchQuery.isNotBlank()) add("\"$searchQuery\"")
                if (city.isNotBlank()) add(stringResource(R.string.city_label, city))
                if (specialisation.isNotBlank()) add(stringResource(R.string.specialisation_optional) + ": $specialisation")
            }.joinToString(" • ")

            if (searchCriteria.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                    shape = RoundedCornerShape(30.dp)) {
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
                            text = stringResource(R.string.searching_for, searchCriteria),
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            if (!isLoading && searchResults.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))

                val optionsMap = SortOption.entries.associateWith { stringResource(it.labelId) }

                SortHeader(
                    count = searchResults.size,
                    selectedDisplayName = stringResource(selectedSortOption.labelId),
                    isExpanded = isDropdownExpanded,
                    onExpandedChange = { isDropdownExpanded = it },
                    options = optionsMap.values.toList(),
                    onOptionSelected = { selectedLabel ->
                        val option = optionsMap.entries.find { it.value == selectedLabel }?.key
                        if (option != null) selectedSortOption = option
                    }
                )
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
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                        shape = RoundedCornerShape(12.dp)
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
                                text = stringResource(R.string.no_doctors_found),
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.try_adjusting_criteria),
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
                        items(sortedResults.size) { index ->
                            val doctor = sortedResults[index]
                            DoctorCard(
                                doctor = doctor,
                                onClick = {
                                    val intent = Intent(context, AppointmentBookingActivity::class.java)
                                    intent.putExtra("doctor_id", doctor.id)
                                    intent.putExtra("doctor_name", "${doctor.name} ${doctor.surname}")
                                    intent.putExtra("doctor_image", doctor.image)
                                    intent.putExtra("doctor_rating", doctor.rating)
                                    intent.putExtra("num_of_ratings", doctor.numOfRatings)
                                    intent.putExtra("specialisations", doctor.specialisations?.joinToString(", ") ?: "")
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
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                        shape = CircleShape
                    )
            ) {
                val imageBitmap = rememberBase64Image(doctor.image)
                if (imageBitmap != null) {
                    Image(
                        bitmap = imageBitmap,
                        contentDescription = stringResource(R.string.doctor_photo),
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(LocalCustomColors.current.blue900.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = LocalCustomColors.current.blue900
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
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
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        if (!doctor.specialisations.isNullOrEmpty()) {
                            Text(
                                text = doctor.specialisations.joinToString(", "),
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                lineHeight = 16.sp
                            )
                        }
                    }

                    StarRating(doctor.rating, doctor.numOfRatings)
                }

                if (!doctor.addresses.isNullOrEmpty()) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        thickness = DividerDefaults.Thickness, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                    )

                    doctor.addresses.take(2).forEach { address ->
                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = stringResource(R.string.location),
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .size(16.dp)
                                    .padding(top = 2.dp)
                            )
                            Column {
                                Text(
                                    text = address.first.institutionName,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = address.second.replace(",", ", "),
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }

                    if (doctor.addresses.size > 2) {
                        Text(
                            text = stringResource(
                                R.string.more_locations,
                                doctor.addresses.size - 2
                            ),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(start = 20.dp)
                        )
                    }
                }
            }
        }
    }
}