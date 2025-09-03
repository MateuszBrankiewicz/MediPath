package com.medipath.ui.auth

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
import com.medipath.ui.theme.MediPathTheme
import com.medipath.ui.components.AuthTextField
import androidx.compose.runtime.getValue
import com.medipath.utils.ValidationUtils
import android.widget.Toast
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.testTag
import com.medipath.viewmodels.ResetPasswordViewModel


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
                onResetSuccess = {
                    Toast.makeText(this, "Success! If an account exists for this email, we've sent password reset instructions.", Toast.LENGTH_LONG).show()
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            ) }
        }
    }
}



@Composable
fun ResetPasswordScreen(viewModel: ResetPasswordViewModel = remember { ResetPasswordViewModel() }, onSignUpClick: () -> Unit = {}, onResetSuccess: () -> Unit = {}) {

    val resetError by viewModel.resetError
    val resetSuccess by viewModel.resetSuccess

    LaunchedEffect(resetSuccess) {
        if (resetSuccess) {
            onResetSuccess()
        }
    }

    var email by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf("") }

    val isFormValid by remember {
        derivedStateOf {
            email.isNotBlank() && emailError.isEmpty()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(horizontal = 30.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(painter = painterResource(id = R.drawable.logo), contentDescription = "Logo", modifier = Modifier.size(110.dp))
        Spacer(modifier = Modifier.height(60.dp))

        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
            Text("Forgot password?", fontSize = 30.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(250.dp).padding(vertical = 10.dp))
            Text("Enter your email address, and we will send you a message with instructions to reset your password.", fontSize = 16.sp, fontWeight = FontWeight.W500, color = MaterialTheme.colorScheme.onBackground)
        }

        Spacer(modifier = Modifier.height(40.dp))

            AuthTextField(email, {
                email = it
                emailError = ValidationUtils.validateEmail(it)
            }, "Email Address", "Enter your email address", keyboardType = KeyboardType.Email, errorMessage = emailError,
                modifier = Modifier.testTag("email_field").fillMaxWidth(),
                leadingIcon = R.drawable.user,
                onFocusLost = {
                    emailError = ValidationUtils.validateEmail(email)
                }
            )

        Spacer(modifier = Modifier.height(25.dp))

        if (resetError.isNotEmpty()) {
            Text(
                text = resetError,
                color = MaterialTheme.colorScheme.error,
                fontSize = 14.sp,
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                viewModel.clearError()
                viewModel.resetPassword(email)
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
