package com.medipath.modules.shared.profile.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.medipath.R
import com.medipath.modules.shared.auth.RegisterViewModel
import com.medipath.modules.shared.components.SearchableCityDropdown
import com.medipath.modules.shared.components.SearchableProvinceDropdown

@Composable
fun ContactAddressSection(
    viewModel: RegisterViewModel,
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
                text = stringResource(R.string.contact_address),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            OutlinedTextField(
                value = editedPhone,
                onValueChange = onPhoneChange,
                label = { Text(stringResource(R.string.phone_number)) },
                modifier = Modifier.fillMaxWidth(),
                isError = phoneError != null,
                supportingText = phoneError?.let { { Text(it) } },
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            SearchableProvinceDropdown(
                viewModel = viewModel,
                selectedProvince = editedProvince,
                onProvinceSelected = onProvinceChange,
                errorMessage = provinceError ?: "",
                onFocusLost = { },
                shape = RoundedCornerShape(12.dp),
                isEdit = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = editedPostal,
                onValueChange = { input ->
                    val filtered = input.filter { it.isDigit() || it == '-' }.take(6)
                    val formatted = if (filtered.length == 2 && !filtered.contains('-')) {
                        "$filtered-"
                    } else {
                        filtered
                    }
                    onPostalChange(formatted)
                },
                label = { Text(stringResource(R.string.postal_code)) },
                modifier = Modifier.fillMaxWidth(),
                isError = postalCodeError != null,
                supportingText = postalCodeError?.let { { Text(it) } },
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            SearchableCityDropdown(
                viewModel = viewModel,
                selectedCity = editedCity,
                onCitySelected = onCityChange,
                errorMessage = cityError ?: "",
                onFocusLost = { },
                shape = RoundedCornerShape(12.dp),
                isEdit = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = editedNumber,
                    onValueChange = onNumberChange,
                    label = { Text(stringResource(R.string.number)) },
                    modifier = Modifier.weight(1f),
                    isError = numberError != null,
                    supportingText = numberError?.let { { Text(it) } },
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = editedStreet,
                    onValueChange = onStreetChange,
                    label = { Text(stringResource(R.string.street)) },
                    modifier = Modifier.weight(1f),
                    isError = streetError != null,
                    supportingText = streetError?.let { { Text(it) } },
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
    }
}
