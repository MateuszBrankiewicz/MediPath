package com.medipath.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.medipath.data.models.City

@Composable
fun SearchableCityDropdown(
    cities: List<City>,
    selectedCity: String,
    onCitySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var query by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    val filteredCities = remember(cities, query) {
        if (query.isEmpty()) {
            cities
        } else {
            cities.filter { it.name.contains(query, ignoreCase = true) }
        }
    }

    Box(modifier = modifier) {
        OutlinedTextField(
            value = if (selectedCity.isNotEmpty()) selectedCity else query,
            onValueChange = { newValue ->
                query = newValue
                expanded = true
                if (newValue != selectedCity) {
                    onCitySelected("")
                }
            },
            label = { Text("Miasto", color = Color(0xFF5D5D5D), fontSize = 14.sp) },
            placeholder = { Text("Wpisz lub wybierz miasto", color = Color(0xFF5D5D5D), fontSize = 14.sp) },
            trailingIcon = {
                Icon(
                    painter = painterResource(
                        id = if (expanded) android.R.drawable.arrow_up_float
                        else android.R.drawable.arrow_down_float
                    ),
                    contentDescription = null,
                    modifier = Modifier.clickable {
                        expanded = !expanded
                        if (expanded) query = ""
                    }
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF2D4A69),
                unfocusedBorderColor = Color.Transparent,
                focusedContainerColor = Color(0xFFD9D9D9),
                unfocusedContainerColor = Color(0xFFD9D9D9)
            ),
            shape = RoundedCornerShape(20.dp)
        )
        if (expanded && filteredCities.isNotEmpty()) {
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth()
                    .background(
                        color = Color(0xFF2D4A69),
                        shape = RoundedCornerShape(12.dp)
                    )
            ) {
                filteredCities.take(5).forEach { cityItem ->
                    DropdownMenuItem(
                        text = { Text(
                            text = cityItem.name,
                            color = Color.White
                        ) },
                        onClick = {
                            onCitySelected(cityItem.name)
                            query = cityItem.name
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}