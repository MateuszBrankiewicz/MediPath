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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.medipath.R
import com.medipath.modules.shared.auth.RegisterViewModel

@Composable
fun SearchableProvinceDropdown(
    viewModel: RegisterViewModel,
    selectedProvince: String,
    onProvinceSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    errorMessage: String = "",
    onFocusLost: () -> Unit = {},
    shape : RoundedCornerShape = RoundedCornerShape(20.dp),
    isEdit: Boolean = false
) {
    var hadFocus by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    val provinces by viewModel.provinces.collectAsState()

    val filteredProvinces = remember(provinces, query) {
        if (query.isEmpty()) {
            provinces
        } else {
            provinces.filter { it.contains(query, ignoreCase = true) }
        }
    }

    Column {
        Box(modifier = modifier) {
            OutlinedTextField(
                value = if (selectedProvince.isNotEmpty()) selectedProvince else query,
                onValueChange = { newValue ->
                    query = newValue
                    expanded = true
                    if (newValue != selectedProvince) {
                        onProvinceSelected("")
                    }
                },
                label = {
                    if (isEdit)
                        Text(stringResource(R.string.province))
                    else
                        Text(stringResource(R.string.province), color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp)
                },
                placeholder = {
                    Text(
                        stringResource(R.string.select_province_or_type_to_search),
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
                        } else if (hadFocus) {
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
            if (expanded && filteredProvinces.isNotEmpty()) {
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
                    filteredProvinces.take(5).forEach { provinceItem ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = provinceItem,
                                    color = MaterialTheme.colorScheme.background
                                )
                            },
                            onClick = {
                                onProvinceSelected(provinceItem)
                                query = provinceItem
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
