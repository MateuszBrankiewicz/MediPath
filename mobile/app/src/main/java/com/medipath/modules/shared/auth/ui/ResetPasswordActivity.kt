package com.medipath.modules.shared.auth.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.medipath.R
import android.content.Intent
import com.medipath.core.theme.MediPathTheme
import com.medipath.modules.shared.components.AuthTextField
import androidx.compose.runtime.getValue
import com.medipath.core.utils.ValidationUtils
import android.widget.Toast
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.testTag
import com.medipath.modules.shared.auth.ResetPasswordViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.lifecycle.viewmodel.compose.viewModel


class ResetPasswordActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MediPathTheme { ResetPasswordScreen(
                onSignUpClick = {
                    val intent = Intent(this, RegisterActivity::class.java)
                    startActivity(intent)
                    finish()
                },
                onBackClick = {
                    finish()
                },
                onResetSuccess = {
                    Toast.makeText(this, "Success! If an account exists, we've sent password reset instructions.", Toast.LENGTH_LONG).show()
                    finish()
                }
            ) }
        }
    }
}



@Composable
fun ResetPasswordScreen(
    viewModel: ResetPasswordViewModel = viewModel(),
    onSignUpClick: () -> Unit = {},
    onBackClick: () -> Unit = {},
    onResetSuccess: () -> Unit = {}
) {
    val resetError by viewModel.resetError
    val resetSuccess by viewModel.resetSuccess
    val email by viewModel.email
    val emailError by viewModel.emailError
    val isFormValid by viewModel.isFormValid

    LaunchedEffect(resetSuccess) {
        if (resetSuccess) {
            onResetSuccess()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(horizontal = 30.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
            horizontalArrangement = Arrangement.Start
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.offset(x = (-20).dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBackIosNew,
                    contentDescription = "Back to login",
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(50.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(50.dp))

        Image(painter = painterResource(id = R.drawable.logo), contentDescription = "Logo", modifier = Modifier.size(110.dp))
        Spacer(modifier = Modifier.height(60.dp))

        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
            Text("Forgot password?", fontSize = 30.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(250.dp).padding(vertical = 10.dp))
            Text("Enter your email address, and we will send you a message with instructions to reset your password.", fontSize = 16.sp, fontWeight = FontWeight.W500, color = MaterialTheme.colorScheme.onBackground)
        }

        Spacer(modifier = Modifier.height(40.dp))

        AuthTextField(
            value = email,
            onValueChange = { viewModel.onEmailChanged(it) },
            fieldText = "Email Address",
            hintText = "Enter your email address",
            keyboardType = KeyboardType.Email,
            errorMessage = emailError ?: "",
            modifier = Modifier.testTag("email_field").fillMaxWidth(),
            leadingIcon = R.drawable.user,
            onFocusLost = { viewModel.validateEmail() }
        )

        Spacer(modifier = Modifier.height(25.dp))

        if (resetError != null) {
            Text(
                text = resetError!!,
                color = MaterialTheme.colorScheme.error,
                fontSize = 14.sp,
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                viewModel.clearError()
                viewModel.resetPassword()
            },
            enabled = isFormValid,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isFormValid) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                contentColor = MaterialTheme.colorScheme.background,
                disabledContainerColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledContentColor = MaterialTheme.colorScheme.background
            ),
            shape = RoundedCornerShape(30.dp),
            modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp)
        ) {
            Text(
                text = "RESET PASSWORD",
                fontSize = 14.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row {
            Text("Don’t have an account? ", fontWeight = FontWeight.W400, fontSize = 14.sp)
            Text("Sign up", fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.clickable {
                onSignUpClick()
            })
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ResetScreenPreview() {
    MediPathTheme { ResetPasswordScreen() }
}
