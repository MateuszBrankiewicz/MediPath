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
import com.medipath.utils.ValidationUtils
import android.widget.Toast
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.lifecycleScope
import com.medipath.core.network.DataStoreSessionManager
import com.medipath.core.models.LoginRequest
import com.medipath.modules.patient.home.ui.HomeActivity
import com.medipath.modules.shared.auth.LoginViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import android.util.Log


class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val sessionManager = DataStoreSessionManager(this)
        setContent {
            MediPathTheme {
                var shouldNavigate by remember { mutableStateOf(false) }
                var sessionId by remember { mutableStateOf("") }
                LoginScreen(
                    onSignUpClick = {
                        val intent = Intent(this, RegisterActivity::class.java)
                        startActivity(intent)
                        finish()
                    },
                    onLoginSuccess = { id ->
                        sessionId = id
                        shouldNavigate = true
                    },
                    onForgotClick = {
                        val intent = Intent(this, ResetPasswordActivity::class.java)
                        startActivity(intent)
                    }
                )
                if (shouldNavigate && sessionId.isNotEmpty()) {
                    LaunchedEffect(sessionId) {
                        // Save session id on a background dispatcher and only navigate after it's saved
                        lifecycleScope.launch(Dispatchers.IO) {
                            try {
                                Log.d("LoginActivity", "Saving session id (masked): ${sessionId.take(8)}...")
                                sessionManager.saveSessionId(sessionId)
                                Log.d("LoginActivity", "Session id saved")
                            } catch (e: Exception) {
                                Log.e("LoginActivity", "Error saving session id: $e")
                            }

                            withContext(Dispatchers.Main) {
                                Toast.makeText(this@LoginActivity, "Login successful!", Toast.LENGTH_LONG).show()
                                val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                        }
                    }
                }
            }
        }
    }
}



@Composable
fun LoginScreen(viewModel: LoginViewModel = remember { LoginViewModel() }, onSignUpClick: () -> Unit = {}, onForgotClick: () -> Unit = {}, onLoginSuccess: (String) -> Unit = {}) {
    val loginError by viewModel.loginError
    val loginSuccess by viewModel.loginSuccess
    val sessionId by viewModel.sessionId
    val isLoading by viewModel.isLoading

    LaunchedEffect(loginSuccess, sessionId) {
        if (loginSuccess && sessionId.isNotEmpty()) {
            onLoginSuccess(sessionId)
        }
    }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var emailError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }

    LaunchedEffect(email) {
        if (email.isNotBlank()) {
            kotlinx.coroutines.delay(300)
            withContext(kotlinx.coroutines.Dispatchers.Default) {
                emailError = ValidationUtils.validateEmail(email)
            }
        } else {
            emailError = ""
        }
    }
    LaunchedEffect(password) {
        if (password.isNotBlank()) {
            kotlinx.coroutines.delay(300)
            withContext(kotlinx.coroutines.Dispatchers.Default) {
                passwordError = ValidationUtils.validatePassword(password)
            }
        } else {
            passwordError = ""
        }
    }

    val isFormValid by remember {
        derivedStateOf {
            email.isNotBlank() &&
            password.isNotBlank() &&
            emailError.isEmpty() &&
            passwordError.isEmpty()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(WindowInsets.navigationBars.asPaddingValues()).background(MaterialTheme.colorScheme.background).padding(horizontal = 30.dp),
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
                AuthTextField(email, {
                    email = it
                }, "Email Address", "Enter your email address", keyboardType = KeyboardType.Email, errorMessage = emailError,
                    modifier = Modifier.testTag("email_field"),
                    leadingIcon = R.drawable.user,
                    onFocusLost = {
                        emailError = ValidationUtils.validateEmail(email)
                    }
                )
                AuthTextField(password, {
                    password = it
                }, "Password", "Enter your password", isPassword = true, errorMessage = passwordError,
                    modifier = Modifier.testTag("password_field"),
                    leadingIcon = R.drawable.password,
                    onFocusLost = {
                        passwordError = ValidationUtils.validatePassword(password)
                    }
                )
                Text("Forgot password?", fontSize = 12.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.ExtraBold, modifier = Modifier.clickable {
                    onForgotClick()
                })
            }

            Spacer(modifier = Modifier.height(25.dp))

            if (loginError.isNotEmpty()) {
                Text(
                    text = loginError,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp,
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    viewModel.clearError()
                    val loginRequest = LoginRequest(
                        email = email,
                        password = password
                    )
                    viewModel.loginUser(loginRequest)
                },
                enabled = isFormValid && !isLoading,
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
                modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background.copy(alpha = 0.5f)),
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
