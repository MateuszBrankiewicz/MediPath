package com.medipath.modules.patient.search.ui

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.medipath.R
import com.medipath.core.models.SearchResult
import com.medipath.core.theme.LocalCustomColors
import com.medipath.modules.patient.search.SearchViewModel
import com.medipath.modules.shared.components.rememberBase64Image


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstitutionSearchResultsScreen(
    searchQuery: String,
    city: String,
    specialisation: String,
    onBackClick: () -> Unit = {},
    onInstitutionClick: (SearchResult) -> Unit = {}
) {
    val viewModel: SearchViewModel = viewModel()
    val searchResults by viewModel.searchResults.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var selectedSortOption by remember { mutableStateOf(SortOption.DEFAULT) }
    var isDropdownExpanded by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val error by viewModel.error.collectAsState()

    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.search(searchQuery, "institution", city, specialisation)
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
                        text = stringResource(R.string.institution_results),
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
                    if (specialisation.isNotBlank()) add(
                        stringResource(
                            R.string.services_label,
                            specialisation
                        )
                    )
                }.joinToString(" • ")

                if (searchCriteria.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Search,
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
                                    imageVector = Icons.Default.Business,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = stringResource(R.string.no_institutions_found),
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
                                val institution = sortedResults[index]
                                InstitutionCard(
                                    institution = institution,
                                    onClick = { onInstitutionClick(institution) }
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
fun InstitutionCard(
    institution: SearchResult,
    onClick: () -> Unit
) {
    val colors = LocalCustomColors.current
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.Top
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
                    val imageBitmap = rememberBase64Image(institution.image)
                    if (imageBitmap != null) {
                        Image(
                            bitmap = imageBitmap,
                            contentDescription = stringResource(R.string.institution_photo),
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(colors.blue900.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Business,
                                contentDescription = null,
                                modifier = Modifier.size(36.dp),
                                tint = colors.blue900
                            )
                        }
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = institution.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (institution.isPublic == true)
                            colors.green800.copy(alpha = 0.1f)
                        else
                            colors.blue800.copy(alpha = 0.1f),
                        modifier = Modifier.padding(top = 6.dp)
                    ) {
                        Text(
                            text = if (institution.isPublic == true) stringResource(R.string.public_institution) else stringResource(R.string.private_institution),
                            fontSize = 12.sp,
                            color = if (institution.isPublic == true) colors.green800 else colors.blue800,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    if (!institution.types.isNullOrEmpty()) {
                        Text(
                            text = institution.types.joinToString(", "),
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            modifier = Modifier.padding(top = 4.dp),
                            lineHeight = 16.sp
                        )
                    }
                }

                StarRating(institution.rating, institution.numOfRatings)
            }

            if (!institution.address.isNullOrEmpty()) {
                HorizontalDivider(
                    Modifier, DividerDefaults.Thickness, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                )

                val addressParts = institution.address.split(",").map { it.trim() }
                val province = addressParts.getOrNull(0)?.takeIf { it.isNotBlank() }
                val city = addressParts.getOrNull(1)?.takeIf { it.isNotBlank() }
                val street = addressParts.getOrNull(2)?.takeIf { it.isNotBlank() && it != "null" }
                val number = addressParts.getOrNull(3)?.takeIf { it.isNotBlank() && it != "null" }
                val postalCode = addressParts.getOrNull(4)?.takeIf { it.isNotBlank() }

                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = stringResource(R.string.location),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(18.dp)
                                .padding(top = 2.dp)
                        )
                        Column {
                            val line1 = listOfNotNull(province, city).joinToString(", ")

                            val streetAndNumber = listOfNotNull(street, number).joinToString(" ")
                            val line2 = listOfNotNull(streetAndNumber.takeIf { it.isNotBlank() }, postalCode).joinToString(", ")

                            if (line1.isNotEmpty()) {
                                Text(
                                    text = line1,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            if (line2.isNotEmpty()) {
                                Text(
                                    text = line2,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }

                            if (line1.isEmpty() && line2.isEmpty()) {
                                Text(
                                    text = institution.address.replace(",", ", "),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

            }
        }
    }
}