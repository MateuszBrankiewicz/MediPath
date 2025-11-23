package com.medipath.modules.patient.codes.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.medipath.R
import com.medipath.core.models.CodeItem
import com.medipath.core.theme.LocalCustomColors
import com.medipath.modules.shared.components.formatDate

@Composable
fun CodeCard(
    codeItem: CodeItem,
    onCopyClick: (String) -> Unit,
    onMarkAsUsedClick: () -> Unit,
    onDeleteClick: () -> Unit,
    isCopied: Boolean = false
) {
    val customColors = LocalCustomColors.current
    var showMarkAsUsedDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (codeItem.codes.isActive) 
                MaterialTheme.colorScheme.background 
            else 
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = if (codeItem.codes.codeType == "PRESCRIPTION") 
                            Icons.Default.Receipt 
                        else 
                            Icons.Default.MedicalServices,
                        contentDescription = null,
                        tint = if (codeItem.codes.isActive) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = codeItem.codes.code,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (codeItem.codes.isActive) 
                                MaterialTheme.colorScheme.onSurface 
                            else 
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (codeItem.codes.isActive) 
                                    customColors.green800 
                                else 
                                    MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (codeItem.codes.isActive) stringResource(R.string.active) else stringResource(R.string.used),
                                fontSize = 14.sp,
                                color = if (codeItem.codes.isActive) 
                                    customColors.green800 
                                else 
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                IconButton(
                    onClick = { onCopyClick(codeItem.codes.code) }
                ) {
                    Icon(
                        Icons.Default.ContentCopy,
                        contentDescription = stringResource(R.string.copy_code),
                        tint = if (isCopied) 
                            customColors.green800 
                        else 
                            MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.doctor_title),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = codeItem.doctor,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.date_title),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = formatDate(codeItem.date),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }


                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { showMarkAsUsedDialog = true },
                        modifier = Modifier.weight(1f),
                        enabled = codeItem.codes.isActive,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = customColors.blue800
                        )
                    ) {
                        Text(stringResource(R.string.mark_as_used))
                    }

                    Button(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LocalCustomColors.current.red800
                        )
                    ) {
                        Text(stringResource(R.string.delete))
                    }
                }
        }
    }
    
    if (showMarkAsUsedDialog) {
        AlertDialog(
            onDismissRequest = { showMarkAsUsedDialog = false },
            title = { Text(stringResource(R.string.confirm_mark_as_used)) },
            text = { 
                Text(
                    stringResource(
                        R.string.mark_as_used_confirmation,
                        if (codeItem.codes.codeType == "PRESCRIPTION") "prescription" else "referral",
                        codeItem.codes.code
                    ))
            },
            confirmButton = {
                Button(
                    onClick = {
                        showMarkAsUsedDialog = false
                        onMarkAsUsedClick()
                    }
                ) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showMarkAsUsedDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            containerColor = MaterialTheme.colorScheme.background
        )
    }
    
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.confirm_deletion)) },
            text = { 
                Text(
                    stringResource(
                        R.string.confirm_deletion_message,
                        if (codeItem.codes.codeType == "PRESCRIPTION") "prescription" else "referral",
                        codeItem.codes.code
                    ))
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteClick()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(stringResource(R.string.delete_capital))
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            containerColor = MaterialTheme.colorScheme.background
        )
    }
}