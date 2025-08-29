package com.medipath.auth

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.medipath.R
import com.medipath.SplashActivity
import android.content.Intent
import com.medipath.ui.theme.MediPathTheme


class RegisterActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MediPathTheme { RegisterScreen(
                onSignInClick = {
                    val intent = Intent(this, SplashActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            ) }
        }
    }
}

@Composable
fun RegisterScreen(onSignInClick: () -> Unit = {}) {
    var isChecked by remember { mutableStateOf(false) }
    
    var name by remember { mutableStateOf("") }
    var surname by remember { mutableStateOf("") }
    var governmentId by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") }
    var number by remember { mutableStateOf("") }
    var street by remember { mutableStateOf("") }
    var postalCode by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    val provinces = listOf("")
    val cities = listOf("")

    Column(
        modifier = Modifier.fillMaxSize().background(Color.White).verticalScroll(rememberScrollState()).padding(30.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(painter = painterResource(id = R.drawable.logo), contentDescription = "Logo", modifier = Modifier.size(90.dp))
        Spacer(modifier = Modifier.height(60.dp))

        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
            Text("Take care of your health.", fontSize = 26.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(250.dp))
            Text("Create an account", fontSize = 16.sp, fontWeight = FontWeight.W500, color = Color(0xFF284662))
        }

        Spacer(modifier = Modifier.height(30.dp))

        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            CustomTextField(name, { name = it }, "Name")
            CustomTextField(surname, { surname = it }, "Surname")
            CustomTextField(governmentId, { governmentId = it }, "Government ID")
            CustomTextField(birthDate, { birthDate = it }, "Birth Date (DD-MM-YYYY)")

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CustomTextField(postalCode, { postalCode = it }, "Postal Code", Modifier.weight(1f))
            }

            CustomTextField(phoneNumber, { phoneNumber = it }, "Phone Number", keyboardType = KeyboardType.Phone)
            CustomTextField(email, { email = it }, "Email Address", keyboardType = KeyboardType.Email)
            CustomTextField(password, { password = it }, "Password", isPassword = true)
            CustomTextField(confirmPassword, { confirmPassword = it }, "Confirm password", isPassword = true)
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(isChecked, { isChecked = it })
            Text("Accept Terms & Conditions", fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            {}, 
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black, contentColor = Color.White),
            shape = RoundedCornerShape(30.dp),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 14.dp)
        ) {
            Text(
                text =" SIGN UP",
                fontSize = 16.sp,
                modifier = Modifier
                .padding(vertical = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row {
            Text("Already have an account? ", fontWeight = FontWeight.W400)
            Text("Sign in", fontWeight = FontWeight.Bold, modifier = Modifier.clickable { 
                onSignInClick()
            })
        }
    }
}

@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    hintText: String,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value, onValueChange,
        placeholder = { Text(hintText, color = Color(0xFF5D5D5D), fontSize = 14.sp) },
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Transparent, unfocusedBorderColor = Color.Transparent, focusedContainerColor = Color(0xFFD9D9D9), unfocusedContainerColor = Color(0xFFD9D9D9)),
        shape = RoundedCornerShape(20.dp)
    )
}

@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    MediPathTheme { RegisterScreen() }
}