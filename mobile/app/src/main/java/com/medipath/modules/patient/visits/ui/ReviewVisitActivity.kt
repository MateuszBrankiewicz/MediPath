package com.medipath.modules.patient.visits.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.medipath.core.network.RetrofitInstance
import com.medipath.core.theme.LocalCustomColors
import com.medipath.core.theme.MediPathTheme
import com.medipath.modules.patient.visits.ReviewVisitViewModel
import com.medipath.modules.patient.visits.ui.components.RatingCard
import com.medipath.modules.shared.auth.ui.LoginActivity

class ReviewVisitActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val visitId = intent.getStringExtra("VISIT_ID") ?: ""
        val doctorName = intent.getStringExtra("DOCTOR_NAME") ?: ""
        val institutionName = intent.getStringExtra("INSTITUTION_NAME") ?: ""
        val preDoctorRating = intent.getDoubleExtra("PRE_DOCTOR_RATING", 0.0)
        val preInstitutionRating = intent.getDoubleExtra("PRE_INSTITUTION_RATING", 0.0)
        val preComments = intent.getStringExtra("PRE_COMMENTS") ?: ""
        val commentId = intent.getStringExtra("COMMENT_ID") ?: ""

        setContent {
            MediPathTheme {
                ReviewVisitScreen(
                    visitId = visitId,
                    doctorName = doctorName,
                    institutionName = institutionName,
                    commentId = commentId,
                    initialDoctorRating = if (preDoctorRating > 0.0) preDoctorRating else null,
                    initialInstitutionRating = if (preInstitutionRating > 0.0) preInstitutionRating else null,
                    initialComments = if (preComments.isNotEmpty()) preComments else null,
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
    viewModel: ReviewVisitViewModel = remember { ReviewVisitViewModel() },
    commentId: String? = null,
    initialDoctorRating: Double? = null,
    initialInstitutionRating: Double? = null,
    initialComments: String? = null,
    onBackClick: () -> Unit,
    onSubmitSuccess: () -> Unit
) {
    val colors = LocalCustomColors.current
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val submitSuccess by viewModel.submitSuccess.collectAsState()
    val shouldRedirectToLogin by viewModel.shouldRedirectToLogin.collectAsState()
    val context = LocalContext.current

    
    var doctorRating by remember { mutableStateOf(initialDoctorRating ?: 0.0) }
    var institutionRating by remember { mutableStateOf(initialInstitutionRating ?: 0.0) }
    var comments by remember { mutableStateOf(initialComments ?: "") }

    LaunchedEffect(submitSuccess) {
        if (submitSuccess) {
            val message = if (commentId.isNullOrEmpty()) {
                "Review added successfully"
            } else {
                "Review updated successfully"
            }
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            onSubmitSuccess()
        }
    }

    if (shouldRedirectToLogin) {
        LaunchedEffect(Unit) {
            Toast.makeText(context, "Session expired. Please log in again.", Toast.LENGTH_LONG).show()
            val sessionManager = RetrofitInstance.getSessionManager()
            sessionManager.deleteSessionId()
            context.startActivity(
                Intent(context, LoginActivity::class.java)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            )
            (context as? ComponentActivity)?.finish()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        if (commentId.isNullOrEmpty()) "Add Review" else "Edit Review",
                        color = Color.White
                    ) 
                },
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
                            commentId = commentId
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
                            text = if (commentId.isNullOrEmpty()) "SAVE REVIEW" else "UPDATE REVIEW",
                            fontSize = 16.sp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    }
                }
            }
        }
    }
