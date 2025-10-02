package com.medipath

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
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
    var startAnimations by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        startAnimations = true
    }

    val infiniteTransition = rememberInfiniteTransition(label = "splash_animation")

    val fadeInAlpha by animateFloatAsState(
        targetValue = if (startAnimations) 1f else 0f,
        animationSpec = tween(durationMillis = 800, easing = EaseOutCubic),
        label = "fade_in"
    )

    val logoOffset by animateFloatAsState(
        targetValue = if (startAnimations) 0f else -100f,
        animationSpec = tween(durationMillis = 1000, delayMillis = 200, easing = EaseOutBack),
        label = "logo_offset"
    )

    val logoScale by animateFloatAsState(
        targetValue = if (startAnimations) 1f else 0.3f,
        animationSpec = tween(durationMillis = 1000, delayMillis = 200, easing = EaseOutBack),
        label = "logo_scale"
    )

    val bottomSectionOffset by animateFloatAsState(
        targetValue = if (startAnimations) 0f else 200f,
        animationSpec = tween(durationMillis = 800, delayMillis = 600, easing = EaseOutCubic),
        label = "bottom_section_offset"
    )

    val buttonScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutQuart),
            repeatMode = RepeatMode.Reverse
        ),
        label = "button_pulse"
    )

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
                .graphicsLayer {
                    scaleX = logoScale
                    scaleY = logoScale
                    translationY = logoOffset
                    alpha = fadeInAlpha
                }
        )

        Column(
            modifier = Modifier
                .width(350.dp)
                .align(Alignment.BottomCenter)
                .padding(bottom = 25.dp)
                .graphicsLayer {
                    translationY = bottomSectionOffset
                    alpha = fadeInAlpha
                },
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
                    .graphicsLayer {
                        scaleX = buttonScale
                        scaleY = buttonScale
                        shadowElevation = (buttonScale - 1f) * 50f
                    }
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