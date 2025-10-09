package com.medipath.modules.patient.codes.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.medipath.core.network.DataStoreSessionManager
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.medipath.core.theme.LocalCustomColors
import com.medipath.modules.patient.codes.CodesViewModel
import com.medipath.core.theme.MediPathTheme
import kotlinx.coroutines.delay

class CodesActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val codeType = intent.getStringExtra("code_type") ?: "PRESCRIPTION"
        val sessionManager = DataStoreSessionManager(this)

        setContent {
            MediPathTheme {
                val viewModel: CodesViewModel = viewModel()
                val codes by viewModel.codes
                val clipboardManager = LocalClipboardManager.current
                var copiedCode by remember { mutableStateOf("") }

                LaunchedEffect(Unit) {
                    val sessionToken = sessionManager.getSessionId()
                    if (!sessionToken.isNullOrEmpty()) {
                        val apiCodeType = when(codeType) {
                            "PRESCRIPTION" -> "prescriptions"
                            "REFERRAL" -> "referrals" 
                            else -> null
                        }
                        viewModel.fetchCodes(sessionToken, apiCodeType)
                    }
                }

                LaunchedEffect(copiedCode) {
                    if (copiedCode.isNotEmpty()) {
                        delay(2000)
                        copiedCode = ""
                    }
                }

                val filteredCodes = codes.filter { it.codes.codeType == codeType }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.secondary)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(LocalCustomColors.current.blue900),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { finish() }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Return",
                                tint = MaterialTheme.colorScheme.background
                            )
                        }
                        Text(
                            text = if (codeType == "PRESCRIPTION") "Prescriptions" else "Referrals",
                            fontSize = 23.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.background,
                            modifier = Modifier.padding(start = 8.dp).padding(vertical = 30.dp)
                        )
                    }

                    if (filteredCodes.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp)
                                .background(MaterialTheme.colorScheme.secondary),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = if (codeType == "PRESCRIPTION") Icons.Default.Receipt else Icons.Default.MedicalServices,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = if (codeType == "PRESCRIPTION") "No active prescriptions" else "No active referrals",
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 25.dp)
                        ) {
                            items(filteredCodes) { codeItem ->
                                CodeCard(
                                    codeData = codeItem.codes,
                                    onCopyClick = { code ->
                                        clipboardManager.setText(AnnotatedString(code))
                                        copiedCode = code
                                    },
                                    isCopied = copiedCode == codeItem.codes.code
                                )
                                Spacer(modifier = Modifier.height(15.dp))
                            }
                        }
                    }

                    if (copiedCode.isNotEmpty()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Code copied to clipboard",
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(16.dp),
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}