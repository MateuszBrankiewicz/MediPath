package com.medipath.modules.shared.profile.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ContactAddressSection(
    editedPhone: String,
    onPhoneChange: (String) -> Unit,
    phoneError: String?,
    editedCity: String,
    onCityChange: (String) -> Unit,
    cityError: String?,
    editedProvince: String,
    onProvinceChange: (String) -> Unit,
    provinceError: String?,
    editedPostal: String,
    onPostalChange: (String) -> Unit,
    postalCodeError: String?,
    editedNumber: String,
    onNumberChange: (String) -> Unit,
    numberError: String?,
    editedStreet: String,
    onStreetChange: (String) -> Unit,
    streetError: String?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Contact & Address",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            OutlinedTextField(
                value = editedPhone,
                onValueChange = onPhoneChange,
                label = { Text("Phone number") },
                modifier = Modifier.fillMaxWidth(),
                isError = phoneError != null,
                supportingText = phoneError?.let { { Text(it) } },
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = editedCity,
                onValueChange = onCityChange,
                label = { Text("City") },
                modifier = Modifier.fillMaxWidth(),
                isError = cityError != null,
                supportingText = cityError?.let { { Text(it) } },
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = editedProvince,
                    onValueChange = onProvinceChange,
                    label = { Text("Province") },
                    modifier = Modifier.weight(1f),
                    isError = provinceError != null,
                    supportingText = provinceError?.let { { Text(it) } },
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = editedPostal,
                    onValueChange = onPostalChange,
                    label = { Text("Postal code") },
                    modifier = Modifier.weight(1f),
                    isError = postalCodeError != null,
                    supportingText = postalCodeError?.let { { Text(it) } },
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = editedNumber,
                    onValueChange = onNumberChange,
                    label = { Text("Number") },
                    modifier = Modifier.weight(1f),
                    isError = numberError != null,
                    supportingText = numberError?.let { { Text(it) } },
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = editedStreet,
                    onValueChange = onStreetChange,
                    label = { Text("Street") },
                    modifier = Modifier.weight(1f),
                    isError = streetError != null,
                    supportingText = streetError?.let { { Text(it) } },
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
    }
}
