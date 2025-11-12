package com.medipath

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.medipath.core.network.RetrofitInstance
import com.medipath.modules.shared.auth.ui.LoginActivity
import com.medipath.modules.patient.home.ui.HomeActivity
import com.medipath.modules.doctor.dashboard.ui.DoctorDashboardActivity
import com.medipath.core.theme.MediPathTheme
import kotlinx.coroutines.delay

@SuppressLint("CustomSplashScreen")
class SplashActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MediPathTheme {
                SplashScreen()

                LaunchedEffect(Unit) {
                    delay(2000)

                    val sessionManager = RetrofitInstance.getSessionManager()
                    val isLoggedIn = sessionManager.isLoggedIn()
                    Log.d("SplashActivity", "Is user logged in? $isLoggedIn")

                    if (isLoggedIn) {
                        try {
                            (application as MediPathApplication).initializeWebSocket()
                            
                            val settingsResponse = RetrofitInstance.settingsService.getSettings()
                            if (settingsResponse.isSuccessful) {
                                val lastPanel = settingsResponse.body()?.settings?.lastPanel ?: 1
                                val targetActivity = if (lastPanel == 2) {
                                    DoctorDashboardActivity::class.java
                                } else {
                                    HomeActivity::class.java
                                }
                                startActivity(Intent(this@SplashActivity, targetActivity))
                            } else {
                                startActivity(Intent(this@SplashActivity, HomeActivity::class.java))
                            }
                        } catch (e: Exception) {
                            startActivity(Intent(this@SplashActivity, HomeActivity::class.java))
                        }
                    } else {
                        startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
                    }
                    finish()
                }
            }
        }
    }
}

@Composable
fun SplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(WindowInsets.navigationBars.asPaddingValues())
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
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Loading MediPath...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(16.dp))

            LinearProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 60.dp)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    MediPathTheme {
        SplashScreen()
    }
}