package com.medipath.modules.shared.components

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
import com.medipath.modules.shared.auth.RegisterViewModel

@Composable
fun SearchableCityDropdown(
    viewModel: RegisterViewModel,
    selectedCity: String,
    onCitySelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    errorMessage: String = "",
    onFocusLost: () -> Unit = {},
    shape : RoundedCornerShape = RoundedCornerShape(20.dp),
    isEdit: Boolean = false
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
            label = {
                if (isEdit)
                    Text("City")
                else
                    Text("City", color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp)
            },
            placeholder = {
                Text(
                    "Select city or type to search",
                    color = MaterialTheme.colorScheme.onSurface,
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
                    if (focusState.isFocused) {
                        hadFocus = true
                        expanded = true
                    } else if (hadFocus){
                        onFocusLost()
                    }
                },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (errorMessage.isNotEmpty()) MaterialTheme.colorScheme.error else Color.Transparent,
                unfocusedBorderColor = if (errorMessage.isNotEmpty()) MaterialTheme.colorScheme.error else Color.Transparent,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            ),
            shape = shape,
            isError = errorMessage.isNotEmpty()
        )
        if (expanded && filteredCities.isNotEmpty()) {
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .widthIn(min = 350.dp)
                    .background(
                        shape = RoundedCornerShape(5.dp),
                        color = MaterialTheme.colorScheme.onBackground
                    )
            ) {
                filteredCities.take(5).forEach { cityItem ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = cityItem.name,
                                color = MaterialTheme.colorScheme.background
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
            color = MaterialTheme.colorScheme.error,
            fontSize = 12.sp,
            modifier = Modifier.padding(start = 16.dp, top = 4.dp)
        )
    }
    }
}
