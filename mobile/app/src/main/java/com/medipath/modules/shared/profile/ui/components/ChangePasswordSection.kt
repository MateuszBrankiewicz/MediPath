package com.medipath.modules.shared.profile.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ChangePasswordSection(
    currentPassword: String,
    onCurrentPasswordChange: (String) -> Unit,
    currentPasswordError: String?,
    newPassword: String,
    onNewPasswordChange: (String) -> Unit,
    newPasswordError: String?,
    onResetPassword: () -> Unit,
    enabled: Boolean
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
                text = "Change password",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            OutlinedTextField(
                value = currentPassword,
                onValueChange = onCurrentPasswordChange,
                label = { Text("Current password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                isError = currentPasswordError != null,
                supportingText = currentPasswordError?.let { { Text(it) } },
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = newPassword,
                onValueChange = onNewPasswordChange,
                label = { Text("New password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                isError = newPasswordError != null,
                supportingText = newPasswordError?.let { { Text(it) } },
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onResetPassword,
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(30.dp)
            ) {
                Text(text = "RESET PASSWORD", color = MaterialTheme.colorScheme.background)
            }
        }
    }
}
