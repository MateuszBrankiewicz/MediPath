package com.medipath.modules.patient.visits.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.StarHalf
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.medipath.core.network.DataStoreSessionManager
import com.medipath.core.theme.LocalCustomColors
import com.medipath.core.theme.MediPathTheme
import com.medipath.modules.patient.visits.ReviewVisitViewModel
import kotlin.math.round

class ReviewVisitActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val visitId = intent.getStringExtra("VISIT_ID") ?: ""
        val doctorName = intent.getStringExtra("DOCTOR_NAME") ?: ""
        val institutionName = intent.getStringExtra("INSTITUTION_NAME") ?: ""
        val sessionManager = DataStoreSessionManager(this)

        setContent {
            MediPathTheme {
                ReviewVisitScreen(
                    visitId = visitId,
                    doctorName = doctorName,
                    institutionName = institutionName,
                    sessionManager = sessionManager,
                    onBackClick = { finish() },
                    onSubmitSuccess = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewVisitScreen(
    visitId: String,
    doctorName: String,
    institutionName: String,
    sessionManager: DataStoreSessionManager,
    viewModel: ReviewVisitViewModel = remember { ReviewVisitViewModel() },
    onBackClick: () -> Unit,
    onSubmitSuccess: () -> Unit
) {
    val colors = LocalCustomColors.current
    val isLoading by viewModel.isLoading
    val error by viewModel.error
    val submitSuccess by viewModel.submitSuccess
    
    var doctorRating by remember { mutableStateOf(0.0) }
    var institutionRating by remember { mutableStateOf(0.0) }
    var comments by remember { mutableStateOf("") }

    LaunchedEffect(submitSuccess) {
        if (submitSuccess) {
            onSubmitSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Review visit", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Return",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.blue900
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.secondary)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Share your thoughts about your recent visit",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                RatingCard(
                    title = "Doctor's rating*",
                    subtitle = doctorName,
                    rating = doctorRating,
                    onRatingChange = { doctorRating = it },
                    color = colors.blue900
                )

                RatingCard(
                    title = "Institution's rating*",
                    subtitle = institutionName,
                    rating = institutionRating,
                    onRatingChange = { institutionRating = it },
                    color = colors.blue900
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Your feedback on the visit",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        OutlinedTextField(
                            value = comments,
                            onValueChange = { comments = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp),
                            placeholder = { 
                                Text("Share your experience...")
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colors.blue900,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                if (error != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = error ?: "Unknown error",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                Button(
                    onClick = {
                        viewModel.submitReview(
                            visitId = visitId,
                            doctorRating = doctorRating,
                            institutionRating = institutionRating,
                            comments = comments,
                            sessionManager = sessionManager
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    enabled = !isLoading && doctorRating > 0 && institutionRating > 0,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.blue900,
                        disabledContainerColor = colors.blue900.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(30.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "SAVE REVIEW",
                            fontSize = 16.sp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RatingCard(
    title: String,
    subtitle: String,
    rating: Double,
    onRatingChange: (Double) -> Unit,
    color: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.primary
            )
            
            Text(
                text = subtitle,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp),
                thickness = DividerDefaults.Thickness,
                color = DividerDefaults.color
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(5) { index ->
                    val fullStar = index + 1.0
                    val halfStar = index + 0.5
                    
                    Icon(
                        imageVector = when {
                            rating >= fullStar -> Icons.Default.Star
                            rating >= halfStar -> Icons.AutoMirrored.Filled.StarHalf
                            else -> Icons.Default.StarBorder
                        },
                        contentDescription = "Star ${index + 1}",
                        tint = if (rating >= halfStar) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
            
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Slider(
                    value = rating.toFloat(),
                    onValueChange = { 
                        val roundedValue = (round(it * 2) / 2.0)
                        onRatingChange(roundedValue.coerceIn(0.5, 5.0))
                    },
                    valueRange = 0.5f..5f,
                    steps = 8,
                    modifier = Modifier.fillMaxWidth(),
                    colors = SliderDefaults.colors(
                        thumbColor = color,
                        activeTrackColor = color,
                        inactiveTrackColor = color.copy(alpha = 0.3f)
                    )
                )
                
                if (rating > 0) {
                    Text(
                        text = when {
                            rating <= 1.0 -> "Very poor"
                            rating <= 2.0 -> "Poor"
                            rating <= 3.0 -> "Fair"
                            rating <= 4.0 -> "Good"
                            else -> "Excellent"
                        } + " (${"%.1f".format(rating)}/5.0)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = color,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}
