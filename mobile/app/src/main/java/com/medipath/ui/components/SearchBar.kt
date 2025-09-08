package com.medipath.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun SearchBar(
    onSearch: (type: String, query: String) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("by-name") }

    Column(
        modifier = Modifier.padding(horizontal = 15.dp)
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("Search...") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            singleLine = true,
            shape = CircleShape,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.background,
                unfocusedContainerColor = MaterialTheme.colorScheme.background,
                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
            ),
            trailingIcon = {
                IconButton(onClick = { onSearch(selectedType, query) }) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        )

        Row(Modifier.padding(horizontal = 7.dp)) {
            TextButton(
                onClick = { selectedType = "by-name" },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = if (selectedType == "by-name") Color(0xFF88A8C9)
                    else MaterialTheme.colorScheme.onSurface
                )
            ) { Text("By name") }

            TextButton(
                onClick = { selectedType = "by-spec" },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = if (selectedType == "by-spec") Color(0xFF88A8C9)
                    else MaterialTheme.colorScheme.onSurface
                )
            ) { Text("By specialisation (type)") }
        }
    }
}
