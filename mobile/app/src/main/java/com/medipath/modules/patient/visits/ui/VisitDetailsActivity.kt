package com.medipath.modules.patient.visits.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.medipath.R
import com.medipath.core.network.RetrofitInstance
import com.medipath.core.theme.LocalCustomColors
import com.medipath.core.theme.MediPathTheme
import com.medipath.modules.patient.visits.VisitDetailsViewModel
import com.medipath.modules.patient.visits.ui.components.VisitDetailsContent
import com.medipath.modules.shared.auth.ui.LoginActivity

class VisitDetailsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val visitId = intent.getStringExtra("VISIT_ID") ?: ""

        setContent {
            MediPathTheme {
                VisitDetailsScreen(
                    visitId = visitId,
                    onBackClick = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisitDetailsScreen(
    visitId: String,
    viewModel: VisitDetailsViewModel = viewModel(),
    onBackClick: () -> Unit
) {
    val colors = LocalCustomColors.current
    val visitDetails by viewModel.visitDetails.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val shouldRedirectToLogin by viewModel.shouldRedirectToLogin.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(visitId) {
        viewModel.fetchVisitDetails(visitId)
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.fetchVisitDetails(visitId)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    if (shouldRedirectToLogin) {
        LaunchedEffect(Unit) {
            Toast.makeText(context, context.getString(R.string.error_session), Toast.LENGTH_LONG).show()
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
                title = { Text(stringResource(R.string.visit_details), color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
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
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                error != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = error ?: stringResource(R.string.unknown_error),
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 16.sp
                        )
                    }
                }

                visitDetails != null && !shouldRedirectToLogin -> {
                    VisitDetailsContent(
                        visitDetails = visitDetails!!,
                        onReviewClick = {
                            val commentId = visitDetails!!.commentId
                            if (!commentId.isNullOrEmpty()) {
                                val intent = Intent(context, ReviewDetailsActivity::class.java).apply {
                                    putExtra("COMMENT_ID", commentId)
                                    putExtra("VISIT_ID", visitId)
                                }
                                context.startActivity(intent)
                            } else {
                                val intent = Intent(context, ReviewVisitActivity::class.java).apply {
                                    putExtra("VISIT_ID", visitId)
                                    putExtra(
                                        "DOCTOR_NAME",
                                        "Dr. ${visitDetails!!.doctor.doctorName} ${visitDetails!!.doctor.doctorSurname}"
                                    )
                                    putExtra(
                                        "INSTITUTION_NAME",
                                        visitDetails!!.institution.institutionName
                                    )
                                }
                                context.startActivity(intent)
                            }
                        },
                        onSeeReviewClick = {
                            val intent = Intent(context, ReviewDetailsActivity::class.java).apply {
                                putExtra("COMMENT_ID", visitDetails!!.commentId)
                                putExtra("VISIT_ID", visitId)
                            }
                            context.startActivity(intent)
                        }
                    )
                }
            }
        }
    }
}
