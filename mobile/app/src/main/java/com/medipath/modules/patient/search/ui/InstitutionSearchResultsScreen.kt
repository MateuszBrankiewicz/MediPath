package com.medipath.modules.patient.search.ui

import android.util.Log
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
import androidx.compose.material.icons.filled.Star
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
import com.medipath.core.models.SearchResult
import com.medipath.core.theme.LocalCustomColors
import com.medipath.modules.patient.search.SearchViewModel
import com.medipath.modules.shared.components.rememberBase64Image

enum class InstitutionSortOption(val displayName: String) {
    DEFAULT("Default"),
    RATING_DESC("Rating (Descending)"),
    RATING_ASC("Rating (Ascending)"),
    NUM_RATINGS_DESC("Reviews (Descending)"),
    NUM_RATINGS_ASC("Reviews (Ascending)")
}

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

    var selectedSortOption by remember { mutableStateOf(InstitutionSortOption.DEFAULT) }
    var isDropdownExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.search(searchQuery, "institution", city, specialisation)
    }

    val sortedResults = remember(searchResults, selectedSortOption) {
        when (selectedSortOption) {
            InstitutionSortOption.DEFAULT -> searchResults
            InstitutionSortOption.RATING_DESC -> searchResults.sortedByDescending { it.rating }
            InstitutionSortOption.RATING_ASC -> searchResults.sortedBy { it.rating }
            InstitutionSortOption.NUM_RATINGS_DESC -> searchResults.sortedByDescending { it.numOfRatings }
            InstitutionSortOption.NUM_RATINGS_ASC -> searchResults.sortedBy { it.numOfRatings }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(WindowInsets.navigationBars.asPaddingValues())
            .background(MaterialTheme.colorScheme.secondary)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(LocalCustomColors.current.blue900)
                .padding(top = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Return",
                    tint = MaterialTheme.colorScheme.background
                )
            }
            Text(
                text = "Institution Results",
                fontSize = 23.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.background,
                modifier = Modifier.padding(start = 8.dp).padding(vertical = 24.dp)
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
                if (specialisation.isNotBlank()) add("Services: $specialisation")
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

                SortHeader(
                    count = searchResults.size,
                    selectedDisplayName = selectedSortOption.displayName,
                    isExpanded = isDropdownExpanded,
                    onExpandedChange = { isDropdownExpanded = it },
                    options = InstitutionSortOption.entries.map { it.displayName },
                    onOptionSelected = { selected ->
                        InstitutionSortOption.entries.firstOrNull { it.displayName == selected }?.let {
                            selectedSortOption = it
                        }
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
                                text = "No institutions found",
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
                            contentDescription = "Institution photo",
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
                            text = if (institution.isPublic == true) "Public Institution" else "Private Institution",
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
                            contentDescription = "Location",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp).padding(top = 2.dp)
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