package com.example.myapplication.createqr

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.R
import com.example.myapplication.resultscan.ResultMultiQrDetailActivity
import com.example.myapplication.room.AppDatabase
import com.example.myapplication.room.entity.QrCreateEntity
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun CreateUrlScreen(
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    var url by remember { mutableStateOf(TextFieldValue("http://")) }
    val db = AppDatabase.getInstance(context)
    val scope = rememberCoroutineScope() // Tạo CoroutineScope để gọi hàm suspend

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1C1E26))
            .padding(16.dp)
    ) {
        // Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = "Website",
                color = Color.White,
                fontSize = 20.sp,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Input URL
        OutlinedTextField(
            value = url,
            onValueChange = { url = it },
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.link),
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = {
                if (url.text.isNotEmpty()) {
                    IconButton(onClick = { url = TextFieldValue("") }) {
                        Icon(
                            painter = painterResource(R.drawable.close),
                            contentDescription = "Clear",
                            tint = Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                focusedContainerColor = Color(0xFF2A2C36),
                unfocusedContainerColor = Color(0xFF2A2C36),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = Color.White
            ),
            shape = MaterialTheme.shapes.medium
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Shortcut buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            AssistChip(
                onClick = { url = TextFieldValue(url.text + "www.") },
                label = { Text("www.") },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = Color(0xFF2A2C36),
                    labelColor = Color.White
                )
            )
            AssistChip(
                onClick = { url = TextFieldValue(url.text + ".com") },
                label = { Text(".com") },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = Color(0xFF2A2C36),
                    labelColor = Color.White
                )
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Create button
        Button(
            onClick = {
                if (url.text.isNotEmpty()) {
                    // Tạo mã QR
                    val qrCodeWriter = QRCodeWriter()
                    val bitMatrix = qrCodeWriter.encode(url.text, BarcodeFormat.QR_CODE, 512, 512)
                    val bitmap = Bitmap.createBitmap(
                        512, 512, Bitmap.Config.ARGB_8888
                    ).apply {
                        for (x in 0 until width) {
                            for (y in 0 until height) {
                                setPixel(x, y, if (bitMatrix[x, y]) Color.Black.toArgb() else Color.White.toArgb())
                            }
                        }
                    }

                    // Lưu ảnh QR vào bộ nhớ trong
                    val imagePath1 = saveBitmapToInternalStorage1(context, bitmap)
                    val imagePath=Uri.fromFile(File(imagePath1))
                    if (imagePath != null) {
                        scope.launch {
                            val qrEntity = QrCreateEntity(
                                value = url.text,
                                type = 0, // Giả định type 0 cho URL
                                imagePath = imagePath.toString()
                            )
                            db.qrCreate().insert(qrCreate = qrEntity)

                            // Chuyển hướng đến ResultMultiQrDetailActivity
                            val intent = Intent(context, ResultMultiQrDetailActivity::class.java)
                            intent.putExtra("scan_result", qrEntity.value)
                            intent.putExtra("scan_type", qrEntity.type)
                            intent.putExtra("qr_image_uri", qrEntity.imagePath)
                            context.startActivity(intent)
                        }
                    } else {
                        Log.e("QrScanner", "Lỗi khi lưu ảnh QR")
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
            shape = RoundedCornerShape(25.dp),
            enabled = url.text.isNotEmpty()
        ) {
            Text("Create", color = Color.White, fontSize = 16.sp)
        }
    }
}

private fun saveBitmapToInternalStorage1(context: android.content.Context, bitmap: Bitmap): String? {
    return try {
        val filename = "qr_${System.currentTimeMillis()}.png"
        val file = File(context.filesDir, filename)
        file.outputStream().use { output ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
        }
        bitmap.recycle() // Giải phóng bitmap
        file.absolutePath
    } catch (e: Exception) {
        Log.e("QrScanner", "Lỗi khi lưu bitmap", e)
        null
    }
}