package com.medipath.ui.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.runtime.getValue
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.medipath.viewmodels.RegisterViewModel

@Composable
fun SearchableCityDropdown(
    viewModel: RegisterViewModel,
    selectedCity: String,
    onCitySelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    errorMessage: String = "",
    onFocusLost: () -> Unit = {}
) {
    var hadFocus by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    val cities by viewModel.cities

    val filteredCities = remember(cities, query) {
        if (query.isEmpty()) {
            cities
        } else {
            cities.filter { it.name.contains(query, ignoreCase = true) }
        }
    }

    Column {
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
            label = { Text("City", color = Color(0xFF5D5D5D), fontSize = 14.sp) },
            placeholder = {
                Text(
                    "Select city or type to search",
                    color = Color(0xFF5D5D5D),
                    fontSize = 14.sp
                )
            },
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
                .onFocusChanged { focusState ->
//                    Log.d("debug", "Focus changed: ${focusState.isFocused}")
                    if (focusState.isFocused) {
                        hadFocus = true
                        expanded = true
                    } else if (hadFocus){
                        onFocusLost()
                    }
                },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (errorMessage.isNotEmpty()) Color.Red else Color.Transparent,
                unfocusedBorderColor = if (errorMessage.isNotEmpty()) Color.Red else Color.Transparent,
                focusedContainerColor = Color(0xFFD9D9D9),
                unfocusedContainerColor = Color(0xFFD9D9D9)
            ),
            shape = RoundedCornerShape(20.dp),
            isError = errorMessage.isNotEmpty()
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
                        text = {
                            Text(
                                text = cityItem.name,
                                color = Color.White
                            )
                        },
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
    if (errorMessage.isNotEmpty()) {
        Text(
            text = errorMessage,
            color = Color.Red,
            fontSize = 12.sp,
            modifier = Modifier.padding(start = 16.dp, top = 4.dp)
        )
    }
    }
}
