package com.medipath.ui.search

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.medipath.ui.theme.MediPathTheme

class SearchResultsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val searchQuery = intent.getStringExtra("search_query") ?: ""
        val searchType = intent.getStringExtra("search_type") ?: "doctor"
        val searchCity = intent.getStringExtra("search_city") ?: ""
        val searchSpecialisations = intent.getStringExtra("search_specialisation") ?: ""

        Log.d("SearchResultsActivity", "Received search parameters: query='$searchQuery', type='$searchType', city='$searchCity', specialisations='$searchSpecialisations'")
        setContent {
            MediPathTheme {
                when (searchType) {
                    "doctor" -> DoctorSearchResultsScreen(
                        searchQuery = searchQuery,
                        city = searchCity,
                        specialisation = searchSpecialisations,
                        onBackClick = { finish() }
                    )
                    "institution" -> InstitutionSearchResultsScreen(
                        searchQuery = searchQuery,
                        city = searchCity,
                        specialisation = searchSpecialisations,
                        onBackClick = { finish() }
                    )
                }
            }
        }
    }
}