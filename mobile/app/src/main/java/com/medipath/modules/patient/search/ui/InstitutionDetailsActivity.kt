package com.medipath.modules.patient.search.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.medipath.R
import com.medipath.core.models.InstitutionDoctor
import com.medipath.core.network.RetrofitInstance
import com.medipath.core.theme.MediPathTheme
import com.medipath.core.theme.LocalCustomColors
import com.medipath.modules.patient.booking.ui.AppointmentBookingActivity
import com.medipath.modules.patient.search.InstitutionDetailsViewModel
import com.medipath.modules.shared.auth.ui.LoginActivity
import com.medipath.modules.shared.components.rememberBase64Image
import com.medipath.modules.patient.booking.ui.components.CommentCard

class InstitutionDetailsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val institutionId = intent.getStringExtra("institution_id") ?: ""

        if (institutionId.isBlank()) {
            Toast.makeText(this, getString(R.string.no_institution_id_provided), Toast.LENGTH_LONG).show()
            finish()
            return
        }

        setContent {
            MediPathTheme {
                InstitutionDetailsScreen(
                    institutionId = institutionId,
                    onBack = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstitutionDetailsScreen(
    institutionId: String,
    onBack: () -> Unit = {}
) {
    val viewModel: InstitutionDetailsViewModel = viewModel()
    val doctors by viewModel.doctors.collectAsState()
    val institution by viewModel.institution.collectAsState()
    val comments by viewModel.comments.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val context = LocalContext.current
    val colors = LocalCustomColors.current

    val error by viewModel.error.collectAsState()

    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

    val shouldRedirectToLogin by viewModel.shouldRedirectToLogin.collectAsState()

    if (shouldRedirectToLogin) {
        LaunchedEffect(Unit) {
            Toast.makeText(context, context.getString(R.string.error_session), Toast.LENGTH_LONG)
                .show()
            val sessionManager = RetrofitInstance.getSessionManager()
            sessionManager.deleteSessionId()
            context.startActivity(
                Intent(context, LoginActivity::class.java)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            )
            (context as? ComponentActivity)?.finish()
        }
    }

    LaunchedEffect(institutionId) {
        if (institutionId.isNotBlank())
            viewModel.loadInstitutionData(institutionId)
    }

    if (!shouldRedirectToLogin) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.institution_details),
                            color = MaterialTheme.colorScheme.background,
                            fontSize = 23.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.back),
                                tint = MaterialTheme.colorScheme.background
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = LocalCustomColors.current.blue900
                    )
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.secondary)
            ) {
                if (isLoading && institution == null) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {

                        institution?.let { institution ->
                            item {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(100.dp)
                                                .clip(CircleShape)
                                                .border(
                                                    width = 2.dp,
                                                    color = MaterialTheme.colorScheme.primary.copy(
                                                        alpha = 0.3f
                                                    ),
                                                    shape = CircleShape
                                                )
                                        ) {
                                            val imageBitmap = rememberBase64Image(institution.image)
                                            if (imageBitmap != null) {
                                                Image(
                                                    bitmap = imageBitmap,
                                                    contentDescription = stringResource(R.string.institution_image),
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = ContentScale.Crop
                                                )
                                            } else {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .background(colors.blue900.copy(alpha = 0.2f)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        Icons.Default.Business,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(40.dp),
                                                        tint = colors.blue900
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))

                                        Text(
                                            institution.name,
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))
                                        if (institution.rating > 0) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Star,
                                                    contentDescription = stringResource(R.string.rating),
                                                    tint = colors.yellow,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Text(
                                                    text = String.format(
                                                        "%.2f",
                                                        institution.rating
                                                    ),
                                                    fontSize = 16.sp,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = stringResource(
                                                        R.string.reviews_number,
                                                        institution.numOfRatings
                                                    ),
                                                    fontSize = 14.sp,
                                                    color = MaterialTheme.colorScheme.onSurface.copy(
                                                        alpha = 0.6f
                                                    )
                                                )
                                            }
                                        }

                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 16.dp),
                                            horizontalAlignment = Alignment.Start
                                        ) {
                                            HorizontalDivider(
                                                color = MaterialTheme.colorScheme.onSurface.copy(
                                                    alpha = 0.1f
                                                )
                                            )
                                            Spacer(modifier = Modifier.height(12.dp))

                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = if (institution.isPublic) colors.green800.copy(
                                                    alpha = 0.1f
                                                ) else colors.blue800.copy(alpha = 0.1f),
                                                modifier = Modifier.align(Alignment.CenterHorizontally)
                                            ) {
                                                Text(
                                                    text = if (institution.isPublic) stringResource(
                                                        R.string.public_institution
                                                    ) else stringResource(
                                                        R.string.private_institution
                                                    ),
                                                    fontSize = 12.sp,
                                                    color = if (institution.isPublic) colors.green800 else colors.blue800,
                                                    modifier = Modifier.padding(
                                                        horizontal = 8.dp,
                                                        vertical = 4.dp
                                                    )
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(12.dp))
                                            HorizontalDivider(
                                                color = MaterialTheme.colorScheme.onSurface.copy(
                                                    alpha = 0.1f
                                                )
                                            )
                                            Spacer(modifier = Modifier.height(12.dp))

                                            Text(
                                                text = stringResource(R.string.address),
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))

                                            institution.address.let { addr ->
                                                val line1 = listOfNotNull(
                                                    addr.province.takeIf { it.isNotBlank() },
                                                    addr.city.takeIf { it.isNotBlank() }).joinToString(
                                                    ", "
                                                )
                                                val streetPart = listOfNotNull(
                                                    addr.street.takeIf { it.isNotBlank() },
                                                    addr.number.takeIf { it.isNotBlank() }).joinToString(
                                                    " "
                                                )
                                                val line2 = listOfNotNull(
                                                    streetPart.takeIf { it.isNotBlank() },
                                                    addr.postalCode.takeIf { it.isNotBlank() }).joinToString(
                                                    ", "
                                                )

                                                if (line1.isNotBlank()) Text(
                                                    text = line1,
                                                    fontSize = 14.sp,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                if (line2.isNotBlank()) Text(
                                                    text = line2,
                                                    fontSize = 14.sp,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Text(
                                text = stringResource(R.string.doctors),
                                style = MaterialTheme.typography.titleMedium
                            )
                        }

                        if (isLoading && doctors.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp), contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                }
                            }
                        } else if (doctors.isEmpty()) {
                            item {
                                Text(
                                    text = stringResource(R.string.no_doctors_found_for_this_institution),
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        } else {
                            items(doctors) { doctor ->
                                DoctorListItem(doctor = doctor) {
                                    val intent =
                                        Intent(context, AppointmentBookingActivity::class.java)
                                    intent.putExtra("doctor_id", doctor.doctorId)
                                    intent.putExtra(
                                        "doctor_name",
                                        "${doctor.doctorName} ${doctor.doctorSurname ?: ""}"
                                    )
                                    intent.putExtra("doctor_image", doctor.doctorPfp ?: "")
                                    intent.putExtra("doctor_rating", doctor.rating)
                                    intent.putExtra("num_of_ratings", doctor.numofratings)
                                    intent.putExtra("specialisations", "")
                                    intent.putExtra("institution_id", institutionId)
                                    context.startActivity(intent)
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.reviews),
                                style = MaterialTheme.typography.titleMedium
                            )
                        }

                        if (isLoading && comments.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp), contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                }
                            }
                        } else if (comments.isEmpty()) {
                            item {
                                Text(
                                    text = stringResource(R.string.no_comments_yet),
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        } else {
                            items(comments) { comment ->
                                CommentCard(comment = comment, isDoctorContext = false)
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DoctorListItem(doctor: InstitutionDoctor, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
            ) {
                val imageBitmap = rememberBase64Image(doctor.doctorPfp)
                if (imageBitmap != null) {
                    Image(
                        bitmap = imageBitmap,
                        contentDescription = stringResource(R.string.doctor_photo),
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Person, contentDescription = null)
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Dr. ${doctor.doctorName} ${doctor.doctorSurname ?: ""}", fontSize = 16.sp)
                if (!doctor.licenceNumber.isNullOrEmpty()) Text(text = stringResource(
                    R.string.licence_number,
                    doctor.licenceNumber
                ), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            }

            if (doctor.rating > 0.0) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = stringResource(R.string.rating),
                        tint = LocalCustomColors.current.yellow,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = String.format("%.2f", doctor.rating),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (doctor.numofratings > 0) {
                        Text(
                            text = "(${doctor.numofratings})",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}
