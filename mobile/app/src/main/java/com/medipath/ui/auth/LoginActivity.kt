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
import androidx.compose.ui.text.style.TextAlign
import com.medipath.data.models.LoginRequest
import com.medipath.viewmodels.LoginViewModel


class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MediPathTheme { LoginScreen(
                onSignUpClick = {
                    val intent = Intent(this, RegisterActivity::class.java)
                    startActivity(intent)
                    finish()
                },
                onLoginSuccess = {
                    Toast.makeText(this, "Login successful!", Toast.LENGTH_LONG).show()
//                    val intent = Intent(this, MainActivity::class.java)
//                    startActivity(intent)
//                    finish()
                },
                onForgotClick = {
                    Toast.makeText(this, "Forgot password clicked!", Toast.LENGTH_LONG).show()
                }
            ) }
        }
    }
}



@Composable
fun LoginScreen(viewModel: LoginViewModel = remember { LoginViewModel() }, onSignUpClick: () -> Unit = {}, onForgotClick: () -> Unit = {}, onLoginSuccess: () -> Unit = {}) {

    val loginError by viewModel.loginError
    val loginSuccess by viewModel.loginSuccess

    LaunchedEffect(loginSuccess) {
        if (loginSuccess) {
            onLoginSuccess()
        }
    }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var emailError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }

    val isFormValid by remember {
        derivedStateOf {
            email.isNotBlank() &&
            password.isNotBlank()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(horizontal = 30.dp),
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
                emailError = ValidationUtils.validateEmail(it)
            }, "Email Address", "Enter your email address", keyboardType = KeyboardType.Email, errorMessage = emailError,
                modifier = Modifier.testTag("email_field"),
                leadingIcon = R.drawable.user,
                onFocusLost = {
                    emailError = ValidationUtils.validateEmail(email)
                }
            )
            AuthTextField(password, {
                password = it
                passwordError = ValidationUtils.validatePassword(it)
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
                text = "SIGN IN",
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
fun LoginScreenPreview() {
    MediPathTheme { LoginScreen() }
}
