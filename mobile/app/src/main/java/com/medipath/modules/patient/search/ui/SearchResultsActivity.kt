package com.medipath.modules.patient.search.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.medipath.core.theme.MediPathTheme

class SearchResultsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val searchQuery = intent.getStringExtra("search_query") ?: ""
        val searchType = intent.getStringExtra("search_type") ?: "doctor"
        val searchCity = intent.getStringExtra("search_city") ?: ""
        val searchSpecialisations = intent.getStringExtra("search_specialisation") ?: ""

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
                        onBackClick = { finish() },
                        onInstitutionClick = { institution ->
                            val intent = Intent(this, InstitutionDetailsActivity::class.java)
                            intent.putExtra("institution_id", institution.id)
                            startActivity(intent)
                        }
                    )
                }
            }
        }
    }
}