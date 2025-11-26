package com.medipath.modules.shared.auth.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import com.medipath.SplashActivity
import com.medipath.modules.shared.auth.LoginViewModel
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
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
                        Toast.makeText(this@LoginActivity,
                            getString(R.string.login_successful), Toast.LENGTH_LONG).show()
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
    onSignUpClick: () -> Unit = {},
    onForgotClick: () -> Unit = {},
    onLoginSuccess: () -> Unit = {},
    viewModel: LoginViewModel = viewModel()
) {

    val loginError by viewModel.loginError.collectAsState()
    val loginSuccess by viewModel.loginSuccess.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()
    val emailError by viewModel.emailError.collectAsState()
    val passwordError by viewModel.passwordError.collectAsState()

    val isFormValid by remember(email, password, emailError, passwordError) {
        derivedStateOf {
            email.isNotBlank() &&
                    password.isNotBlank() &&
                    emailError == null &&
                    passwordError == null
        }
    }

    LaunchedEffect(loginSuccess) {
        if (loginSuccess) {
            onLoginSuccess()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(WindowInsets.systemBars.asPaddingValues())
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(painter = painterResource(id = R.drawable.logo), contentDescription = stringResource(
                R.string.logo
            ), modifier = Modifier.size(120.dp))
            Spacer(modifier = Modifier.height(60.dp))

            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
                Text(
                    stringResource(R.string.welcome_back), fontSize = 30.sp, fontWeight = FontWeight.Bold, modifier = Modifier
                        .width(250.dp)
                        .padding(vertical = 10.dp))
                Text(stringResource(R.string.login_to_your_account), fontSize = 17.sp, fontWeight = FontWeight.W500, color = MaterialTheme.colorScheme.onBackground)
            }

            Spacer(modifier = Modifier.height(40.dp))

            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp), horizontalAlignment = Alignment.End) {
                AuthTextField(
                    value = email,
                    onValueChange = { viewModel.onEmailChanged(it) },
                    fieldText = stringResource(R.string.email_address),
                    hintText = stringResource(R.string.enter_your_email_address),
                    keyboardType = KeyboardType.Email,
                    errorMessage = emailError ?: "",
                    modifier = Modifier.testTag("email_field"),
                    leadingIcon = R.drawable.user,
                    onFocusLost = { viewModel.validateEmail() }
                )
                AuthTextField(
                    value = password,
                    onValueChange = { viewModel.onPasswordChanged(it) },
                    fieldText = stringResource(R.string.password),
                    hintText = stringResource(R.string.enter_your_password),
                    isPassword = true,
                    errorMessage = passwordError ?: "",
                    modifier = Modifier.testTag("password_field"),
                    leadingIcon = R.drawable.password,
                    onFocusLost = { viewModel.validatePassword() }
                )

                Text(stringResource(R.string.forgot_password), fontSize = 12.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.ExtraBold, modifier = Modifier.clickable {
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
                    text = if (isLoading) stringResource(R.string.signing_in) else stringResource(R.string.sign_in),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row {
                Text(stringResource(R.string.don_t_have_an_account), fontWeight = FontWeight.W400, fontSize = 14.sp)
                Text(stringResource(R.string.sign_up), fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.clickable {
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