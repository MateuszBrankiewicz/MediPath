package com.medipath.modules.patient.visits.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.medipath.R
import com.medipath.core.models.Code

@Composable
fun CodeItem(code: Code, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = color.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.code_label) + " " + code.code,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = color
        )
        Text(
            text = if (code.active) stringResource(R.string.active) else stringResource(R.string.inactive),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (code.active) Color(0xFF4CAF50) else Color(0xFF9E9E9E),
            modifier = Modifier
                .background(
                    color = if (code.active) Color(0xFF4CAF50).copy(alpha = 0.1f) else Color(
                        0xFF9E9E9E
                    ).copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}