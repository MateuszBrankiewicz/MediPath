package com.medipath.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Intent
import android.util.Log
import com.medipath.data.api.RetrofitInstance
import com.medipath.ui.search.SearchResultsActivity
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar() {
    var query by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("doctor") }
    var city by remember { mutableStateOf("") }
    var specialisation by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var showAdvanced by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val apiService = RetrofitInstance.api

    val types = listOf("doctor", "institution")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 5.dp)
    ) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = if (selectedType == "doctor") "Doctor" else "Institution",
                onValueChange = {},
                readOnly = true,
                label = { Text("Search type") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedTextColor = Color.Gray,
                    unfocusedTextColor = Color.Gray
                ),
                shape = RoundedCornerShape(25.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                types.forEach { type ->
                    DropdownMenuItem(
                        text = { Text(if (type == "doctor") "Doctor" else "Institution") },
                        onClick = {
                            selectedType = type
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("Search...") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedTextColor = Color.Gray,
                unfocusedTextColor = Color.Gray
            ),
            shape = RoundedCornerShape(25.dp),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        TextButton(
            onClick = { showAdvanced = !showAdvanced },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (showAdvanced) "Hide advanced options" else "Advanced options")
        }

        if (showAdvanced) {
            OutlinedTextField(
                value = city,
                onValueChange = { city = it },
                label = { Text("City (optional)") },
                placeholder = { Text("e.g. Lublin, Kraków") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedTextColor = Color.Gray,
                    unfocusedTextColor = Color.Gray
                ),
                shape = RoundedCornerShape(25.dp),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = specialisation,
                onValueChange = { specialisation = it },
                label = { Text("Specialisation (optional)") },
                placeholder = { Text("e.g. cardiologist,neurology") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedTextColor = Color.Gray,
                    unfocusedTextColor = Color.Gray
                ),
                shape = RoundedCornerShape(25.dp),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = {
                scope.launch {
                    Log.d("SearchBar", "Searching for $query in $selectedType, $city, $specialisation")
                    try {
                        Log.d("SearchBar", "Initiating search API call")
                        val response = apiService.search(
                            query = query.ifBlank { "" },
                            type = selectedType,
                            city = if (city.isBlank()) null else city,
                            specialisation = if (specialisation.isBlank()) null else specialisation
                        )
                        Log.d("SearchBar", "API call completed with response: $response")

                        if (response.isSuccessful) {
                            val intent = Intent(context, SearchResultsActivity::class.java)
                            intent.putExtra("search_query", query)
                            intent.putExtra("search_type", selectedType)
                            intent.putExtra("search_city", city)
                            intent.putExtra("search_specialisation", specialisation)
                            context.startActivity(intent)
                        } else {
                            Log.e("SearchBar", "Search API call failed: ${response.errorBody()?.string()}")
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.background
            ),
            shape = RoundedCornerShape(30.dp),
            modifier = Modifier.width(410.dp)
        ) {
            Text(
                text = "SEARCH",
                fontSize = 16.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
    }
}