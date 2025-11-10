package com.medipath.modules.shared.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class ActionButton(
    val icon: ImageVector,
    val label: String,
    val onClick: () -> Unit,
    val color: Color? = null,
    val isOutlined: Boolean = false
)

@Composable
fun GenericActionButtonsRow(
    buttons: List<ActionButton>,
    buttonsPerRow: Int = 3,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        buttons.chunked(buttonsPerRow).forEach { rowButtons ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowButtons.forEach { button ->
                    if (button.isOutlined) {
                        OutlinedButton(
                            onClick = button.onClick,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(30.dp),
                            colors = button.color?.let {
                                ButtonDefaults.outlinedButtonColors(
                                    contentColor = it
                                )
                            } ?: ButtonDefaults.outlinedButtonColors()
                        ) {
                            Icon(
                                imageVector = button.icon,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(button.label, fontSize = 12.sp)
                        }
                    } else {
                        Button(
                            onClick = button.onClick,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(30.dp),
                            colors = button.color?.let {
                                ButtonDefaults.buttonColors(
                                    containerColor = it
                                )
                            } ?: ButtonDefaults.buttonColors()
                        ) {
                            Icon(
                                imageVector = button.icon,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(button.label, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
