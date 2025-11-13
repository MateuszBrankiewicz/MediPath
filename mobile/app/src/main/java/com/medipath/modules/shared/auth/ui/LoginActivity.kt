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
import android.widget.Toast
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import com.medipath.SplashActivity
import com.medipath.modules.shared.auth.LoginViewModel
import androidx.lifecycle.viewmodel.compose.viewModel


class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MediPathTheme {
                LoginScreen(
                    onSignUpClick = {
                        val intent = Intent(this, RegisterActivity::class.java)
                        startActivity(intent)
                        finish()
                    },
                    onLoginSuccess = {
                        Toast.makeText(this@LoginActivity, "Login successful!", Toast.LENGTH_LONG).show()
                        val intent = Intent(this@LoginActivity, SplashActivity::class.java)
                        startActivity(intent)
                        finish()
                    },
                    onForgotClick = {
                        val intent = Intent(this, ResetPasswordActivity::class.java)
                        startActivity(intent)
                    }
                )
            }
        }
    }
}



@Composable
fun LoginScreen(
    viewModel: LoginViewModel = viewModel(),
    onSignUpClick: () -> Unit = {},
    onForgotClick: () -> Unit = {},
    onLoginSuccess: () -> Unit = {}
) {
    val loginError by viewModel.loginError
    val loginSuccess by viewModel.loginSuccess
    val isLoading by viewModel.isLoading
    val email by viewModel.email
    val password by viewModel.password
    val emailError by viewModel.emailError
    val passwordError by viewModel.passwordError
    val isFormValid by viewModel.isFormValid

    LaunchedEffect(loginSuccess) {
        if (loginSuccess) {
            onLoginSuccess()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(WindowInsets.navigationBars.asPaddingValues())
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(painter = painterResource(id = R.drawable.logo), contentDescription = "Logo", modifier = Modifier.size(120.dp))
            Spacer(modifier = Modifier.height(60.dp))

            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
                Text("Welcome Back.", fontSize = 30.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(250.dp).padding(vertical = 10.dp))
                Text("Login to your account", fontSize = 17.sp, fontWeight = FontWeight.W500, color = MaterialTheme.colorScheme.onBackground)
            }

            Spacer(modifier = Modifier.height(40.dp))

            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp), horizontalAlignment = Alignment.End) {
                AuthTextField(
                    value = email,
                    onValueChange = { viewModel.onEmailChanged(it) },
                    fieldText = "Email Address",
                    hintText = "Enter your email address",
                    keyboardType = KeyboardType.Email,
                    errorMessage = emailError ?: "",
                    modifier = Modifier.testTag("email_field"),
                    leadingIcon = R.drawable.user,
                    onFocusLost = { viewModel.validateEmail() }
                )
                AuthTextField(
                    value = password,
                    onValueChange = { viewModel.onPasswordChanged(it) },
                    fieldText = "Password",
                    hintText = "Enter your password",
                    isPassword = true,
                    errorMessage = passwordError ?: "",
                    modifier = Modifier.testTag("password_field"),
                    leadingIcon = R.drawable.password,
                    onFocusLost = { viewModel.validatePassword() }
                )

                Text("Forgot password?", fontSize = 12.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.ExtraBold, modifier = Modifier.clickable {
                    onForgotClick()
                })
            }

            Spacer(modifier = Modifier.height(25.dp))

            if (loginError != null) {
                Text(
                    text = loginError!!,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp,
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    viewModel.clearError()
                    viewModel.loginUser()
                },
                enabled = isFormValid && !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isFormValid) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    contentColor = MaterialTheme.colorScheme.background,
                    disabledContainerColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledContentColor = MaterialTheme.colorScheme.background
                ),
                shape = RoundedCornerShape(30.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 14.dp)
            ) {
                Text(
                    text = if (isLoading) "Signing in..." else "SIGN IN",
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
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    MediPathTheme { LoginScreen() }
}