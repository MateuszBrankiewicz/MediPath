package com.medipath

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.medipath.core.network.DataStoreSessionManager
import com.medipath.modules.shared.auth.ui.LoginActivity
import com.medipath.modules.patient.home.ui.HomeActivity
import com.medipath.core.theme.MediPathTheme
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val sessionManager = DataStoreSessionManager(this)

        setContent {
            MediPathTheme {
                SplashScreen(
                    onGetStartedClick = {
                        lifecycleScope.launch {
                            val isLoggedIn = sessionManager.isLoggedIn()
                            if (isLoggedIn) {
                                startActivity(Intent(this@SplashActivity, HomeActivity::class.java))
                            } else {
                                startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
                            }
                            finish()
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun SplashScreen(onGetStartedClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Image(
            painter = painterResource(id = R.drawable.splash_background),
            contentDescription = "Background",
            modifier = Modifier
                .fillMaxWidth()
                .height(610.dp),
            contentScale = ContentScale.Crop
        )

        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "MediPath Logo",
            modifier = Modifier
                .size(100.dp)
                .offset(y = 200.dp)
                .align(Alignment.TopCenter)
        )

        Column(
            modifier = Modifier
                .width(350.dp)
                .align(Alignment.BottomCenter)
                .padding(bottom = 25.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Welcome!",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Sign up to book your first visit and start setting medication reminders",
                fontSize = 14.sp,
                fontWeight = FontWeight.W400,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onGetStartedClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.background
                ),
                shape = RoundedCornerShape(30.dp),
                modifier = Modifier
                    .width(410.dp)
                    .padding(vertical = 14.dp)
            ) {
                Text(
                    text = "GET STARTED",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    MediPathTheme {
        SplashScreen(onGetStartedClick = {})
    }
}