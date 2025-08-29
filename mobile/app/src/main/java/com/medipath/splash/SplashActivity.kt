package com.medipath

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.medipath.ui.theme.MediPathTheme
import android.content.Intent
import com.medipath.auth.RegisterActivity

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MediPathTheme {
                SplashScreen{
                    val intent = Intent(this, RegisterActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        }
    }
}


@Composable
fun SplashScreen(onGetStartedClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
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
            contentDescription = "Logo",
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
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Sign up to book your first visit and start setting medication reminders",
                fontSize = 14.sp,
                fontWeight = FontWeight.W400,
                color = Color.Black,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onGetStartedClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(30.dp),
                modifier = Modifier
                    .width(410.dp)
                    .padding(horizontal = 24.dp, vertical = 14.dp)
            ) {
                Text(
                    text = "GET STARTED",
                    fontSize = 16.sp,
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SpashScreenPreview() {
    MediPathTheme {
        SplashScreen(onGetStartedClick = {})
    }
}