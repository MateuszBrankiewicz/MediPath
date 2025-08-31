package com.medipath.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    hintText: String,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    errorMessage: String = "",
    onFocusLost: () -> Unit = {}
) {
    var hadFocus by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        OutlinedTextField(
            value, onValueChange,
            placeholder = { Text(hintText, color = Color(0xFF5D5D5D), fontSize = 14.sp) },
            visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            modifier = Modifier.fillMaxWidth()
                .onFocusChanged { focusState ->
                    if (focusState.isFocused) {
                        hadFocus = true
                    } else if (hadFocus){
                        onFocusLost()
                    }
                },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (errorMessage.isNotEmpty()) Color.Red else Color.Transparent,
                unfocusedBorderColor = if (errorMessage.isNotEmpty()) Color.Red else Color.Transparent,
                focusedContainerColor = Color(0xFFD9D9D9),
                unfocusedContainerColor = Color(0xFFD9D9D9)
            ),
            shape = RoundedCornerShape(20.dp),
            isError = errorMessage.isNotEmpty()
        )
        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = Color.Red,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

