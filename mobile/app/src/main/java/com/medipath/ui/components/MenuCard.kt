package com.medipath.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MenuCard(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    backgroundColor: Color,
    iconColor: Color
) {
    Column(
        modifier = Modifier
            .background(
                color = backgroundColor,
                shape = MaterialTheme.shapes.large
            )
            .clickable { onClick() }
            .padding(horizontal = 5.dp, vertical = 15.dp)
            .width(70.dp)
            .height(90.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(45.dp)
                .background(iconColor, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = "Show $title",
                tint = MaterialTheme.colorScheme.background
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            color = MaterialTheme.colorScheme.background,
            fontWeight = FontWeight.W400,
            fontSize = 16.sp,
            letterSpacing = (-1.12).sp,
            textAlign = TextAlign.Center,
            lineHeight = 11.2.sp
        )
    }
}
