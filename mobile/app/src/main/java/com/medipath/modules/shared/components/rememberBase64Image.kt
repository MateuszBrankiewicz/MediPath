package com.medipath.modules.shared.components

import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import com.medipath.R

@Composable
fun rememberBase64Image(base64String: String?): ImageBitmap? {
    return remember(base64String) {
        try {
            if (base64String.isNullOrEmpty()) return@remember null

            val base64Data = if (base64String.contains(",")) {
                base64String.substringAfter(",")
            } else {
                base64String
            }

            val decodedBytes = Base64.decode(base64Data, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)

            bitmap?.asImageBitmap()
        } catch (_: Exception) {
            null
        }
    }
}

@Composable
fun rememberBase64ImagePicker(
    onImageSelected: (String) -> Unit
): ManagedActivityResultLauncher<String, Uri?> {
    val context = LocalContext.current
    
    return rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val bytes = inputStream?.readBytes()
                inputStream?.close()
                
                bytes?.let { imageBytes ->
                    val base64String = Base64.encodeToString(imageBytes, Base64.NO_WRAP)
                    onImageSelected("data:image/jpeg;base64,$base64String")
                }
            } catch (_: Exception) {
                Toast.makeText(context,
                    context.getString(R.string.error_loading_image), Toast.LENGTH_SHORT).show()
            }
        }
    }
}