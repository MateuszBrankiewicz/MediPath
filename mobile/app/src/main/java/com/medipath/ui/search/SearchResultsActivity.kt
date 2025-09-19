package com.medipath.ui.search

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.medipath.ui.theme.MediPathTheme

class SearchResultsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val searchQuery = intent.getStringExtra("search_query") ?: ""
        val searchType = intent.getStringExtra("search_type") ?: "doctor"

        setContent {
            MediPathTheme {
                when (searchType) {
                    "doctor" -> DoctorSearchResultsScreen(
                        searchQuery = searchQuery,
                        onBackClick = { finish() }
                    )
                    "institution" -> InstitutionSearchResultsScreen(
                        searchQuery = searchQuery,
                        onBackClick = { finish() }
                    )
                }
            }
        }
    }
}